;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2026 Morgan Astra

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

(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [environ.core :refer [env]]
            [pronouns.pages :as pages])
  (:gen-class))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/front)})

  (GET "/all-pronouns" []
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/all-pronouns)})

  (GET "/pronouns.css" []
     {:status 200
     :headers {"Content-Type" "text/css"}
     :body (slurp (io/resource "pronouns.css"))})

  (GET "/*" {params :params}
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/pronouns params)})

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-gnu-natalie-nguyen [handler]
  (fn [req]
    (when-let [resp (handler req)]
      (assoc-in resp [:headers "X-Clacks-Overhead"] "GNU Natalie Nguyen"))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           (binding [*out* *err*]
             {:status 500
              :headers {"Content-Type" "text/html"}
              :body (slurp (io/resource "500.html"))})))))

(def base-middleware
  #(-> %
       wrap-content-type
       wrap-not-modified
       wrap-with-logger
       wrap-error-page
       wrap-gnu-natalie-nguyen
       wrap-params))

(def prod-app
  (base-middleware app-routes))

(def dev-app
  (-> app-routes
      base-middleware
      wrap-stacktrace
      wrap-reload))

(defn -main []
  (when-not (:port env)
    (binding [*out* *err*]
      (println "Error: PORT environment variable is required")
      (println "Example: PORT=3000 lein run"))
    (System/exit 1))
  (let [port (Integer. (:port env))]
    (jetty/run-jetty prod-app {:port port})))
