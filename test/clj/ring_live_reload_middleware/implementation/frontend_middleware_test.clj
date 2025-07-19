(ns ring-live-reload-middleware.implementation.frontend-middleware-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.implementation.frontend-middleware :as sut]
            [ring.util.response :as response]
            [clojure.string :as str]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Test data                                                              ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn- extend-content-types-collection
  "Takes a list of `<type>/<subtype>` strings and extends the collection adding
  different parameter combinations to each one of them.

  Note that a Content-Type string is formed by a `<type>/<subtype>` string and
  optional parameters sepparated by a semicolon."
  [types-and-subtypes]
  (for [prefix    types-and-subtypes
        parameter ["" "; charset=utf-8" "; charset=UTF-8" "; charset=ISO-8859-1"]]
    (str prefix parameter)))

; FIXME Re-include XHTML "application/xhtml+xml". I left it out because
; injecting code on XHTML is slightly more complex than including it on HTML5.

(def valid-html-content-types
  (extend-content-types-collection ["text/html"]))

(def non-html-content-types
  (extend-content-types-collection
   ["text/json"
    "application/json"
    "application/javascript"
    "text/plain"]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Test Helpers                                                           ║
; ╚═══════════════════════════════════════════════════════════════════════╝

(defn- blank-string? [x]
  (and (string? x) (str/blank? x)))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Unit tests                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(deftest is-html?-test

  (testing "Returns `true` given a response with a valid HTML content type string."
    (doseq [content-type valid-html-content-types]
      (testing (str "(" content-type ")")
        (is (#'sut/is-html? {:headers {"Content-Type" content-type}})))))

  (testing "Returns `false` given a response with a non-HTML content type string."
    (doseq [content-type non-html-content-types]
      (testing (str "(" content-type ")")
        (is (not (#'sut/is-html? {:headers {"Content-Type" content-type}}))))))

  ; FIXME Implement "MIME Sniffing" and replace this test.
  (testing "Returns false when Content-Type is nil"
    (is (not (#'sut/is-html? {})))))

(deftest serve-script-middleware-test
  (let [fallback-response (response/response "Hello")
        fallback-handler  (fn [_] fallback-response)
        handler           (sut/wrap-client fallback-handler)]

    (testing "when the :uri is not the script's path:"
      (let [response (handler {:uri "/some/random/path"})]
        (testing "It delegates the request to the handler"
          (is (= fallback-response response)))))

    (testing "When the :uri matches the script's path:"
      (let [response (handler {:uri sut/uri})]

        (testing "Returns a valid response map"
          (is (response/response? response)))

        (testing "Sets :status to 200"
          (is (= 200 (:status response))))

        (testing "Sets content type to JS"
          (is (= "application/javascript" (get-in response [:headers "Content-Type"]))))

        (testing "Response's body is neither `nil` nor `blank`."
          ; This test is intended to catch error states wehere the file is not
          ; being read correctly. The task of checking the contents of the script
          ; itself is responsibility of the e2e test suite.
          (let [body (:body response)]
            (is (some? body))
            (is (not (blank-string? body)))))))))
