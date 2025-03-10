(ns tilde.main
  (:require [babashka.cli :as cli]
            [tilde.cfg :as cfg]
            [tilde.inputs :as inputs]
            [tilde.log :as log]
            [tilde.outputs :as outputs]
            [tilde.plugins.input :as input]
            [tilde.plugins.output :as output]))

(def cli-opts
  {:output-dir {:default (cfg/output-dir)}
   :source-dir {:default (cfg/source-dir)}})

(defn refresh-all
  [{:keys [output-dir]}]
  (let [input-plugins  (inputs/get-all-plugins)]
    (doseq [plugin input-plugins]
      (let [plugin-sources (input/sources plugin)]
        (log/debug "plugin" (input/plugin-name plugin) "found sources:"
                   (pr-str plugin-sources))
        (doseq [[source-key source] plugin-sources]
          (let [source-last-updated (input/latest-source-update plugin source)
                output-plugin (outputs/output-plugin-for source-key)]
            (when output-plugin
              (let [outputs-last-updated (output/latest-output-update
                                          output-plugin output-dir)]
                (log/debug "outputs last updated:" outputs-last-updated)
                (if (> source-last-updated outputs-last-updated)
                  (let [output (input/rebuild plugin source)]
                    (output/write! output-plugin output-dir output))
                  (log/debug "not regenerating" source-key
                             "because source is older than output"))))))))))

(defn -main
  [& args]
  (let [opts       (cli/parse-opts args {:spec cli-opts})
        output-dir (:output-dir opts)
        _          (log/info "tilde is rebuilding dotfiles in"
                             (.getPath output-dir) "...")]
    (refresh-all opts)))
