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

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Unit tests                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(deftest wrap-test
  (let [fallback-response (response/response "Hello")
        fallback-handler  (fn [_] fallback-response)
        handler           (sut/wrap fallback-handler)]

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
            ; FIXME Remove helper function and use str/blank?
            (is (not (blank-string? body)))))))))
