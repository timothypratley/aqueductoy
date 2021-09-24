(ns aqueductoy.client.views.subscriptions
  (:require [reagent.core :as r]
            [reanimated.core :as anim]))

(def *show (r/atom false))

(let [size (r/atom 0.01)
      spring-size (anim/interpolate-to size)]
  (defn <subscriptions> []
    [:div {:style {:transition "all 1s ease"
                   :transform-origin "0 0"}}
     [:h1 "Subscriptions"]
     [:textarea {:rows 10 :style {:width "100%"}}]
     [:div
      [:button {:on-click (fn [ev]
                            (reset! size (if (= @size 1)
                                           0.01
                                           1)))}
       "A"]
      [:button {:on-click (fn [ev]
                            (reset! size (if (= @size 1)
                                           0.01
                                           1))
                            (swap! *show not))}
       ">"]
      " this is a title"]
     [anim/pop-when
      @*show
      [:div {:style {:overflow "hidden"}}
       [:div {:style {;;:zoom       @spring-size
                      :height           "100%"
                      :transform        (str
                                          ;;"translateX(" (* -10 (- 1 @spring-size)) ") "
                                          "scale(1," @spring-size ") "

                                          )
                      :transform-origin "0 0"
                      :border           "solid black 1px"
                      :box-sizing       "border-box"
                      ;;:overflow   "hidden"
                      }}
        [:ul
         [:li "1 hahahaha"]
         [:li "2 hohohoho"]
         [:li "3 hehehehe"]]]]]
     [:p "Lorem ipsum"]]))
