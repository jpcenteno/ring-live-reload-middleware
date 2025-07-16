(ns ring-live-reload-middleware.e2e-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-live-reload-middleware.core :as sut]
            [etaoin.api :as etaoin]
            [ring.adapter.jetty :as jetty]))

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
      (fn [_request] {:status  200
                      :headers {"Content-Type" "text/plain"}
                      :body    "Hello"})

      :callback
      (fn [url]
        (testing "The driver can:"
          (etaoin/with-chrome-headless driver
            (testing "Visit the server's URL."
              (is (etaoin/go driver url)))
            (testing "read the server's message."
              (is (etaoin/has-text? driver "Hello")))))))))
