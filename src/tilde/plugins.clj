(ns tilde.plugins
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [tilde.plugins.input.exec :as input-exec-plugin]
            [tilde.plugins.input.local-files :as input-files-plugin]
            [tilde.plugins.output.exec :as output-exec-plugin]))

(defn- get-plugin-cfg
  [path]
  (let [plugin-cfg-file (io/file path "tilde.edn")]
    (if (.exists plugin-cfg-file)
      (-> plugin-cfg-file slurp edn/read-string :tilde/plugin)
      {:type :input/files, :path "."})))

(defn load-plugin
  [path]
  (let [plugin-cfg (get-plugin-cfg path)]
    (case (:type plugin-cfg)
      :input/executable (->> plugin-cfg :executable (io/file path)
                             input-exec-plugin/new)
      :input/files (->> plugin-cfg :path (io/file path) input-files-plugin/new)
      :output/executable (->> plugin-cfg :executable (io/file path)
                              output-exec-plugin/new))))
