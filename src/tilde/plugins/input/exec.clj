(ns tilde.plugins.input.exec
  (:require [tilde.log :as log]
            [tilde.plugins.exec :as exec]
            [tilde.plugins.input :as input]
            [tilde.plugins.shared :as shared]))

(defrecord InputPluginExecutable
  [path]
  input/InputPlugin
  ;; TODO: Cache this at some point if it's a good perf boost
  (plugin-config [_this] (exec/load-config path))
  (plugin-name [this] (-> this input/plugin-config :name))
  ;; TODO: Rethink `sources` for exec plugins
  (sources [this] ())
  (latest-source-update [this source]
    (log/debug "input plugin" (str "'" (input/plugin-name this) "'")
               "getting latest update timestamp for:" (pr-str source))
    (:last-modified source))
  (read-source [this source-file] ())
  (rebuild [this source]
    (log/debug "input plugin" (str "'" (input/plugin-name this) "'") "rebuilding:"
               (pr-str source))
    nil) ; TODO: Write me
  (get-snippet [_this snippet-name]
    (shared/stdout-from-command path ["--snippet" snippet-name])))

(defn new
  [path]
  (map->InputPluginExecutable
   {:path path}))
