(ns tilde.plugins.input.exec
  (:require [tilde.plugins.input :as input]
            [tilde.plugins.exec :as exec-plugin]
            [tilde.shared :as shared]))

(defrecord InputPluginExecutable
  [source-dir path plugin-config]
  input/InputPlugin
  (plugin-name [this] (-> this plugin-config :name))
  (latest-source-update [this source-key]
    (println "input plugin" (str "'" (input/plugin-name this) "'")
             "getting latest update timestamp for:" (pr-str source-key))
    true) ; TODO: Write me
  (rebuild [this source]
    (println "input plugin" (str "'" (input/plugin-name this) "'") "rebuilding:"
             (pr-str source))
    nil) ; TODO: Write me
  shared/CachedPluginConfig
  (plugin-config [this] (exec-plugin/get-plugin-config this)))

(defn path->input-plugin
  [source-dir path]
  (map->InputPluginExecutable
   {:source-dir    source-dir
    :path          path
    :sources-atom  (atom nil)
    :plugin-config (atom nil)}))
