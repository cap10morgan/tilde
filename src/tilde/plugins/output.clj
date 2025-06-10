(ns tilde.plugins.output
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tilde.log :as log])
  (:import (java.io File)))

(defn latest-modified
  [dir files]
  (let [xf (comp
            (map #(conj [dir] %))
            (map #(str/join "/" %))
            (map io/file)
            (map (fn [f] (log/debug "checking modified timestamp of" f) f))
            (map File/.lastModified))]
    (transduce xf max 0 files)))

(defprotocol OutputPlugin
  "Output plugins must implement a `write!` fn that writes the updated file(s)
  based on the `cfg` config map. It MUST do this atomically."
  (plugin-config [this])
  (plugin-name [this])
  (output-files [this])
  (output-generators [this])
  (output-preambles [this])
  (latest-output-update [this output-dir])
  (write! [this dest-dir cfg]))
