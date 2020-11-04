(ns aqueductoy.main
  (:require [aleph.http :as http]
            [aqueductoy.handler :as h]
            [aqueductoy.db :as db]))

(defonce *server (atom nil))

(def config
  {:server {:port 3000}
   :db     {:db-uri "asami:mem://dbname"}})

(defn start* [{:keys [db server]}]
  (db/init db)
  (http/start-server #'h/handler server))

(defn start []
  (if @*server
    (println "Server is already running")
    (reset! *server (start* config))))

(defn stop []
  (if @*server
    (do (.close @*server)
        (reset! *server nil))
    (println "Server is not running")))

(defn restart []
  (stop)
  (start))

(defn -main []
  (start))

(comment
  (restart))



;; http://swannodette.github.io/2013/08/17/comparative/ 
;From Filipe Silva to Everyone: (7:22 AM)
; example of rxjs operation:
;https://rxjs-dev.firebaseapp.com/api/index/function/combineLatest
;
;example of rxjs code for that operation:
;https://www.learnrxjs.io/learn-rxjs/operators/combination/combinelatest
