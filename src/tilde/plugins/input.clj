(ns tilde.plugins.input
  (:require [clojure.string :as str]
            [tilde.fs :as fs]))

(defprotocol InputPlugin
  "Input plugins must implement the following methods:
  - `plugin-config`        returns a map with the plugin's configuration
  - `plugin-name`          returns a string with the plugin's name
  - `sources`              returns a seq of the sources this plugin cares about
  - `latest-source-update` (must be FAST!) returns a timestamp of the last time this source was changed
  - `read-source`          reads source and returns its EDN representation
  - `rebuild`              reads the input source and returns a new config map
  - `get-snippet`          returns snippet contents if plugin exports one by that name (optional)"
  (plugin-config [this])
  (plugin-name [this])
  (sources [this])
  (latest-source-update [this source])
  (read-source [this source-file])
  (rebuild [this source-key])
  (get-snippet [this snippet-name]))

(defn sources->map
  [plugin sources]
  (reduce (fn [sm s]
            (let [contents   (delay (read-source plugin s))
                  source-key (-> s
                                 .getName
                                 (str/replace-first
                                  (str "." (fs/filename-extension s)) "")
                                 keyword)]
              (merge-with merge sm
                          {source-key {:contents      contents
                                       :last-modified (.lastModified s)}})))
          {} sources))
