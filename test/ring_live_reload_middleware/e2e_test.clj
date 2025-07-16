(ns ring-live-reload-middleware.e2e-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.core :as sut]
            [etaoin.api :as etaoin]
            [ring.adapter.jetty :as jetty]
            [ring-live-reload-middleware.core-test :as core-test]))

(def plaintext-response
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello"})

(defn with-server [& {:keys [handler callback]}]
  (let [port   (+ 8000 (rand-int 1000))
        server (jetty/run-jetty handler {:port port :join? false})
        url    (str "http://localhost:" port)]
    (try (callback url)
         (finally (.stop server)))))

;; Simple test to check that the e2e test libraries and helpers work.
(deftest simple-e2e-test
  (testing "Given a plaintext server:"
    (with-server
      :handler
      (fn [_request] plaintext-response)

      :callback
      (fn [url]
        (testing "The driver can:"
          (etaoin/with-chrome-headless driver
            (testing "Visit the server's URL."
              (is (etaoin/go driver url)))
            (testing "read the server's message."
              (is (= "Hello" (etaoin/get-element-text driver {:tag :body}))))))))))

(deftest script-injection-test
  (testing "Given a plaintext response"
    (with-server
      :handler
      (-> (fn [_request] plaintext-response)
          (sut/wrap-live-reload :fixme-reloader))

      :callback
      (fn [url]
        (etaoin/with-chrome-headless driver
          (etaoin/go driver url)
          (testing "The middleware does not inject any script to the response"
            ; Etaoin will return the plaintext response wrapped in some HTML
            ; boilerplate. It will also escape any script that we inject.
            ; Getting the text content of the `<body>` tag should return the
            ; original plaintext.
            ;
            ; I was able to break this test by appending a `<script>` element to
            ; the plaintext response. The purpose of this test is to ensure that
            ; we are not modifying non-HTML responses.
            (is (= (:body plaintext-response)
                   (etaoin/get-element-text driver {:tag :body}))))))))

  (doseq [response core-test/html-responses]
    (testing (str  "When wrapping a server that responds a " (:description response))
      (with-server
        :handler
        (-> (fn [_request] response)
            (sut/wrap-live-reload :fixme-reloader))

        :callback
        (fn [url]
          (etaoin/with-chrome-headless driver
            (etaoin/go driver url)

            (testing "The middleware injects a <script> tag"
              (clojure.pprint/pprint {:response response
                                      :src      (etaoin/get-source driver)})
              (is (etaoin/exists? driver {:tag "script"})))))))))
