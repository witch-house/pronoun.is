(ns pronouns.util
  (:require [clojure.string :as s]))

(defn slurp-tabfile [path]
  (let [lines (s/split (slurp path) #"\n")]
    (map #(s/split % #"\t") lines)))

(defn table-lookup
  [query-key table]
  (let [arity (count query-key)
        filtered-table (filter #(= query-key (take arity %)) table)]
    (first filtered-table)))

(defn tabfile-lookup
  [query-key tabfile]
  (table-lookup query-key (slurp-tabfile tabfile)))

(defn minimum-unambiguous-path
  ([pronouns-table sections] (minimum-unambiguous-path pronouns-table sections 1))
  ([pronouns-table sections number-of-sections]
    (let [sections-subset (take number-of-sections sections)
          results (filter #(= (take number-of-sections %) sections-subset) pronouns-table)]
      (case (count results)
        0 nil
        1 (clojure.string/join "/" sections-subset)
        (recur pronouns-table sections (+ number-of-sections 1))))))

(defn abbreviate
  "given a list of pronoun rows, return a list of minimum unabiguous paths"
  [pronouns-table]
  (map (partial minimum-unambiguous-path pronouns-table) pronouns-table))
