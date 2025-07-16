(ns ring-live-reload-middleware.implementation.frontend-middleware-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.implementation.frontend-middleware :as sut]))

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

(def valid-html-content-types
  (extend-content-types-collection ["text/html" "application/xhtml+xml"]))

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
        (is (not (#'sut/is-html? {:headers {"Content-Type" content-type}})))))))
