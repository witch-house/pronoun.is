;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2017 Morgan Astra

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>

(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [ring.middleware.logger :as logger]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.params :as params]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [pronouns.util :as u]
            [pronouns.pages :as pages]))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/front)})

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
      logger/wrap-with-logger
      wrap-error-page
      trace/wrap-stacktrace
      params/wrap-params))

(defn -main []
  (let [port (Integer. (:port env))]
    (jetty/run-jetty app {:port port})))
