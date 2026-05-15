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
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.logger :as logger]
            [environ.core :refer [env]]
            [pronouns.pages :as pages]
            [clojure.string :as s])
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

  (GET "/robots.txt" []
    {:status 200
     :headers {"Content-Type" "text"}
     :body "User-agent: *\nDisallow: /coffee"})

  (GET "/coffee" []
    {:status 418
     :headers {"Content-Type" "text/html"}
     :body "<strong>Sorry, this device cannot brew coffee</strong>"})

  (ANY "/DEBUG-FORCE-500" []
    (throw (Exception. "oh no a DEBUG-FORCE-500 error occurred!")))

  (GET "/*" {params :params}
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (pages/pronouns params)})

  (ANY "*" {params :params}
    (-> params
        s/lower-case
        pages/not-found
        route/not-found)))

(defn wrap-gnu-natalie-nguyen [handler]
  (fn [req]
    (when-let [resp (handler req)]
      (assoc-in resp [:headers "X-Clacks-Overhead"] "GNU Natalie Nguyen"))))

(defn wrap-slash-normalization [handler]
  (fn [req]
    (let [uri (:uri req)
          normalized (s/replace uri #"/{2,}" "/")]
      (handler (assoc req :uri normalized)))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           (log/error e)
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (pages/error req)}))))

(def base-middleware
  #(-> %
       wrap-slash-normalization
       wrap-content-type
       wrap-not-modified
       logger/wrap-with-logger
       wrap-error-page
       wrap-gnu-natalie-nguyen
       wrap-params))

(def prod-app
  (base-middleware app-routes))

(def dev-app
  (-> app-routes
      wrap-stacktrace
      base-middleware))

(defn no-port []
  (log/fatal "PORT environment variable is required"
             "Example: PORT=3000 lein run")
  (System/exit 1))

(defn uri-compliance-legacy
  "Jetty configurator function to set UriCompliance to LEGACY.

  This configuration allows URIs containing multiple subsequent slashes
  to pass through to Ring, where we handle them as a single slash.

  For context see:
  https://jetty.org/docs/jetty/12.1/programming-guide/server/compliance.html#uri
  and this issue on the Jetty project:
  https://github.com/jetty/jetty.project/issues/11298 "
  [^org.eclipse.jetty.server.Server server]
  (doseq [^org.eclipse.jetty.server.Connector
          connector (.getConnectors server)

          ^org.eclipse.jetty.server.HttpConnectionFactory
          factory (.getConnectionFactories connector)

          :when (instance? org.eclipse.jetty.server.HttpConnectionFactory factory)

          :let [^org.eclipse.jetty.server.HttpConfiguration
                conf (.getHttpConfiguration factory)]]
    (.setUriCompliance conf org.eclipse.jetty.http.UriCompliance/LEGACY)))

(defn -main []
  (if-let [port (:port env)]
    (jetty/run-jetty prod-app
                     {:port (Integer/parseInt port)
                      :configurator uri-compliance-legacy})
    (no-port)))
