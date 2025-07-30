(ns ring-live-reload-middleware.implementation.websocket-server-middleware-test
  (:require [clojure.test :refer [are deftest is testing]]
            [ring.util.response :as response]
            [ring.websocket :as ws]
            [ring-live-reload-middleware.implementation.websocket-server-middleware :as sut]
            [ring-live-reload-middleware.utils.test.ring :as utils.ring]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Helpers: Misc                                                          ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn spy
  "Wraps the function `f` allowing the user to inspect the parameters it
  received.
  
  NOTE: The wrapped function can only be called once."
  [f]
  (let [*args (promise)
        g     (fn [& args]
                (deliver *args args)
                (apply f args))]
    [g *args]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Unit tests                                                             ║
; ╚════════════════════════════════════════════════════════════════════════╝

(deftest can-handle?-test

  (testing "Can't handle"
    (are [request] (not (#'sut/can-handle? request))
      {:uri "/some/uri"}
      (utils.ring/map->upgrade-request {:uri "/some/uri"})
      {:uri sut/uri}))

  (testing "Can handle"
    (is (#'sut/can-handle? (utils.ring/map->upgrade-request {:uri sut/uri})))))

(deftest wrap-test

  (testing "Delegates to the handler when it can't handle a request by itself"
    (are [request]
         (let [expected-response (response/response "ok")
               [handler *args]   (spy (constantly expected-response))
               wrapped-handler   (sut/wrap handler)
               actual-response   (wrapped-handler request)]
           (is (= [request] (deref *args)))
           (is (= expected-response actual-response)))

      {:uri "/some/uri"}
      (utils.ring/map->upgrade-request {:uri "/some/uri"})
      {:uri sut/uri}))

  (testing "Given an upgrade request for the WS URI:"
    (let [request          (utils.ring/map->upgrade-request {:uri sut/uri})
          fallback-handler (fn [_]
                             ; The middleware should never call this function
                             ; when provided with a `request` that it can handle
                             ; itself. This throw is in place to break the tests
                             ; in case the execution reaches this function.
                             (throw (UnsupportedOperationException. "Unreachable")))
          handler          (sut/wrap fallback-handler)
          response         (handler request)]

      (testing "Returns a websocket response"
        (is (ws/websocket-response? response))))))
