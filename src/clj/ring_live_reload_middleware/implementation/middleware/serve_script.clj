(ns ring-live-reload-middleware.implementation.middleware.serve-script)

(defn wrap [handler]
  ; FIXME move the script server middleware from the `inject` ns to here.
  handler)
