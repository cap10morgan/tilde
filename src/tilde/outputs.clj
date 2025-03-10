(ns tilde.outputs
  (:require [clojure.java.io :as io]
            [tilde.cfg :as cfg]
            [tilde.log :as log]
            [tilde.plugins.output :as output]
            [tilde.plugins.output.git :as git-plugin]
            [tilde.plugins.output.exec :as exec-plugin]))

(def builtin-output-plugins
  #{(git-plugin/new)})

(defn exec-plugin->output-plugin
  [config-dir path]
  (exec-plugin/map->OutputPluginExecutable
   {:config-dir config-dir
    :path       path}))

(defn get-user-output-plugins
  []
  (-> (cfg/source-dir)
      (io/file "plugins/output/")
      file-seq
      rest
      (->> (map exec-plugin->output-plugin))))

(defn get-all-plugins
  []
  (into builtin-output-plugins (get-user-output-plugins)))

(defn output-plugin-for
  [source]
  (let [plugins (get-all-plugins)
        plugin (some #(when (= source (output/plugin-name %)) %) plugins)]
    (if plugin plugin (log/warn "unable to find output plugin for" source))))
