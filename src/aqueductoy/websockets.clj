(ns aqueductoy.websockets
  "Browser subscriptions with bi-directional communication"
  (:require [compojure.core :as c]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.aleph :refer (get-sch-adapter)]
            [gniazdo.core :as ws]
            [hiccup.core :as h]
            [ring.middleware.anti-forgery :as af]))

(defn notify [data])

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def connected-uids connected-uids) ; Watchable, read-only atom
  )

(comment
  @connected-uids)

;; send message to particular clients
;; authentication / identification

(defn page [req]
  (h/html
    [:html {:lang "en"}
     [:head
      [:meta {:charset "UTF-8"}]
      [:title "Title"]]
     [:body
      [:h1 "HELLO??"]
      [:div#sente-csrf-token {:data-csrf-token (force af/*anti-forgery-token*)}]
      [:script {:src "js/compiled/main.js" :type "text/javascript"}]]]))

;; might want to var trick here
(c/defroutes websocket-routes
  (c/GET "/client" req #'page)
  (c/GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (c/POST "/chsk" req (ring-ajax-post req)))

#_
(comment
  (def socket
    (ws/connect
      "ws://localhost:3000/chsk"
      :on-receive #(prn 'received %)
      :on-connect #(prn "CONNECTED" %)
      :on-error #(prn "ERROR" %)))
  (ws/send-msg socket "hello")
  (ws/close socket)


  (chsk-send! ; Using Sente
    [:some/request-id {:name "Rich Hickey" :type "Awesome"}] ; Event
    8000 ; Timeout
    ;; Optional callback:
    (fn [reply] ; Reply is arbitrary Clojure data
      (if (sente/cb-success? reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
        (do-something! reply)
        (error-handler!))))
  )
