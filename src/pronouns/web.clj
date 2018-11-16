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

(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.logger :as logger]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.params :as params]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [environ.core :refer [env]]
            [pronouns.util :as u]
            [pronouns.pages :as pages]))

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

  (GET "/*" {params :params headers :headers}
       (if (= "application/json" (s/lower-case (get headers "accept")))
         {:status 200
          :headers {"Content-Type" "application/json"}
          :body (pages/pronouns-json params pronouns-table)}
         {:status 200
          :headers {"Content-Type" "text/html"}
          :body (pages/pronouns params pronouns-table)}))

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

(def app
  (-> app-routes
      ;; FIXME morgan.astra <2018-11-14 Wed>
      ;; use this resource or delete it
      ;; (wrap-resource "images")
      wrap-content-type
      wrap-not-modified
      logger/wrap-with-logger
      wrap-error-page
      wrap-gnu-natalie-nguyen
      trace/wrap-stacktrace
      params/wrap-params))

(defn -main []
  (let [port (Integer. (:port env))]
    (jetty/run-jetty app {:port port})))
