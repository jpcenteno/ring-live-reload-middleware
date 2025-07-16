(ns ring-live-reload-middleware.core
  (:require [ring-live-reload-middleware.implementation.frontend-middleware :as frontend-middleware]))

(defn start! []
  (throw (Error. "Unimplemented `start!`")))

(defn stop! [*reloader]
  (throw (Error. "Unimplemented `stop!`")))

(defn broadcast-reload! [*reloader]
  (throw (Error. "Unimplemented `broadcast-reload!`")))

(defn wrap-live-reload
  [handler *reloader]
  (-> handler
      frontend-middleware/wrap-inject-live-reload-script))
