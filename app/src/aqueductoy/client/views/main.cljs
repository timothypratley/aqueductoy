(ns aqueductoy.client.views.main
  (:require [aqueductoy.client.views.subscriptions :as s]))

(defn <main> [name]
  [:div
   [:h1 "It kinda works!"]
   [s/<subscriptions>]])
