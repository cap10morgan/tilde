(ns tilde.log
  (:require [clojure.string :as str]))

(defn log-level
  []
  (or (-> "LOG_LEVEL" System/getenv keyword) :warn))

(def log-levels
  [:debug :info :warn :error])

(defn level-active?
  [level]
  (let [level-index  (.indexOf log-levels level)
        active-index (.indexOf log-levels (log-level))]
    (when (and (nat-int? active-index) (nat-int? level-index))
      (>= level-index active-index))))

(defn log
  [level & msgs]
  (when (level-active? level)
    (println (str (-> level name str/upper-case) ": " (str/join " " msgs)))))

(def debug (partial log :debug))
(def info (partial log :info))
(def warn (partial log :warn))
(defn error
  [& args]
  (if (instance? Throwable (first args))
    (apply log :error (conj (-> args rest vec) "-" (.getMessage (first args))))))
