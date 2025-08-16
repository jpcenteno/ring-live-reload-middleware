(ns ring-live-reload-middleware.core
  (:require
   [clojure.spec.alpha :as s]
   [ring-live-reload-middleware.implementation.state :as state]
   [ring-live-reload-middleware.implementation.middleware.serve-script :as middleware.serve-script]
   [ring-live-reload-middleware.implementation.middleware.serve-channel :as middleware.serve-channel]
   [ring-live-reload-middleware.implementation.middleware.inject :as middleware.inject]))

(s/def ::*state ::state/*state)

(s/fdef start!
  :args (s/cat)
  :ret  ::*state
  :fn   #(state/empty-state? @(:ret %)))
(defn start! []
  (atom (state/->empty-state)))

(s/fdef stop!
  :args (s/cat :*state ::*state)
  :ret  ::*state
  :fn   (s/and #(state/empty-state? @(:ret %))))
(defn stop! [*state]
  (throw (Error. "Unimplemented `stop!`")))

(s/fdef reload-clients!
  :args (s/cat :*state ::*state)
  :ret  nil?)
(defn reload-clients! [*state]
  (throw (Error. "Unimplemented `broadcast-reload!`")))

(s/fdef wrap-live-reload
  :args (s/cat :handler ifn? :*state ::*state)
  :ret  ifn?)
(defn wrap-live-reload ; FIXME rename to `wrap`
  [handler *state]
  (-> handler
      middleware.inject/wrap-client ; FIXME rename to middleware.inject
      middleware.serve-script/wrap
      middleware.serve-channel/wrap))
