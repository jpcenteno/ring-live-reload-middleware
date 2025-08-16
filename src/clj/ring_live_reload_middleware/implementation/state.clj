(ns ring-live-reload-middleware.implementation.state
  (:require [clojure.spec.alpha :as s]))

(defn- atom? [x]
  (instance? clojure.lang.Atom x))

(defn- atom-of [spec]
  (s/and atom? #(s/valid? spec (deref %))))

(defn- client? [_x]
  true) ; FIXME

(s/def ::clients (s/coll-of client?))
(s/def ::state   (s/keys :req [::clients]))
(s/def ::*state  (atom-of ::state))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Observers                                                              ║
; ╚════════════════════════════════════════════════════════════════════════╝

(s/fdef empty-state?
  :args (s/cat :state ::state)
  :ret  boolean?)
(defn empty-state? [state]
  (empty? (::clients state)))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Constructors                                                           ║
; ╚════════════════════════════════════════════════════════════════════════╝

(s/fdef ->empty-state
  :args (s/cat)
  :ret  (s/and ::state empty-state?))

(defn ->empty-state
  "Returns a blank instance of `::state`."
  []
  {::clients #{}})
