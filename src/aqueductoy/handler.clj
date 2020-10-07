(ns aqueductoy.handler
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [ring.middleware.defaults :as d]))

(defn hello [req]
  "<h1>Hello World</h1>")

(defn get-subscription [req]
  "GOT IT")

(defn put-subscription [req]
  "PUT IT")

(defn delete-subscription [req]
  "DELETE IT")

(defn get-subscriptions [req]
  "GET EM")

(c/defroutes app
  (c/GET "/" req #'hello)
  (c/GET "/subscription" req #'get-subscriptions)
  (c/PUT "/subscription" req #'put-subscription)
  (c/DELETE "/subscription" req #'delete-subscription)
  (c/GET "/subscription/:id" req #'get-subscription)

  (route/not-found "<h1>Page not found</h1>"))

(def handler
  (d/wrap-defaults #'app {}))
