(ns ring-live-reload-middleware.implementation.websocket-server-middleware
  "Implements a notification server"
  (:require [clojure.spec.alpha :as s]
            [ring.websocket :as ws]))

(def uri "/live-reload-notifications")

(s/fdef websocket-handler
  :ret ws/websocket-response?)
(defn- websocket-handler
  [_request]
  {::ws/listener {}})

(defn- can-handle?
  [request]
  (and (ws/upgrade-request? request)
       (= uri (:uri request))))

(defn wrap
  [handler]
  (fn [request]
    (if (can-handle? request)
      (websocket-handler request)
      (handler request))))
