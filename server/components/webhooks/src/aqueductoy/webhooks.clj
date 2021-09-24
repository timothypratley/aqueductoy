(ns aqueductoy.webhooks
  "Use case: Third party implementation of a service"
  (:require [compojure.core :as c]
            [ring.util.request :as request]
            [clojure.pprint :as pprint]
            [clj-http.client :as client]
            [aqueductoy.log :as log]
            [integrant.core :as ig])
  (:import (java.util Date)))

(defmethod ig/init-key :aqueductoy/webhooks [_ {:keys [db]}]
  {:db             db
   ;; For now storing state in atom instead of db
   ;; This is a map of user identifiers to maps of queries and callbacks
   ;; '{user-id {{:query query,
   ;;             :callback-url callback-url]}
   ;;            {:other :stuff}}
   :*subscriptions (atom {})})

(defmethod ig/halt-key! :aqueductoy/webhooks [_ {:keys []}]
  )


;;;; db

;; queries are [:page 100]

;;;; model

(defn add-subscription
  [this user-id query callback-url]
  (swap! (:*subscriptions this) update user-id assoc
         {:query        query
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

(defn notify [this data]
  (doseq [[user subscriptions] @(:*subscriptions this)
          [{:keys [query callback-url]} _] subscriptions]
    (when (and query data)
      ;; async? outbound queue size? monitoring?
      (try
        (client/post callback-url {:content-type :json
                                   :form-params  data})
        (catch Exception ex
          (log/warn (:logger this) ::fail {:reason       "Failed to send outgoing webhook"
                                           :callback-url callback-url
                                           :exception    ex}))))))

;;;; request plumbing

; curl -X POST 'http://localhost:3000/subscriptions?query=blah&callback_url=http://localhost:3000/echo'
(defn post-subscriptions [this {{:keys [query callback_url]} :params}]
  ;; user, query, callback
  (add-subscription this "default-user" query callback_url)
  "subscribed")

; curl -X GET http://localhost:3000/subscriptions/one
(defn get-subscriptions [this req]
  (let [k (get-in req [:params :id] req)
        v (get @(:subscriptions this) k)]
    (if v
      (str "<h1>Subscription " k " to " v "</h1>")
      (str "<h1>Subscription " k " does not exist</h1>"))))

; curl -X PUT 'http://localhost:3000/subscriptions/one?query=blah&callback_url=bleh'
(defn put-subscriptions [this req]
  (let [k (get-in req [:params :id] req)
        v (request/body-string req)]
    (swap! (:subscriptions this) assoc k v)
    (str "<h1>Updated " k " subscription to " v "</h1>")))

; curl -X DELETE http://localhost:3000/subscriptions/one
(defn delete-subscriptions [this req]
  (let [k (get-in req [:params :id] req)]
    (swap! (:subscriptions this) dissoc k)
    (str "<h1>Removed " k " subscription</h1>")))

; curl -X GET http://localhost:3000/subscriptions
(defn get-subscriptions [this req]
  (with-out-str (pprint/pprint @(:subscriptions this))))

; curl -X POST -d 'hi' http://localhost:3000/echo
(defn get-echo [this {:keys [params] :as req}]
  (println "ECHO:" params)
  "OK")

; curl -X GET http://localhost:3000/
(defn hello [this _req]
  (str "<h1>Hello World</h1>"
       "<p>Subscriptions that will be ran on this route:</p>"
       (with-out-str (pprint/pprint @(:subscriptions this)))))

;;;; routes

(defn routes [this]
  (c/routes
    (c/GET "/" req (hello this req))
    (c/GET "/subscriptions" req (get-subscriptions this req))
    (c/POST "/subscriptions" req (post-subscriptions this req))
    (c/PUT "/subscriptions/:id" req (put-subscriptions this req))
    (c/DELETE "/subscriptions/:id" req (delete-subscriptions this req))
    (c/GET "/subscriptions/:id" req (get-subscriptions this req))
    (c/POST "/echo" _req #'get-echo)))
