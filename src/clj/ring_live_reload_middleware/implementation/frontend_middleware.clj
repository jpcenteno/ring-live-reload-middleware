(ns ring-live-reload-middleware.implementation.frontend-middleware
  "This namespace provides a Ring middleware that:
    - Injects the client-side _live reload_ script into HTML responses.
    - Intercepts and serves requests for the client-side _live reload_ script.
  
  ## Implementation notes

  TODO"
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [ring.util.response :as response]))

; Resource path to the client-side JS script.
(def ^:private script-resource-path "public/main.js")

(def uri "/ring-live-reload-client.js")

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ HTML script injection                                                  ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn- remove-content-type-parameters
  "Removes the parameter suffix from a Content-Type string.
  
  See: https://en.wikipedia.org/wiki/Media_type#Structure"
  [content-type-str]
  (first (str/split content-type-str #";")))

(defn- is-html?
  "Returns true if and only if the response is an HTML page."
  [response]
  (let [content-type (get-in response [:headers "Content-Type"])]
    (and (some? content-type)
         (contains? #{"text/html" "application/xhtml+xml"} (remove-content-type-parameters content-type)))))

(defn- inject-live-reload-script
  [response]
  (update response :body #(str % (format "<script src=\"%s\"></script>" uri))))

(defn- wrap-inject-live-reload-script
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (is-html? response)
        (inject-live-reload-script response)
        response))))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Client script server                                                   ║
; ╚════════════════════════════════════════════════════════════════════════╝

(s/fdef handle-serve-client-script
  :args nil?
  :ret  response/response?)

(defn- handle-serve-client-script
  []
  (-> script-resource-path
      response/resource-response
      (response/content-type "application/javascript")))

(s/fdef wrap-serve-client-script
  :args (s/cat :handler fn?)
  :ret  fn?)

(defn- wrap-serve-client-script
  "Ring middleware that intercepts HTTP requests for the client live reload
  script and serves it's JS source code. Delegates every other request to the
  `handler` passed as parameter."
  [handler]
  (fn [request]
    (if (= uri (:uri request))
      (handle-serve-client-script)
      (handler request))))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Public API                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn wrap-client
  "Ring middleware that injects the live-reload script into HTML responses."
  [handler]
  (-> handler
      wrap-serve-client-script
      wrap-inject-live-reload-script))
