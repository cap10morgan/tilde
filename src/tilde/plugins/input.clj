(ns tilde.plugins.input
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [tilde.fs :as fs]))

(defprotocol InputPlugin
  "Input plugins must implement the following methods:
  - `plugin-name`   returns a string with the plugin's name
  - `latest-update` (must be FAST!) returns a timestamp of the last time this source was changed
  - `rebuild`       reads the input source and returns a new config map."
  (plugin-name [this])
  (sources [this])
  (latest-source-update [this source-key])
  (rebuild [this source-key]))

(defn sources->map
  [sources]
  (reduce (fn [sm s]
            (let [contents   (delay (-> s slurp edn/read-string))
                  source-key (-> s
                                 .getName
                                 (str/replace-first
                                  (str "." (fs/filename-extension s)) "")
                                 keyword)]
              (merge-with merge sm
                          {source-key {:contents      contents
                                       :last-modified (.lastModified s)}})))
          {} sources))
