(ns ring-live-reload-middleware.implementation.middleware.inject
  "This namespace provides a Ring middleware that injects the client-side _live
  reload_ script into HTML responses."
  (:require [clojure.string :as str]
            [ring-live-reload-middleware.implementation.middleware.serve-script :as middleware.serve-script]))

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
  (update response :body
          #(str % (format "<script src=\"%s\"></script>"
                          middleware.serve-script/uri))))

(defn- wrap-inject-live-reload-script
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (is-html? response)
        (inject-live-reload-script response)
        response))))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Public API                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn wrap-client
  "Ring middleware that injects the live-reload script into HTML responses."
  [handler]
  (-> handler
      wrap-inject-live-reload-script))
