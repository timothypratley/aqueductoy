(ns aqueductoy.log)

(defprotocol Logger
  (log [this msg]))

(extend-type nil Logger
  (log [this msg]
    (println msg)))

(defn info [logger service desc]
  (log logger {:service service :level :info :description desc}))

(defn warn [logger service desc]
  (log logger {:service service :level :warn :description desc}))

(defn error [logger service desc]
  (log logger {:service service :level :error :description desc}))

(defn with-context [m logger]
  (reify Logger
    (log [this msg] (log logger (merge m msg)))))
