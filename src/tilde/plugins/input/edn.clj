(ns tilde.plugins.input.edn
  (:require [tilde.log :as log]
            [tilde.fs :as fs]
            [tilde.plugins.input :as input]))

(defrecord InputPluginEdn
  [name source-dir sources]
  input/InputPlugin
  (plugin-name [this] (.-name this))
  (sources [this]
    (if-let [s @(.-sources-atom this)]
      s
      (let [s (->> source-dir
                   file-seq
                   (filter #(= (fs/filename-extension %) "edn")))]
        (reset! (.-sources-atom this) (input/sources->map s)))))
  (latest-source-update [_this source] (:last-modified source))
  (rebuild [this source]
    (log/debug "input plugin" (str "'" (input/plugin-name this) "'") "rebuilding:"
             (pr-str source))
    (-> source :contents deref)))

(defn new
  [source-dir]
  (map->InputPluginEdn
   {:name         :edn
    :source-dir   source-dir
    :sources-atom (atom nil)}))
