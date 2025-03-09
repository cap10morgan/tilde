(ns tilde.plugins.exec
  (:require [babashka.process :as process]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn load-exec-plugin-config
  [executable]
  (-> @(process/process {:out :string} executable "--config")
      :out str/split-lines first edn/read))

(defn get-plugin-config
  [plugin]
  (if-let [cfg @(.-plugin-config plugin)]
    cfg
    (let [cfg (load-exec-plugin-config (io/file (.-config-dir plugin)
                                                (.-path plugin)))]
      (reset! (.-plugin-config plugin) cfg))))
