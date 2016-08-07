(ns pronouns.pages
  (:require [clojure.string :as s]
            [pronouns.util :as u]
            [hiccup.core :refer :all]
            [hiccup.util :refer [escape-html]]))

(defn wrap-pronoun
  [pronoun]
  [:b pronoun])

(defn render-sentence [& content]
  [:p [:span.sentence content]])

(defn subject-example
  [subject]
  (render-sentence (wrap-pronoun (s/capitalize subject)) " went to the park."))

(defn object-example
  [object]
  (render-sentence "I went with " (wrap-pronoun object) "."))

(defn posessive-determiner-example
  [subject possessive-determiner]
  (render-sentence (wrap-pronoun (s/capitalize subject))
                   " brought "
                   (wrap-pronoun possessive-determiner)
                   " frisbee."))

(defn possessive-pronoun-example
  [possessive-pronoun]
  (render-sentence "At least I think it was " (wrap-pronoun possessive-pronoun) "."))

(defn reflexive-example
  [subject reflexive]
  (render-sentence (wrap-pronoun (s/capitalize subject))
                   " threw the frisbee to "
                   (wrap-pronoun reflexive)
                   "."))

(defn title-block [title]
  [:div {:class "title"}
   [:h1 title]])

(defn examples-block
  [subject object possessive-determiner possessive-pronoun reflexive]
  (let [sub-obj (str subject "/" object)
        header-str (str "Here are some usage examples for my "
                        sub-obj
                        " pronouns")]
    [:div {:class "examples"}
     [:p [:h2 header-str]]
     (subject-example subject)
     (object-example object)
     (posessive-determiner-example subject possessive-determiner)
     (possessive-pronoun-example possessive-pronoun)
     (reflexive-example subject reflexive)]))

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
  [pronoun-declension alternates]
  (let [title "Pronoun Island: English Language Examples"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (title-block title)
       (apply examples-block pronoun-declension)
       (map #(apply examples-block %) alternates)
       (about-block)
       (contact-block)]])))

(defn lookup-pronouns [pronouns-string pronouns-table]
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
       [:ul links]]]
      (contact-block)])))

(defn not-found []
  (str "We couldn't find those pronouns in our database, please ask us to "
       "add them, or issue a pull request at "
       "https://github.com/witch-house/pronoun.is/blob/master/resources/pronouns.tab"))

(defn pronouns [params pronouns-table]
  (let [path (params :*)
        ors (u/vec-coerce (params "or"))
        pronoun-declension (lookup-pronouns (escape-html path)
                                            pronouns-table)
        alternates (map #(lookup-pronouns (escape-html %)
                                          pronouns-table)
                        ors)]
    (if pronoun-declension
      (format-pronoun-examples pronoun-declension alternates)
      (not-found))))
