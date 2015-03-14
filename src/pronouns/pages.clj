(ns pronouns.pages
  (:require [clojure.string :as s]
            [pronouns.util :as u]
            [hiccup.core :refer :all]))

(defn wrap-pronoun
  [pronoun]
  [:b pronoun])

(defn wrap-para
  [whatever]
  [:p whatever])

(defn subject-example
  [subject]
  (wrap-para
   [:span#sentence (wrap-pronoun subject) " went to the park."]))

(defn object-example
  [object]
  (wrap-para
   [:span#sentence "I went with " (wrap-pronoun object) "."]))

(defn posessive-determiner-example
  [subject possessive-determiner]
  (wrap-para
   [:span#sentence (wrap-pronoun subject) " brought " (wrap-pronoun possessive-determiner) " frisbee."]))

(defn possessive-pronoun-example
  [possessive-pronoun]
  (wrap-para
   [:span#sentence "At least I think it was " (wrap-pronoun possessive-pronoun) "."]))

(defn reflexive-example
  [subject reflexive]
  (wrap-para
   [:span#sentence (wrap-pronoun subject) " threw it to " (wrap-pronoun reflexive)]))


(defn twitter-name [name]
  [:a {:href (str "https://www.twitter.com/" name)} (str "@" name)])

(defn contact-block []
  [:div {:class "contact"}
   [:p "Written by " (twitter-name "morganastra") " and " (twitter-name "thelseraphim") "."]
   [:p "visit the project on " [:a {:href "https://github.com/witch-house/pronoun.is"} "github."]]])


(defn about-block []
  [:div {:class "about"}
   [:p "Full usage:"]
   [:p
    [:tt "http://pronoun.is/subject-pronoun/object-pronoun/possessive-determiner/possessive-pronoun/reflexive"]
    " displays examples of your pronouns. If we have a good guess we'll let you use just the first one or two."]
   [:p "Quick examples:"]
   [:p "My name is Thel Seraphim, my " [:a {:href "http://pronoun.is/she"} "pronoun.is/she"] "."]
   [:p "My name is Morgan, my " [:a {:href "http://pronoun.is/ze/zir"} "pronoun.is/ze/zir"] "."]])



(defn examples-block
  [subject object possessive-determiner possessive-pronoun reflexive]
  [:div {:class "examples"}
   [:p [:h1 "Usage examples:"]]
   (subject-example subject)
   (object-example object)
   (posessive-determiner-example subject possessive-determiner)
   (possessive-pronoun-example possessive-pronoun)
   (reflexive-example subject reflexive)])

(defn format-pronoun-examples
  [subject object possessive-determiner possessive-pronoun reflexive]
  (html
   [:html
    [:head ""]
    [:body
     (examples-block subject object possessive-determiner possessive-pronoun reflexive)
     (about-block)
     (contact-block)]]))


(defn parse-pronouns-with-lookup [pronouns-string pronouns-table]
  (let [inputs (s/split pronouns-string #"/")
        n (count inputs)]
    (if (>= n 5)
      (take 5 inputs)
      (u/table-lookup inputs pronouns-table))))

(defn front []
  (str "pronoun.is is a www site for showing people how to use pronouns"))

(defn not-found []
  (str "We couldn't find those pronouns in our database, please ask us to "
       "add them, or issue a pull request at "
       "https://github.com/witch-house/pronoun.is/blob/master/resources/pronouns.tab"))

(defn pronouns [path pronouns-table]
  (let [pronouns (parse-pronouns-with-lookup path pronouns-table)]
    (if pronouns
      (apply format-pronoun-examples pronouns)
      (not-found))))
