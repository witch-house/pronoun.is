(defproject witch-house/pronouns "1.12.0-SNAPSHOT"
  :description "Pronoun.is is a website for personal pronoun usage examples"
  :url "https://pronoun.is"
  :license "GNU Affero General Public License 3.0"
  :dependencies [[compojure "1.6.2"]
                 [environ "1.2.0"]
                 [hiccup "1.0.5"]
                 [lambdaisland/ring.middleware.logger "0.5.1"]
                 [org.clojure/clojure "1.10.3"]
                 [ring/ring-devel "1.9.4"]
                 [ring/ring-jetty-adapter "1.9.4"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1" :hooks false]
            [lein-ring "0.9.7"]
            [lein-ancient "1.0.0-RC3"]
            [org.clojure/core.unify "0.5.7"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "pronouns-standalone.jar"
  ;; FIXME morgan.astra <2018-11-14 Wed>
  ;; Is this production profile used for anything?
  :profiles {:production {:env {:production true}}}
  :ring {:handler pronouns.web/app})
