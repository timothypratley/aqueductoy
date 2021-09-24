(ns aqueductoy.client.main
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
    [aqueductoy.client.db :as db]
    [aqueductoy.client.views.main :as mv]
    [reagent.core :as r]
    [reagent.dom :as rd]
    [cljs.core.async :as async :refer (<! >! put! chan)]
    [taoensso.sente  :as sente :refer (cb-success?)] ; <--- Add this
    ))

;;; Add this: --->

(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

#_
(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
        "/chsk" ; Note the same path as before
        ?csrf-token
        {:type :auto ; e/o #{:auto :ajax :ws}
         })]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )



(defn main []
  (if-let [el (.getElementById js/document "app")]
    (rd/render [mv/<main>] el)
    (println "Dom node #app not found")))

(main)
