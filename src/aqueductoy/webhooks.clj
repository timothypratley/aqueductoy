(ns aqueductoy.webhooks
  "Use case: Third party implementation of a service"
  (:require [compojure.core :as c]
            [ring.util.request :as request]
            [clojure.pprint :as pprint]
            [clj-http.client :as client]
            [clojure.tools.logging :as log])
  (:import (java.util Date)))

;;;; db

;; queries are [:page 100]

;; This is a map of user identifiers to maps of queries and callbacks
;; '{user-id {{:query query,
;;             :callback-url callback-url]}
;;            {:other :stuff}}
(defonce *subscriptions
  (atom {}))

(defonce *delivery
  (atom {}))


;;;; model

(defn add-subscription
  [user-id query callback-url]
  (swap! *subscriptions update user-id assoc
         {:query query
          :callback-url callback-url}
         {:created-at (Date.)}))

;; Guarantee order, and delivery, but not single delivery
;;;; What if it gets too far behind?
;;;; How far behind is too far behind?
;;;; 10 updates happen within 10ms, send each each takes 200ms, 2 seconds to send them all
;;;; Should it be configurable?
;;;; Retry when request is not accepted
;;;; An identifier would be useful to detect duplicate delivery
;; Implementation
;;;; Threads, core.async, manifest database
;; Queue needs to be durable (for now we'll simulate in memory)
;;;; Per user:endpoint What hasn't been sent
;;;; ... t1 ... t2
;;;; SQS

(defn notify [data]
  (doseq [[user subscriptions] @*subscriptions
          [{:keys [query callback-url]} _] subscriptions]
    (when (and query data)
      ;; async? outbound queue size? monitoring?
      (try
        (client/post callback-url {:content-type :json
                                   :form-params data})
        (catch Exception ex
          (log/warn "Failed to send outgoing webhook" callback-url (str ex)))))))

;;;; request plumbing

; curl -X POST 'http://localhost:3000/subscriptions?query=blah&callback_url=http://localhost:3000/echo'
(defn post-subscriptions [{{:keys [query callback_url]} :params}]
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

; curl -X POST -d 'hi' http://localhost:3000/echo
(defn get-echo [{:keys [params] :as req}]
  (println "ECHO:" params)
  "OK")

;;;; routes

(c/defroutes webhook-routes
  (c/GET "/subscriptions" _req #'get-subscriptions)
  (c/POST "/subscriptions" _req #'post-subscriptions)
  (c/PUT "/subscriptions/:id" _req #'put-subscriptions)
  (c/DELETE "/subscriptions/:id" _req #'delete-subscriptions)
  (c/GET "/subscriptions/:id" _req #'get-subscriptions)
  (c/POST "/echo" _req #'get-echo))
