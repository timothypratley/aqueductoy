(ns aqueductoy.handler
  (:require [clojure.pprint :as pprint]
            [compojure.core :as c]
            [compojure.route :as route]
            [ring.middleware.defaults :as defaults]
            [aqueductoy.webhooks :as webhooks]
            [aqueductoy.websockets :as websockets]))

; curl -X GET http://localhost:3000/
(defn hello [_req]
  (str "<h1>Hello World</h1>"
       "<p>Subscriptions that will be ran on this route:</p>"
       (with-out-str (pprint/pprint @webhooks/*subscriptions))))

(c/defroutes app
  (c/GET "/" _req #'hello)
  #'webhooks/webhook-routes
  #'websockets/websocket-routes
  (route/not-found "<h1>Page not found</h1>"))

(def handler
  (-> #'app
      (defaults/wrap-defaults defaults/site-defaults)))
