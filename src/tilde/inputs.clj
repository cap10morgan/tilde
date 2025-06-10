(ns tilde.inputs
  (:require [clojure.java.io :as io]
            [tilde.plugins.input.exec :as exec-plugin]
            [tilde.plugins.input.local-files :as lf-plugin]
            [tilde.cfg :as cfg]))

(def builtin-plugins
  #{(lf-plugin/new cfg/source-dir)})

(defn get-user-plugins
  []
  (-> cfg/source-dir
      (io/file "plugins/input/")
      file-seq
      rest
      (->> (map exec-plugin/new))))

;; TODO: Consider other locations for plugins to live (e.g. system-wide)

(defn get-all-plugins
  []
  (-> builtin-plugins
      (into (get-user-plugins))))
