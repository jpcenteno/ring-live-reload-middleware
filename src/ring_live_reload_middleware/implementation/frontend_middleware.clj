(ns ring-live-reload-middleware.implementation.frontend-middleware
  (:require
   [clojure.string :as str]))

(defn- remove-content-type-parameters
  "Removes the parameter suffix from a Content-Type string.
  
  See: https://en.wikipedia.org/wiki/Media_type#Structure"
  [content-type-str]
  (first (str/split content-type-str #";")))

(defn- is-html?
  "Returns true if and only if the response is an HTML page."
  [response]
  (contains? #{"text/html" "application/xhtml+xml"}
             (remove-content-type-parameters (get-in response [:headers "Content-Type"]))))

(defn- inject-live-reload-script
  [response]
  (update response :body #(str % "<script></script>")))

(defn wrap-inject-live-reload-script
  "Ring middleware that injects the live-reload script into HTML responses."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (is-html? response)
        (inject-live-reload-script response)
        response))))
