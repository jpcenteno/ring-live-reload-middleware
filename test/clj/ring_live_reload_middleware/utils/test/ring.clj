(ns ring-live-reload-middleware.utils.test.ring
  (:require [clojure.spec.alpha :as s]
            [ring.adapter.jetty :as jetty]
            [ring.websocket     :as ws]))

(defn- random-port []
  (+ 8000 (rand-int 1000)))

(defn with-server [& {:keys [handler callback]}]
  (let [port   (random-port)
        server (jetty/run-jetty handler {:port port :join? false})
        url    (str "http://localhost:" port)]
    (try (callback url)
         (finally (.stop server)))))

(s/fdef map->upgrade-request
  :args (s/cat :request map?)
  :ret  ws/upgrade-request?)
(defn map->upgrade-request
  "Takes a Ring `request` and adds the headers required to pass
  `ws/upgrade-request?`."
  [m]
  (-> m
      (assoc-in [:headers "connection"] "Upgrade")
      (assoc-in [:headers "upgrade"] "websocket")))
