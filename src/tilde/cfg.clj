(ns tilde.cfg
  (:require [clojure.java.io :as io]))

(defn user-cfg-dir
  []
  (io/file (or (System/getenv "XDG_CONFIG_HOME")
               (str (System/getProperty "user.home") "/.config"))))

(defn source-dir
  []
  (io/file (user-cfg-dir) "tilde"))

(def output-dir user-cfg-dir)
