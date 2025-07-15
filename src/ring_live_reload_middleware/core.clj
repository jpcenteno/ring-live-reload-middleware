(ns ring-live-reload-middleware.core)

(defn start! []
  (throw (Error. "Unimplemented `start!`")))

(defn stop! [*reloader]
  (throw (Error. "Unimplemented `stop!`")))

(defn broadcast-reload! [*reloader]
  (throw (Error. "Unimplemented `broadcast-reload!`")))

(defn wrap-live-reload
  [handler *reloader]
  (throw (Error. "Unimplemented `wrap-live-reload`")))
