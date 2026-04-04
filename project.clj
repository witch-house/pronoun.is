(defproject witch-house/pronouns "1.12.0-SNAPSHOT"
  :description "Pronoun.is is a website for personal pronoun usage examples"
  :url "https://pronoun.is"
  :license "GNU Affero General Public License 3.0"
  :dependencies [[compojure "1.7.2"]
                 [environ "1.2.0"]
                 [hiccup "2.0.0"]
                 [lambdaisland/ring.middleware.logger "0.5.1"]
                 [org.clojure/clojure "1.12.4"]
                 [ring/ring-devel "1.15.3"]
                 [ring/ring-jetty-adapter "1.15.3"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-environ "1.2.0"]
            [lein-ring "0.12.6"]]
  :uberjar-name "pronouns-standalone.jar"
  :main pronouns.web
  ;; FIXME morgan.astra <2018-11-14 Wed>
  ;; Is this production profile used for anything?
  :profiles {:production {:env {:production true}}
           :dev {:ring {:handler pronouns.web/dev-app}}
           :uberjar {:aot :all
                     :ring {:handler pronouns.web/prod-app}
                     :dependencies [[com.github.clj-easy/graal-build-time "1.0.5"]]}}
  :ring {:handler pronouns.web/app})
