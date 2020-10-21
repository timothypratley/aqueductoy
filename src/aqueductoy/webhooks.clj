(ns aqueductoy.webhooks
  "Use case: Third party implementation of a service"
  (:refer-clojure :exclude [send])
  (:require [compojure.core :as c]
            [ring.util.request :as request]
            [clojure.pprint :as pprint]
            [clj-http.client :as client])
  (:import (java.util Date)))

;;;; db

;; queries are [:page 100]

;; This is a map of user identifiers to maps of queries and callbacks
;; '{user-id {{:query query,
;;             :callback-url callback-url]}
;;            {:other :stuff}}
(defonce *subscriptions
  (atom {}))


;;;; model

(defn add-subscription
  [user-id query callback-url]
  (swap! *subscriptions update user-id assoc {:query query
                                              :callback-url callback-url}
         {:created-at (Date.)}))

(defn send [data]
  (doseq [[user subscriptions] @*subscriptions
          [{:keys [query callback-url]} _] subscriptions]
    (prn query callback-url)
    (when (and query data)
      (client/post callback-url))))

;;;; request plumbing

; curl -X POST 'http://localhost:3000/subscriptions?query=blah&callback_url=http://localhost:3000/echo'
(defn post-subscriptions [{{:keys [query callback_url]} :params :as req}]
  (prn req)
  ;; user, query, callback
  (add-subscription "default-user" query callback_url)
  "subscribed")

; curl -X GET http://localhost:3000/subscriptions/one
(defn get-subscriptions [req]
  (let [k (get-in req [:params :id] req)
        v (get @*subscriptions k)]
    (if v
      (str "<h1>Subscription " k " to " v "</h1>")
      (str "<h1>Subscription " k " does not exist</h1>"))))

; curl -X PUT 'http://localhost:3000/subscriptions/one?query=blah&callback_url=bleh'
(defn put-subscriptions [req]
  (let [k (get-in req [:params :id] req)
        v (request/body-string req)]
    (swap! *subscriptions assoc k v)
    (str "<h1>Updated " k " subscription to " v "</h1>")))

; curl -X DELETE http://localhost:3000/subscriptions/one
(defn delete-subscriptions [req]
  (let [k (get-in req [:params :id] req)]
    (swap! *subscriptions dissoc k)
    (str "<h1>Removed " k " subscription</h1>")))

; curl -X GET http://localhost:3000/subscriptions
(defn get-subscriptions [req]
  (with-out-str (pprint/pprint @*subscriptions)))

; curl -X POST http://localhost:3000/echo
(defn get-echo [req]
  (doto "GOT IT" prn))

;;;; routes

(c/defroutes webhook-routes
  (c/GET "/subscriptions" req #'get-subscriptions)
  (c/POST "/subscriptions" req #'post-subscriptions)
  (c/PUT "/subscriptions/:id" req #'put-subscriptions)
  (c/DELETE "/subscriptions/:id" req #'delete-subscriptions)
  (c/GET "/subscriptions/:id" req #'get-subscriptions)
  (c/POST "/echo" req #'get-echo))

