(ns aqueductoy.handler
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [compojure.core :as c]
            [compojure.route :as route]
            [ring.middleware.defaults :as defaults]
            [ring.util.request :as util.request]
            [cheshire.core :as json]
            [aqueductoy.webhooks :as webhooks]))

#_
(extend-type clojure.core.async.impl.channels.ManyToManyChannel
  StreamableResponseBody
  (write-body-to-stream [channel response output-stream]
    (async/go (with-open [writer (io/writer output-stream)]
                (async/loop []
                            (when-let [msg (async/<! channel)]
                              (doto writer (.write msg) (.flush))
                              (recur)))))))

(def stream-response
  (partial assoc {:status 200, :headers {"Content-Type" "text/event-stream"}} :body))

(def EOL "\n")

#_(defn stream-msg [payload]
  (str "data:" (json/write-str payload) EOL EOL))




; curl -X GET http://localhost:3000/
(defn hello [req]
  (str "<h1>Hello World</h1>"
       "<p>Subscriptions that will be ran on this route:</p>"
       (with-out-str (pprint/pprint @webhooks/*subscriptions))))

; curl -X GET http://localhost:3000/subscription/one
(defn get-subscription [req]
  (let [k (get-in req [:params :id] req)
        v (get @webhooks/*subscriptions k)]
    (if v
      (str "<h1>Subscription " k " to " v "</h1>")
      (str "<h1>Subscription " k " does not exist</h1>"))))

; curl -X PUT -d 'https://www.google.com/' http://localhost:3000/subscription/one
(defn put-subscription [req]
  (let [k (get-in req [:params :id] req)
        v (util.request/body-string req)]
    (swap! webhooks/*subscriptions assoc k v)
    (str "<h1>Updated " k " subscription to " v "</h1>")))

; curl -X DELETE http://localhost:3000/subscription/one
(defn delete-subscription [req]
  (let [k (get-in req [:params :id] req)]
    (swap! webhooks/*subscriptions dissoc k)
    (str "<h1>Removed " k " subscription</h1>")))

; curl -X GET http://localhost:3000/subscription
(defn get-subscriptions [req]
  (with-out-str (pprint/pprint @webhooks/*subscriptions)))

; curl -X POST -d '["one" "https://www.google.com/"]' http://localhost:3000/subscription
(defn post-subscriptions [{{:keys [query callback_url]} :params :as req}]
  (webhooks/add-subscription "default-user" query callback_url)
  "subscribed")

(c/defroutes app
  (c/GET "/" req #'hello)

  ;;(c/POST "/")

  (c/GET "/subscription" req #'get-subscriptions)
  (c/POST "/subscription" req #'post-subscriptions)
  (c/PUT "/subscription/:id" req #'put-subscription)
  (c/DELETE "/subscription/:id" req #'delete-subscription)
  (c/GET "/subscription/:id" req #'get-subscription)

  #_(c/GET "/async" []
       (fn [req res raise]
         (let [ch (async/chan)]
           (res (stream-response ch))
           (async/go (async/>! ch (stream-msg {:val 42}))
                     (async/<! (async/timeout 1000))
                     (async/>! ch (stream-msg {:val 100}))
                     (async/close! ch)))))

  (route/not-found "<h1>Page not found</h1>"))

(def handler
  (defaults/wrap-defaults #'app defaults/api-defaults))
