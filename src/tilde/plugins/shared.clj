(ns tilde.plugins.shared
  (:require [babashka.process :as process]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [tilde.log :as log])
  (:import (java.io BufferedReader InputStreamReader PushbackReader)))

(defn process
  [executable args]
  (apply process/process executable args))

(defn read-edn-from-command
  [executable args & [opts]]
  (log/debug "reading EDN from" executable (str/join " " args))
  (let [result @(apply process/process (merge opts {:out :string})
                       executable args)]
    (if (zero? (:exit result))
      (edn/read-string {:default *default-data-reader-fn*} (:out result))
      (log/error "Could not read EDN from" (str/join (cons executable args))
                 "output was:" (:out result))))
  #_(-> executable (process args) :out (InputStreamReader.) (BufferedReader.)
        (PushbackReader.) (->> (edn/read {:default *default-data-reader-fn*}))))

(defn stdout-from-command
  [executable args & [opts]]
  (let [{:keys [exit out err]} @(apply process/process
                                       (merge opts {:out :string})
                                       executable args)]
    (if (zero? exit)
      out
      (log/error "Could not read stdout from" (str/join (cons executable args))
                 "exit status was:" exit "\n" err))))
