(ns tilde.plugins.output.exec
  (:require [tilde.plugins.output :as output]
            [tilde.plugins.exec :as exec]
            [tilde.plugins.shared :as shared]))

(defrecord OutputPluginExecutable
  [path]
  output/OutputPlugin
  (plugin-name [this] (-> this shared/plugin-config :name))
  (write! [this dest-dir output-key cfg]
    (println "output plugin" (str "'" (output/plugin-name this) "'") "writing cfg:"
             (pr-str cfg))
    nil) ; TODO: Write me
  shared/CachedPluginConfig
  (plugin-config [this] (exec/get-plugin-config this)))
