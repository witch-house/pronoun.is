(ns pronouns.resource-test
  (:require [pronouns.util :as util]
            [clojure.test :refer [deftest testing is]]))

(deftest valid-pronouns-table
  (let [table (util/slurp-tabfile "resources/pronouns.tab")]
    (is table "pronouns.tab exists and is non-empty")
    (doseq [row table]
      (is (= (count row) 6)
          "row has six elements")
      (is (re-matches #".*sel(f|ves)$" (first (take-last 2 row)))
          "penultimate element is reflexive")
      (is (re-matches #"#[0-9A-F]{6}$" (last row)))
          "last element is a hexadecimal")))
