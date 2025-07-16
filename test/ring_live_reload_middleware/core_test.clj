(ns ring-live-reload-middleware.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.core :as sut]
            [clojure.string :as str]
            [ring-live-reload-middleware.implementation.frontend-middleware-test :as frontend-middleware-test]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Test responses                                                         ║
; ╚════════════════════════════════════════════════════════════════════════╝

(def plaintext-responses
  (for [content-type frontend-middleware-test/non-html-content-types]
    {:status  200
     :headers {"Content-Type" content-type}
     :body    "Hello"}))

;; A collection of different formats of valid (ish) html responses.
(def html-responses
  (for [content-type frontend-middleware-test/valid-html-content-types
        body-meta    [{:description "well-formed HTML file"
                       :body        "<!DOCTYPE html><html><head></head><body>Hello HTML</body></html>"}
                      {:description "HTML file without a <head> tag"
                       :body        "<!DOCTYPE html><html><body>Hello HTML</body></html>"}
                      {:description "HTML file without a <body> tag"
                       :body        "<!DOCTYPE html><html><head></head></html>"}
                      {:description "File without any HTML tags at all"
                       :body        "Hello"}]]

    {:status      200
     :headers     {"Content-Type" content-type}
     :body        (:body body-meta)
     :description (format "%s with Content-Type `%s`" (:description body-meta) content-type)}))

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
  (testing "When wrapping a handler that returns a plaintext response"
    (doseq [response plaintext-responses
            :let
            [content-type (get-in response [:headers "Content-Type"])]]
      (testing (format  "With `Content-Type` set to `%s`:" content-type)
        (let [handler (-> (fn [_request] response)
                          (sut/wrap-live-reload :fixme-reloader))]
          (testing "The middleware does nothing."
            (is (= response (handler :not-a-request-but-it-should-not-matter))))))))

  ;; Repeat the same tests for different forms of HTML responses that should be
  ;; supported.
  (doseq [response html-responses]
    (testing (str "When wrapping a handler that returns a " (:description response) ":")
      (let [handler  (sut/wrap-live-reload (fn [_request] response) :fixme-reloader)
            response (handler :not-a-request-but-it-should-not-matter-here)]
        (testing "The middleware injects a <script> tag"
          (is (includes-a-script? response)))))))
