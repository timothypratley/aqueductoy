(ns aqueductoy.websocket
  "Browser subscriptions with bi-directional communication"
  (:require [compojure.core :as c]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.aleph :as aa]
            [hiccup.core :as h]
            [ring.middleware.anti-forgery :as af]
            [integrant.core :as ig]))

;; send message to particular clients
;; authentication / identification

;; Events from the clients

(defmulti handle-event :id)

(defmethod handle-event :default [{:keys [event]}]
  (println "Unhandled event: " event))

(defmethod handle-event :some-event [{:keys [?data]}]
  (println "Got" ?data))

(defmethod handle-event :chsk/uidport-open [{:keys [uid client-id]}]
  (println "New connection:" uid client-id))

(defmethod handle-event :chsk/uidport-close [{:keys [uid]}]
  (println "Disconnected:" uid))

(defmethod handle-event :chsk/ws-ping [_])


;; Don't really want a hiccup page, but using it for now due to CSRF

(defn page [this req]
  (h/html
    [:html {:lang "en"}
     [:head
      [:meta {:charset "UTF-8"}]
      [:title "Title"]]
     [:body
      [:h1 "HELLO??"]
      ;; TODO: This does not add true security and is distracting.
      ;; The key issue here is to make sure both routes are protected when Origin does not match...
      ;; which should be the case already?
      ;; What is stopping an attacker from getting the token by requesting this page?
      ;; The answer is the origin header, but if that is true for the websocket routes, then this is redundant.
      ;; Things to check:
      ;; 1. Does the difference between POST and GET matter?
      ;; 2. Why does everyone believe csrf-tokens are undefeatable? (Is it more than just an extra call?)
      ;; 3. Are people assuming that the websocket server might not have the same protections in place on the server side?
      ;; 4. Does ring even check Origin normally? (pretty sure it does!)
      [:div#sente-csrf-token {:data-csrf-token (force af/*anti-forgery-token*)}]
      [:script {:src "js/compiled/main.js" :type "text/javascript"}]]]))

(defn get-chsk [this req]
  ((:ring-ajax-get-or-ws-handshake (:channel-socket this)) req))

(defn post-chsk [this req]
  ((:ring-ajax-post (:channel-socket this)) req))

(defn send! [this & args]
  (apply (:send-fn (:channel-socket this)) args))

(defn notify [x]
  ;; figure out who to send stuff to
  )

;; record??
;; just match?
(defn routes [this]
  (c/routes
    (c/GET "/client" req (page this req))
    (c/GET "/chsk" req (get-chsk this req))
    (c/POST "/chsk" req (post-chsk this req))))

(defmethod ig/init-key :aqueductoy/websocket [_ {:keys [logger]}]
  (let [channel-socket (sente/make-channel-socket!
                         (aa/get-sch-adapter)
                         ;; TODO: does this actually do anything? Shouldn't ring take care of this??
                         {:allowed-origins #{"http://localhost:3000"}})]
    {:stop-router    (sente/start-chsk-router! (:ch-recv channel-socket) handle-event)
     :channel-socket channel-socket
     :routes         routes
     :config         {}}))

(defmethod ig/halt-key! :aqueductoy/websocket [_ {:keys [stop-router]}]
  (stop-router))
