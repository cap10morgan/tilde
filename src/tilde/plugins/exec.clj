(ns tilde.plugins.exec
  (:require [clojure.java.io :as io]
            [tilde.log :as log]
            [tilde.plugins.shared :as shared]))

(defn load
  [executable args err-msg]
  (try
    (shared/read-edn-from-command executable args)
    (catch RuntimeException e
      (log/error e err-msg))))

(defn load-sources
  [executable]
  (load executable ["--sources"]
        (str "Could not load sources from exec plugin " executable)))

(defn- load-config
  [executable]
  (load executable ["--config"]
        (str "Could not read config from exec plugin " executable)))

(def config
  (memoize load-config))

(def sources
  (memoize load-sources))

(defn get-plugin-config
  [plugin]
  (config (.-path plugin)))

(defn get-plugin-sources
  [plugin]
  (sources (.-path plugin)))
