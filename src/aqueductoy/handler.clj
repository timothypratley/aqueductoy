(ns aqueductoy.handler
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [ring.middleware.defaults :as d]))

(defn hello [req]
  "<h1>Hello World</h1>")

(c/defroutes app
  (c/GET "/" req #'hello)
  (route/not-found "<h1>Page not found</h1>"))

(def handler
  (d/wrap-defaults app {}))
