;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2016 Morgan Astra

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>

(ns pronouns.pages
  (:require [clojure.string :as s]
            [pronouns.config :refer [*pronouns-table*]]
            [pronouns.util :as u]
            [hiccup.core :refer :all]
            [hiccup.util :refer [escape-html]]))

(defn href
  [url text]
  [:a {:href url} text])

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
  [:div {:class "section title"}
   [:h1 title]])

(defn examples-block
  [subject object possessive-determiner possessive-pronoun reflexive]
  (let [sub-obj (str subject "/" object)
        header-str (str "Here are some usage examples for my "
                        sub-obj
                        " pronouns")]
    [:div {:class "section examples"}
     [:p [:h2 header-str]]
     (subject-example subject)
     (object-example object)
     (posessive-determiner-example subject possessive-determiner)
     (possessive-pronoun-example possessive-pronoun)
     (reflexive-example subject reflexive)]))

(defn about-block []
  [:div {:class "section about"}
   [:p "Full usage: "
    [:tt "http://pronoun.is/subject-pronoun/object-pronoun/possessive-determiner/possessive-pronoun/reflexive"]
    " displays examples of your pronouns."]
   [:p "This is a bit unwieldy. If we have a good guess we'll let you use just the first one or two."]])

(defn contact-block []
  (let [twitter-name (fn [handle] (href (str "https://www.twitter.com/" handle)
                                       (str "@" handle)))]
  [:div {:class "section contact"}
   [:p
    "Written by "
    (twitter-name "morganastra")
    ", whose "
    (href "http://pronoun.is/ze/zir?or=she" "pronoun.is/ze/zir?or=she")
    [:br]
    [:br]
    "pronoun.is is free software under the "
    (href "https://www.gnu.org/licenses/agpl.html" "AGPLv3")
    "! visit the project on "
    (href "https://github.com/witch-house/pronoun.is" "github")
    [:br]
    [:br]
    "<3"]]))


(defn format-pronoun-examples
  [pronoun-declensions]
  (let [title "Pronoun Island: English Language Examples"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (title-block title)
       (map #(apply examples-block %) pronoun-declensions)
       (about-block)
       (contact-block)]])))

(defn lookup-pronouns [pronouns-string]
  (let [inputs (s/split pronouns-string #"/")
        n (count inputs)]
    (if (>= n 5)
      (take 5 inputs)
      (u/table-lookup inputs *pronouns-table*))))

(defn make-link [path]
  (let [link (str "/" path)
        label path]
    [:li (href link label)]))

(defn front []
  (let [abbreviations (u/abbreviate *pronouns-table*)
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
       [:div {:class "section table"}
       [:p "pronoun.is is a website for personal pronoun usage examples"]
       [:p "here are some pronouns the site knows about:"]
       [:ul links]]]
      (contact-block)])))

(defn not-found []
  (let [title "Pronoun Island: English Language Examples"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (title-block title)
      [:div {:class "section examples"}
       [:p [:h2 (str "We couldn't find those pronouns in our database."
                     "If you think we should have them, please reach out!")]]]
       (about-block)
       (contact-block)]])))

(defn pronouns [params]
  (let [path (params :*)
        alts (or (params "or") [])
        pronouns (concat [path] (u/vec-coerce alts))
        pronoun-declensions (filter some? (map #(lookup-pronouns
                                                 (escape-html %))
                                               pronouns))]
    (if (seq pronoun-declensions)
      (format-pronoun-examples pronoun-declensions)
      (not-found))))
