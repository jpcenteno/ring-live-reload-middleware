(ns ring-live-reload-middleware.implementation.middleware.inject-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.implementation.middleware.inject :as sut]
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
