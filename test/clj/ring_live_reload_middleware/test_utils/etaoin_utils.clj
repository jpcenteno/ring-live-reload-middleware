(ns ring-live-reload-middleware.test-utils.etaoin-utils
  (:require [clojure.string :as str]
            [etaoin.api :as etaoin]))

; ╔════════════════════════════════════════════════════════════════════════╗
; ║ Log helpers                                                            ║
; ╚════════════════════════════════════════════════════════════════════════╝

(defn- clean-console-log-message-str
  [s]
  (let [[_ _ _ message] (re-matches #"^(\S+)\s+(\S+)\s+\"(.*)\"$" s)]
    (clojure.string/replace message #"\\\"" "\"")))

(defn- clean-console-log-message
  [log-map]
  (update log-map :message clean-console-log-message-str))

(defn console-log?
  [log-map]
  ; JS `console.log` messages have `:console-api` for `:source`. (See docs for
  ; `etaoin/get-logs`)
  (= :console-api (:source log-map)))

(defn get-console-logs
  [driver]
  (->> (etaoin/get-logs driver)
       (filter console-log?)
       (map clean-console-log-message)))

(defn get-console-log-strings
  [driver]
  (map :message (get-console-logs driver)))

(defn logged-message?
  "Returns true if and only if there is a JS console log emitted by the
  live-reload script that matches the message.
  
  NOTE: This function clears the logs because it calls `etaoin/get-logs`."
  [driver message]
  (contains? (set (get-console-log-strings driver)) message))
