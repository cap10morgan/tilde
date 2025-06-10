(ns tilde.main
  (:require [babashka.cli :as cli]
            [clojure.string :as str]
            [tilde.cfg :as cfg]
            [tilde.inputs :as inputs]
            [tilde.log :as log]
            [tilde.outputs :as outputs]
            [tilde.plugins.input :as input]
            [tilde.plugins.loader :as loader]
            [tilde.plugins.output :as output]
            [tilde.plugins.loader.git :as git]))

(defonce output-dir (atom nil))

(def cli-opts
  {:output-dir {:default (cfg/output-dir)}})

(defn default-data-reader
  [tag val]
  (log/debug "default-data-reader tag:" (pr-str tag) "val:" (pr-str val))
  (let [tag (str tag)]
    (cond
      (str/includes? tag "github") ; TODO: Need a better way to detect this
      (let [loader (git/new tag cfg/git-clone-dir)
            plugin (loader/load-plugin loader)]
        (log/debug "loaded plugin:" (pr-str plugin))
        ;; TODO: Need to replace this with some kind of load-resource instead
        ;; not necessarily an input plugin and not necessarily a snippet
        (input/get-snippet plugin val)))))

(defn refresh-all
  [{:keys [output-dir]}]
  (let [input-plugins (inputs/get-all-plugins)]
    (doseq [plugin input-plugins]
      (let [plugin-sources (input/sources plugin)]
        (log/debug "plugin" (input/plugin-name plugin) "found sources:"
                   (pr-str plugin-sources))
        (doseq [[source-key source] plugin-sources]
          (let [source-last-updated (input/latest-source-update plugin source)
                output-plugin       (outputs/output-plugin-for source-key)]
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
  (let [opts (cli/parse-opts args {:spec cli-opts})]
    (reset! output-dir (:output-dir opts))
    (log/info "tilde is refreshing dotfiles in" @output-dir)
    (binding [*default-data-reader-fn* default-data-reader]
      (refresh-all opts))))
