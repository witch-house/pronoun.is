;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2018 Morgan Astra

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <https://www.gnu.org/licenses/>

(ns pronouns.pages
  (:require [clojure.string :as s]
            [pronouns.config :refer [pronouns-table]]
            [pronouns.util :as u]
            [hiccup.core :refer :all]
            [hiccup.element :as e]
            [hiccup.util :refer [escape-html]]))

(defn prose-comma-list
  [items]
  (let [c (count items)]
    (cond
      (<= c 1) (or (first items) "")
      (= c 2) (s/join " and " items)
      :else (str (s/join ", " (butlast items)) ", and " (last items)))))

(defn href
  [url text]
  [:a {:href url} text])

;; FIXME morgan.astra <2018-11-14 Wed>
;; use a div for this instead of a plain bold tag
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
  (render-sentence "At least I think it was "
                   (wrap-pronoun possessive-pronoun)
                   "."))

(defn reflexive-example
  [subject reflexive]
  (render-sentence (wrap-pronoun (s/capitalize subject))
                   " threw the frisbee to "
                   (wrap-pronoun reflexive)
                   "."))

(defn header-block [header]
  [:div {:class "section title"}
   (href "/" [:h1 header])])

(defn examples-block
  [subject object possessive-determiner possessive-pronoun reflexive]
  (let [sub-obj (s/join "/" [subject object])
        header-str (str "Here are some example sentences using my "
                        sub-obj
                        " pronouns:")]
    [:div {:class "section examples"}
     [:h2 header-str]
     [:p (subject-example subject)
         (object-example object)
         (posessive-determiner-example subject possessive-determiner)
         (possessive-pronoun-example possessive-pronoun)
         (reflexive-example subject reflexive)]]))

(defn usage-block []
  [:div {:class "section usage"}
   [:p "Full usage: "
    ;; FIXME morgan.astra <2018-11-14 Wed>
    ;; This looks really ugly in the browser
       [:tt "https://pronoun.is/subject-pronoun/object-pronoun/possessive-determiner/possessive-pronoun/reflexive"]
       " displays examples of your pronouns."]
   [:p "This is a bit unwieldy. If we have a good guess we'll let you use"
       " just the first one or two."]])

(defn contact-block []
  (let [twitter-name (fn [handle] (href (str "https://www.twitter.com/" handle)
                                       (str "@" handle)))]
    [:div {:class "section contact"}
     [:p "Written by "
         (twitter-name "morganastra")
         ", whose "
         (href "https://pronoun.is/she" "pronoun.is/she")]
     [:p "pronoun.is is free software under the "
         (href "https://www.gnu.org/licenses/agpl.html" "AGPLv3")
         "! visit the project on "
         (href "https://github.com/witch-house/pronoun.is" "github")]
     [:p "&lt;3"]]))

(defn footer-block []
  [:footer (usage-block) (contact-block)])

(defn format-pronoun-examples
  [pronoun-declensions]
  (let [sub-objs (map #(s/join "/" (take 2 %)) pronoun-declensions)
        title (str "Pronoun Island: " (prose-comma-list sub-objs) " examples")
        examples (map #(apply examples-block %) pronoun-declensions)]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:meta {:charset "utf-8"}]
       [:meta {:name "description" :content (u/strip-markup examples)}]
       [:meta {:name "twitter:card" :content "summary"}]
       [:meta {:name "twitter:title" :content title}]
       [:meta {:name "twitter:description" :content (u/strip-markup examples)}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (header-block title)
       examples
       (footer-block)]])))

(defn table-lookup* [pronouns-string]
  (let [inputs (s/split pronouns-string #"/")
        n (count inputs)]
    (if (>= n 5)
      (take 5 inputs)
      (u/table-lookup inputs @pronouns-table))))

(defn lookup-pronouns
  "Given a seq of pronoun sets, look up each set in the pronouns table"
  [pronoun-sets]
  (->> pronoun-sets
       (map (comp table-lookup* escape-html))
       (filter some?)))

(defn make-link [path]
  (let [link (str "/" path)
        label path]
    [:li (href link label)]))

(defn front []
  (let [abbreviations (take 6 (u/abbreviate @pronouns-table))
        links (map make-link abbreviations)
        title "Pronoun Island"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:meta {:charset "utf-8"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (header-block title)
       [:div {:class "section table"}
        [:p "pronoun.is is a website for personal pronoun usage examples"]
        [:p "here are some pronouns the site knows about:"]
        [:ul links]
        [:p [:small (href "all-pronouns" "see all pronouns in the database")]]]]
      (footer-block)])))

(defn all-pronouns []
  (let [abbreviations (u/abbreviate @pronouns-table)
        links (map make-link abbreviations)
        title "Pronoun Island"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:meta {:charset "utf-8"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (header-block title)
       [:div {:class "section table"}
        [:p "All pronouns the site knows about:"]
        [:ul links]]]
      (footer-block)])))

(defn not-found [path]
  (let [title "Pronoun Island: English Language Examples"
        or-re #"/[oO][rR]/"]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:name "viewport" :content "width=device-width"}]
       [:meta {:charset "utf-8"}]
       [:link {:rel "stylesheet" :href "/pronouns.css"}]]
      [:body
       (header-block title)
       [:div {:class "section examples"}
        [:p [:h2 "We couldn't find those pronouns in our database :("]
         "If you think we should have them, please reach out!"]
        (when (re-find or-re path)
          (let [alts (s/split path or-re)
                new-path (str "/" (s/join "/:OR/" alts))]
            [:div
             "Did you mean: "
             (href new-path
                   (str "pronoun.is"
                        new-path))]))]
       (footer-block)]])))

(defn pronouns [params]
  (let [path (params :*)
        param-alts (u/vec-coerce (or (params "or") []))
        path-alts (s/split path #"/:[oO][rR]/")
        pronouns (lookup-pronouns (concat path-alts param-alts))]
    (if (seq pronouns)
      (format-pronoun-examples pronouns)
      (not-found path))))
