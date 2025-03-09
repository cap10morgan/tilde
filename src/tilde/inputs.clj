(ns tilde.inputs
  (:require [clojure.java.io :as io]
            [tilde.plugins.input.edn :as edn-plugin]
            [tilde.plugins.input.exec :as exec-plugin]
            [tilde.cfg :as cfg]))

(def builtin-plugins
  #{(edn-plugin/new (cfg/source-dir))})

(defn get-user-plugins
  []
  (let [cfg-dir        (cfg/source-dir)
        ->input-plugin (partial exec-plugin/path->input-plugin cfg/source-dir)]
    (-> cfg-dir
        (io/file "plugins/input/")
        file-seq
        rest
        (->> (map ->input-plugin)))))

;; TODO: Consider other locations for plugins to live (e.g. system-wide)

(defn get-all-plugins
  []
  (into builtin-plugins (get-user-plugins)))
