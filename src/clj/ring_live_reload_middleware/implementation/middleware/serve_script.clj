(ns ring-live-reload-middleware.implementation.middleware.serve-script
  (:require [clojure.spec.alpha :as s]

            [ring.util.response :as response]))

(def uri "/ring-live-reload-client.js")

; Resource path to the client-side JS script.
(def ^:private script-resource-path "public/main.js")

(s/fdef handle-serve-client-script
  :args nil?
  :ret  response/response?)

(defn- handle-serve-client-script
  []
  (-> script-resource-path
      response/resource-response
      (response/content-type "application/javascript")))

(s/fdef wrap
  :args (s/cat :handler fn?)
  :ret  fn?)

(defn wrap
  "Ring middleware that intercepts HTTP requests for the client live reload
  script and serves it's JS source code. Delegates every other request to the
  `handler` passed as parameter."
  [handler]
  (fn [request]
    (if (= uri (:uri request))
      (handle-serve-client-script)
      (handler request))))
