(ns aqueductoy.handler
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [compojure.core :as c]
            [compojure.route :as route]
            [ring.middleware.defaults :as d]
            [ring.util.request :as util.request]))

(defonce *subscriptions (atom {}))

; curl -X GET http://localhost:3000/
(defn hello [req]
  (str "<h1>Hello World</h1>"
       "<p>Subscriptions that will be ran on this route:</p>"
       (with-out-str (pprint/pprint @*subscriptions))))

; curl -X GET http://localhost:3000/subscription/one
(defn get-subscription [req]
  (let [k (get-in req [:params :id] req)
        v (get @*subscriptions k)]
    (if v
      (str "<h1>Subscription " k " to " v "</h1>")
      (str "<h1>Subscription " k " does not exist</h1>"))))

; curl -X PUT -d 'https://www.google.com/' http://localhost:3000/subscription/one
(defn put-subscription [req]
  (let [k (get-in req [:params :id] req)
        v (util.request/body-string req)]
    (swap! *subscriptions assoc k v)
    (str "<h1>Updated " k " subscription to " v "</h1>")))

; curl -X DELETE http://localhost:3000/subscription/one
(defn delete-subscription [req]
  (let [k (get-in req [:params :id] req)]
    (swap! *subscriptions dissoc k)
    (str "<h1>Removed " k " subscription</h1>")))

; curl -X GET http://localhost:3000/subscription
(defn get-subscriptions [req]
  (with-out-str (pprint/pprint @*subscriptions)))

; curl -X POST -d '["one" "https://www.google.com/"]' http://localhost:3000/subscription
(defn post-subscriptions [req]
  (let [[k v] (edn/read-string (util.request/body-string req))]
    (swap! *subscriptions assoc k v)
    (str "<h1>Added " k " subscription to " v "</h1>")))

(c/defroutes app
  (c/GET "/" req #'hello)
  (c/GET "/subscription" req #'get-subscriptions)
  (c/POST "/subscription" req #'post-subscriptions)
  (c/PUT "/subscription/:id" req #'put-subscription)
  (c/DELETE "/subscription/:id" req #'delete-subscription)
  (c/GET "/subscription/:id" req #'get-subscription)

  (route/not-found "<h1>Page not found</h1>"))

(def handler
  (d/wrap-defaults #'app {}))
