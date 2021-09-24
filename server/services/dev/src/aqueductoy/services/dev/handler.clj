(ns aqueductoy.services.dev.handler
  (:require [compojure.core :as c]
            [integrant.core :as ig]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.anti-forgery]))
(defn deep-merge [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))

(defmethod ig/init-key :aqueductoy.services.dev/handler [_ {:keys [route-providers]}]
  (let [f (apply c/routes
                 (for [provider route-providers
                       :let [{:keys [routes]} provider]]
                   ;; This function calls the routes of the provider passing the provider as the first argument,
                   ;; in the same way you would call an object method.
                   ;; Thus the provider can be stateful, which is necessary for it to have dependencies like a db.
                   (fn [req]
                     (routes provider req))))
        config (apply deep-merge
                      defaults/site-defaults
                      (map :config route-providers))]
    (prn "CONFIG" config)
    {:handler (defaults/wrap-defaults f config)}))

(defmethod ig/halt-key! :aqueductoy.services.dev/handler [_ handler]
  )

;; I want a function!,
;; that function should be in sync with the code!
;; I want 2 systems!, do you really though? would 2 singletons be ok?
;; adapters should be singletons (have 2 singletons if you want to use 2 ports)
;; start and stop is good, only having 1 is good
;; Function must either be called with the context that lives on, or be a singleton, or never change
;; Can pass resources in the request, or depend on them externally
