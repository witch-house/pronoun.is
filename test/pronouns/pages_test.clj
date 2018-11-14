(ns pronouns.pages-test
  (:require [pronouns.pages :as pages]
            [clojure.test :refer [deftest testing is are]]))

(deftest prose-comma-list
  (testing "prose-comma-list turns a list of strings into a prose list"
    (are [call result] (= call result)
      (pages/prose-comma-list ["foo"]) "foo"
      (pages/prose-comma-list ["foo" "bar"]) "foo and bar"
      (pages/prose-comma-list ["foo" "bar" "baz"]) "foo, bar, and baz"
      (pages/prose-comma-list ["foo" "bar" "baz" "bobble"]) "foo, bar, baz, and bobble"
      (pages/prose-comma-list []) "")))
