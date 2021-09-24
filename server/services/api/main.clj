(ns aqueductoy.main
  (:require [aqueductoy.config :as config]
            [integrant.core :as ig]))

(ig/load-namespaces @config/config)

(defn -main [& args]
  (ig/init @config/config))

(comment
  (restart))



;; http://swannodette.github.io/2013/08/17/comparative/
;From Filipe Silva to Everyone: (7:22 AM)
; example of rxjs operation:
;https://rxjs-dev.firebaseapp.com/api/index/function/combineLatest
;
;example of rxjs code for that operation:
;https://www.learnrxjs.io/learn-rxjs/operators/combination/combinelatest
