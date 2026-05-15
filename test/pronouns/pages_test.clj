(ns pronouns.pages-test
  (:require [pronouns.pages :as pages]
            [clojure.walk :as walk]
            [clojure.test :refer [deftest testing are is]]
            [clojure.string :as s]))

;; Tests for page logic functions
(deftest ^:unit prose-comma-list
  (testing "`prose-comma-list` turns a list of strings into a prose list"
    (are [v s] (= (pages/prose-comma-list v) s)
      ["foo" "bar" "baz" "bobble"] "foo, bar, baz, and bobble"
      ["foo" "bar" "baz"]          "foo, bar, and baz"
      ["foo" "bar"]                "foo and bar"
      ["foo"]                      "foo"
      []                           "")))

(deftest ^:unit lookup-pronouns
  (are [pronoun-strs pronouns]
      (= (pages/lookup-pronouns pronoun-strs)
         pronouns)
    ["she/her"]           '(["she" "her" "her" "hers" "herself"])
    ["she" "they"]        '(["she" "her" "her" "hers" "herself"]
                            ["they" "them" "their" "theirs" "themselves"])
    ["she/her" "foo/bar"] '(["she" "her" "her" "hers" "herself"])
    ["foo/bar"]           '()
    ["a/b/c/d/e"]         '(("a" "b" "c" "d" "e"))))

;; Tests for page construction
(defn is-element?
  "Is this <form> the element described by <tag>?"
  [tag form]
  (when (and (vector? form)
             (= (first form) tag))
    form))

(defn find-element-by-tag
  "Return the seq of children of <form> that match <tag>"
  [tag form]
  (walk/walk (partial is-element? tag)
             (partial remove nil?)
             form))

(defn find-element-by-path
  "Return the element at path in the body.

  path - A sequence of lookup keys in traversal order
         lookup keys can either be a keyword representing a tag,
         or a [k v] pair to be found in an attributes map.

         If a [k v] pair, the resulting attributes map will be returned
         directly without traversing further.

  body - A hiccup-style vector tree representing an HTML document"
  [path body]
  (if-let [key (first path)]
    (if (vector? key)
      (let [[k v] key]
        (first (filter #(= v (k %)) body)))
      (let [matches (find-element-by-tag key body)]
        (recur (rest path) (apply concat matches))))
    body))

(defn assert-element-values
  "Generate test assertion that a given value is found at a given path
  within the document tree.

  FIXME: The argument ordering is a historical accident of the
  implementation and makes very little sense."
  ([v path form key-fn]
   (testing (str "Assert element values: " v path))
   (is (= v
          (key-fn
           (find-element-by-path path form)))))
  ([v path form]
   (assert-element-values v path form identity)))

(defn assert-has-head-block [result title]
  (testing (str title " has head block")
    (assert-element-values title [:head :title] result second)
    (assert-element-values "/pronouns.css"
                           [:head :link [:rel "stylesheet"]]
                           result
                           :href)
    (assert-element-values "width=device-width"
                           [:head :meta [:name "viewport"]]
                           result
                           :content)))

(defn assert-contact-block [result title]
  (testing (str title " has contact block")
    (let [anchors (find-element-by-path [:body :footer :div :p :a]
                                        result)
          labels (into #{} (filter string? anchors))]
      (is (labels "@morganastra"))
      (is (labels "pronoun.is/she"))
      (is (labels "AGPLv3"))
      (is (labels "github")))))

(defn assert-twitter-card
  ([result title description]
   (testing (str title " has twitter card meta block")
     (assert-element-values title
                            [:head :meta [:name "twitter:title"]]
                            result
                            :content)
     (assert-element-values "summary"
                            [:head :meta [:name "twitter:card"]]
                            result
                            :content)
     (let [pg-desc (:content (find-element-by-path [:head :meta [:name "description"]]
                                                   result))
           tw-desc (:content (find-element-by-path [:head :meta
                                                    [:name "twitter:description"]]
                                                   result))]
       (is (string? pg-desc))
       (is (string? tw-desc))
       (if (some? description)
         (is (= description pg-desc tw-desc))
         (is (= pg-desc tw-desc))))))
  ([result title] (assert-twitter-card result title nil)))

(defn assert-no-twitter-card [result title]
  (testing (str title " should NOT have a twitter card")
    (is nil? (find-element-by-path [:head :meta [:name "twitter:card"]]
                                   result))))

(defn assert-open-html [page]
  (is (s/starts-with? page "<html><head>")))

(defn assert-close-html [page]
  (is (s/ends-with? page "</body></html>")))

(defn assert-final-render-html [data]
  (let [page (pages/render data)]
    (assert-open-html page)
    (assert-close-html page)))

(deftest ^:unit format-pronoun-examples*-page
  (let [result (pages/format-pronoun-examples*
                '(["she" "her" "her" "hers" "herself"]))
        title "Pronoun Island: she/her examples"]
    (assert-has-head-block result title)
    (assert-twitter-card result title)
    (assert-contact-block result title)
    (assert-final-render-html result)))

(deftest ^:unit front*-page
  (let [result (pages/front*)
        title "Pronoun Island"
        description "Pronoun.is is a website for personal pronoun usage examples."]
    (assert-has-head-block result title)
    (assert-contact-block result title)
    (assert-twitter-card result title description)
    (assert-final-render-html result)))

(deftest ^:unit all-pronouns*-page
  (let [result (pages/all-pronouns*)
        title "Pronoun Island"
        description "Pronoun.is is a website for personal pronoun usage examples."]
    (assert-has-head-block result title)
    (assert-twitter-card result title description)
    (assert-contact-block result title)
    (assert-final-render-html result)))

(deftest ^:unit not-found*-page
  (let [result (pages/not-found* "/foo/bar")
        title "Pronoun Island - Not Found :("]
    (assert-has-head-block result title)
    (assert-contact-block result title)
    (assert-no-twitter-card result title)
    (assert-final-render-html result)))

(deftest ^:unit error*-page
  (let [result (pages/error* "/foo/bar")
        title "Pronoun Island - Error :("]
    (assert-has-head-block result title)
    (assert-contact-block result title)
    (assert-no-twitter-card result title)
    (assert-final-render-html result)))

(deftest ^:unit pronouns-page-render
  (let [pages (map pages/pronouns [{:* "/asdf"}
                                   {:* "/ze/:or/they"}
                                   {:* "/a/b/c/d/e"}
                                   {:* "/he" "or" "they"}
                                   {:* "/she"}])]
    (doseq [page pages]
      (assert-open-html page)
      (assert-close-html page))))
