(ns ring-live-reload-middleware.implementation.middleware.serve-script-test
  (:require [ring-live-reload-middleware.implementation.middleware.serve-script :as sut]
            [clojure.test :refer [deftest is testing]]

            [clojure.string :as str]

            [ring.util.response :as response]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Test Helpers                                                           ║
; ╚═══════════════════════════════════════════════════════════════════════╝

(defn- blank-string? [x]
  (and (string? x) (str/blank? x)))

(defmulti not-blank? class)

(defmethod not-blank? String [s]
  (not (str/blank? s)))

(defmethod not-blank? java.io.File
  [file]
  (not-blank? (slurp file)))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Unit tests                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(deftest wrap-delegates-test
  (let [*arg     (promise)
        expected (response/response "Hello")
        handler  (sut/wrap (fn [x]
                             (deliver *arg x)
                             expected))
        request  {:uri "/some/random/uri"}
        response (handler request)]

    (testing "When the request `:uri` is not the script URI:"
      (testing "It passes the request to the fallback handler."
        (is (= request @*arg)))

      (testing "Returns the handler's response without changes."
        (is (= expected response))))))

(deftest wrap-serve-script-test
  (let [request  {:uri sut/uri}
        handler  (sut/wrap #(throw (UnsupportedOperationException. "Unimplemented!")))
        response (handler request)]

    (testing "When the request `:uri` matches the scirpt URI:"

      (testing "Returns a valid Ring response"
        (is (response/response? response)))

      (testing "Returns a response with status 200"
        (is (= 200 (:status response))))

      (testing "Returns a response with content-type set to JS"
        (is (= "application/javascript" (get-in response [:headers "Content-Type"]))))

      (testing "Body is non-empty"
        (is (not-blank? (:body response)))))))
