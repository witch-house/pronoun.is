(ns pronouns.pages
  (:require [clojure.string :as s]
            [pronouns.util :as u]
            [hiccup.core :refer :all]))

(defn subject-example
  [subject]
  [:span#blah [:b subject] " went to the park."])

(defn format-pronoun-examples
  [subject object possessive-determiner possessive-pronoun reflexive]
  (s/join "\n"
          [(str subject " went to the park")
           (html (subject-example subject))
           (str "I went with " object)
           (str subject " brought " possessive-determiner " frisbee")
           (str "at least I think it was " possessive-pronoun)
           (str subject " threw it to " reflexive)]))


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
