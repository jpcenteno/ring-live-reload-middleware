(ns ring-live-reload-middleware.implementation.state-test
  (:require [ring-live-reload-middleware.implementation.state :as sut]
            [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]))

(deftest atom-of-test

  (testing "Non-atoms are invalid"
    (let [spec (#'sut/atom-of any?)]
      (is (not (s/valid? spec :some-value)))))

  (testing "Value must be an atom"
    (let [spec (#'sut/atom-of any?)]
      (is (s/valid? spec (atom :some-value)))))

  (testing "Atom's value must conform to spec"
    (let [spec (#'sut/atom-of keyword?)]

      (testing "Conforming value"
        (is (s/valid? spec (atom :some-keyword))))

      (testing "Non-conforming value"
        (is (not (s/valid? spec (atom "Not a keyword"))))))))
