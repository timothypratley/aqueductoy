(ns aqueductoy.main
  (:require [aleph.http :as http]
            [aqueductoy.handler :as h]))

(defonce *server (atom nil))

(defn start []
  (if @*server
    (println "Server is already running")
    (reset! *server
            (http/start-server #'h/handler {:port  3000}))))

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
