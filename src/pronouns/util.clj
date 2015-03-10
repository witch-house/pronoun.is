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
