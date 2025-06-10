(ns tilde.plugins.output.exec
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tilde.log :as log]
            [tilde.plugins.output :as output]
            [tilde.plugins.exec :as exec]
            [tilde.plugins.shared :as shared]))

(defn cli-arg->generator
  [executable cli-arg]
  (fn [_plugin _output-dir cfg]
    ;; TODO: Figure out how to support returning a new config too
    ;; This assumes top-level currently
    [nil (shared/stdout-from-command executable [cli-arg] {:in (pr-str cfg)})]))

(defrecord OutputPluginExecutable
  [path]
  output/OutputPlugin
  (plugin-config [_this]
    (let [cfg (exec/load-config path)]
      (log/debug "output plugin loaded config:" cfg)
      cfg))
  (plugin-name [this] (-> this output/plugin-config :name))
  (output-files [this] (-> this output/plugin-config :files))
  (output-generators [this]
    (let [->gen (partial cli-arg->generator path)]
      (-> this output/plugin-config :generators (update-vals ->gen))))
  (output-preambles [this] (-> this output/plugin-config :preambles))
  (latest-output-update [this output-dir]
    (log/debug "output exec plugin" (str "'" output/plugin-name this "'")
               "getting latest update timestamp of all outputs")
    (output/latest-modified output-dir (-> this output/output-files vals)))
  (write! [this output-dir cfg]
    (log/debug "output plugin" (str "'" (output/plugin-name this) "'") "writing cfg:"
               (pr-str cfg))
    (let [top-level-output  (output/plugin-name this)
          potential-outputs (conj (vec (keys cfg)) top-level-output)
          _                 (log/debug "potential outputs:" (pr-str potential-outputs))
          output-gens       (select-keys (output/output-generators this)
                                         potential-outputs)
          _                 (log/debug "output generators:" (pr-str output-gens))
          active-outputs    (-> output-gens
                                keys
                                (->> (remove #(= % top-level-output)))
                                vec
                                (conj top-level-output))
          _                 (log/debug "active outputs:" active-outputs)
          output-pres       (select-keys (output/output-preambles this)
                                         (conj potential-outputs :tilde/all))
          output-contents   (loop [[k & ks] active-outputs
                                   cfg      cfg
                                   contents {}]
                              (let [preamble (or (get output-pres k)
                                                 (get output-pres :tilde/all))
                                    generate (get output-gens k)
                                    _        (log/debug "generating output from config:" (pr-str cfg))
                                    [cfg out] (generate this output-dir cfg)
                                    contents (assoc contents
                                               k (str (when preamble
                                                        (str preamble "\n"))
                                                      out))]
                                (if (seq ks)
                                  (recur ks cfg contents)
                                  contents)))]
      (doseq [[output-key filename] (output/output-files this)]
        (log/debug "writing" filename)
        (let [dest (str/join "/" [output-dir filename])]
          (io/make-parents dest)
          (spit dest (get output-contents output-key)))))))

(defn new
  [exec-path]
  (map->OutputPluginExecutable
   {:plugin-config (atom nil)
    :path          exec-path}))
