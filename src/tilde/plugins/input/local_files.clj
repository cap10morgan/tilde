(ns tilde.plugins.input.local-files
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [tilde.log :as log]
            [tilde.fs :as fs]
            [tilde.plugins.input :as input]))

(defrecord InputPluginLocalFiles
  [name source-dir sources]
  input/InputPlugin
  (plugin-name [this] (.-name this))
  (sources [this]
    (if-let [s @(.-sources-atom this)]
      s
      (let [s (->> source-dir
                   file-seq
                   (filter #(= (fs/filename-extension %) "edn")))]
        (reset! (.-sources-atom this) (input/sources->map this s)))))
  (latest-source-update [_this source] (:last-modified source))
  (read-source [_this source-file] (->> source-file slurp
                                        (edn/read-string
                                         {:default *default-data-reader-fn*})))
  (rebuild [this source]
    (log/debug "input plugin" (str "'" (input/plugin-name this) "'") "rebuilding:"
             (pr-str source))
    (-> source :contents deref))
  (get-snippet [_this snippet-name]
    (slurp (io/file source-dir (str snippet-name ".snippet")))))

(defn new
  [source-dir]
  (map->InputPluginLocalFiles
   {:name         :local-files
    :source-dir   source-dir
    :sources-atom (atom nil)}))
