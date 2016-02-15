(ns pronouns.pages
  (:require [clojure.string :as s]
            [clojure.data.json :as json]
            [pronouns.util :as u]
            [hiccup.core :refer :all]
            [hiccup.util :refer [escape-html]]))

(defn wrap-pronoun
  [pronoun]
  [:b pronoun])

(declare capitalize)
(defn capitalize-html [[name attrs content]]
  (if (= :input name)
    [name (update-in attrs [:placeholder] capitalize) content]
    [name (assoc attrs :data-capitalize true) (capitalize content)]))

(defn capitalize [html]
  (cond
    (string? html) (s/capitalize html)
    (vector? html) (capitalize-html html)
    :else html))

(defn render-sentence [& content]
  [:p [:span.sentence content]])

(defn subject-example
  [subject]
  (render-sentence (wrap-pronoun (capitalize subject)) " went to the park."))

(defn object-example
  [object]
  (render-sentence "I went with " (wrap-pronoun object) "."))

(defn posessive-determiner-example
  [subject possessive-determiner]
  (render-sentence (wrap-pronoun (capitalize subject))
                   " brought "
                   (wrap-pronoun possessive-determiner)
                   " frisbee."))

(defn possessive-pronoun-example
  [possessive-pronoun]
  (render-sentence "At least I think it was " (wrap-pronoun possessive-pronoun) "."))

(defn reflexive-example
  [subject reflexive]
  (render-sentence (wrap-pronoun (capitalize subject))
                   " threw the frisbee to "
                   (wrap-pronoun reflexive)
                   "."))

(defn title-block [title]
  [:div {:class "title"}
   [:h1 title]])

(defn examples-block
  [subject object possessive-determiner possessive-pronoun reflexive]
  [:div {:class "examples"}
   [:p [:h2 "Here are some usage examples for my pronouns:"]]
   (subject-example subject)
   (object-example object)
   (posessive-determiner-example subject possessive-determiner)
   (possessive-pronoun-example possessive-pronoun)
   (reflexive-example subject reflexive)])

(defn custom-pronoun-block [subject object possessive-determiner possessive-pronoun reflexive]
  [:div {:class "custom-pronoun"}
   [:p "Fill out the example to create a link for your own pronouns: "
       [:a {:class "url"}]]
   [:form
    (subject-example [:input {:name "subject" :placeholder subject}])
    (object-example [:input {:name "object" :placeholder object}])
    (posessive-determiner-example
      [:span {:data-refer "subject"} subject]
      [:input {:name "possessive-determiner" :placeholder possessive-determiner}])
    (possessive-pronoun-example [:input {:name "possessive-pronoun" :placeholder possessive-pronoun}])
    (reflexive-example
      [:span {:data-refer "subject"} subject]
      [:input {:name "reflexive" :placeholder reflexive}])]])

(defn about-block []
  [:div {:class "about"}
   [:p "Full usage: "
    [:tt "http://pronoun.is/subject-pronoun/object-pronoun/possessive-determiner/possessive-pronoun/reflexive"]
    " displays examples of your pronouns."]
   [:p "This is a bit unwieldy. If we have a good guess we'll let you use just the first one or two."]])

(defn contact-block []
  (let [twitter-name (fn [handle] [:a {:href (str "https://www.twitter.com/" handle)} (str "@" handle)])]
   [:div {:class "contact"}
    [:p
     "Written by "
     (twitter-name "morganastra")
     ", whose "
     [:a {:href "http://pronoun.is/ze/zir"} "pronoun.is/ze/zir"]
     ". "
     "Visit the project on " [:a {:href "https://github.com/witch-house/pronoun.is"} "github!"]]]))


(defn format-pronoun-examples
  [subject object possessive-determiner possessive-pronoun reflexive]
  (let [title "Pronoun Island: English Language Examples"]
   (html
    [:html
     [:head
      [:title title]
      [:meta {:name "viewport" :content "width=device-width"}]
      [:link {:rel "stylesheet" :href "/pronouns.css"}]]
     [:body
      (title-block title)
      (examples-block subject object possessive-determiner possessive-pronoun reflexive)
      (about-block)
      (contact-block)]])))


(defn format-pronoun-json [pronouns]
  (json/write-str pronouns))


(defn parse-pronouns-with-lookup [pronouns-string pronouns-table]
  (let [inputs (s/split pronouns-string #"/")
        n (count inputs)]
    (if (>= n 5)
      (take 5 inputs)
      (u/table-lookup inputs pronouns-table))))

(defn make-link [path]
  (let [link (str "/" path)
        label path]
    [:li [:a {:href link} label]]))

(defn front [pronouns-table]
  (let [abbreviations (u/abbreviate pronouns-table)
        links (map make-link abbreviations)
        title "Pronoun Island"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (title-block title)
       [:div {:class "table"}
        [:p "pronoun.is is a www site for showing people how to use pronouns in English."]
        [:p "here are some pronouns the site knows about:"]
        [:ul links]]
       (apply custom-pronoun-block (rand-nth pronouns-table))
       (contact-block)
       [:script {:src "/custom-pronouns.js"}]]])))

(defn not-found []
  (str "We couldn't find those pronouns in our database, please ask us to "
       "add them, or issue a pull request at "
       "https://github.com/witch-house/pronoun.is/blob/master/resources/pronouns.tab"))

(defn pronouns [path pronouns-table accept]
  (let [pronouns (parse-pronouns-with-lookup (escape-html path) pronouns-table)]
    (if pronouns
      (if (= accept :json)
        (format-pronoun-json pronouns)
        (apply format-pronoun-examples pronouns))
      (not-found))))
