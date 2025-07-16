(ns ring-live-reload-middleware.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.core :as sut]
            [clojure.string :as str]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Test responses                                                         ║
; ╚════════════════════════════════════════════════════════════════════════╝

(def plaintext-response
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello"})

;; A collection of different formats of valid (ish) html responses.
(def html-responses
  (for [content-type ["text/html" "text/html; charset=utf-8"]
        m      [{:description "well-formed HTML file"
                 :body        "<!DOCTYPE html><html><head></head><body>Hello HTML</body></html>"}
                {:description "HTML file without a <head> tag"
                 :body        "<!DOCTYPE html><html><body>Hello HTML</body></html>"}
                {:description "HTML file without a <body> tag"
                 :body        "<!DOCTYPE html><html><head></head></html>"}
                {:description "File without any HTML tags at all"
                 :body        "Hello"}]]

    {:status      200
     :headers     {"Content-Type" content-type}
     :body        (:body m)
     :description (format "%s with Content-Type `%s`" (:description m) content-type)}))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Test helpers                                                           ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn- includes-a-script?
  "Asserts that the response includes a <script> tag."
  [response]
  ; FIXME Use a selector library to write something more robust than
  ; this substring-based test.
  (and (str/includes? (:body response) "<script")
       (str/includes? (:body response) "</script>")))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Unit tests                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(deftest wrap-live-reload-test
  (testing "When the handler returns a plaintext response:"
    (let [handler (-> (fn [_request] plaintext-response)
                      (sut/wrap-live-reload :fixme-reloader))]
      (testing "The middleware does nothing:"
        (is (= plaintext-response (handler :not-a-request-but-it-should-not-matter))))))

  ;; Repeat the same tests for different forms of HTML responses that should be
  ;; supported.
  (doseq [response html-responses]
    (testing (str "When wrapping a handler that returns a " (:description response) ":")
      (let [handler  (sut/wrap-live-reload (fn [_request] response) :fixme-reloader)
            response (handler :not-a-request-but-it-should-not-matter-here)]
        (testing "The middleware injects a <script> tag"
          (is (includes-a-script? response)))))))
