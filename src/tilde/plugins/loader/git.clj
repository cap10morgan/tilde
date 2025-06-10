(ns tilde.plugins.loader.git
  (:require [babashka.process :as process]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [tilde.log :as log]
            [tilde.plugins.loader :as loader]
            [tilde.plugins :as plugins]))

(set! *warn-on-reflection* true)

(def remote-aliases
  {:github "git@github.com:"})

(defn- git-clone
  "Clones a git repository to a local directory."
  [git-url clone-dir]
  (log/debug "cloning git repo" git-url "into" clone-dir)
  (let [result @(process/process ["git" "clone" "--depth=1" "-b" "main"
                                  "--single-branch" git-url clone-dir]
                                 {:out :string :err :string})]
    (when-not (zero? (:exit result))
      (throw (ex-info (str "Failed to clone repository: " (:err result))
                      {:git-url git-url :clone-dir clone-dir})))))

(defn- git-pull
  "Pulls latest changes from a git repository."
  [repo-dir]
  (log/debug "pulling changes from git remote into" repo-dir)
  (let [result @(process/process ["git" "pull"]
                                 {:dir repo-dir :out :string :err :string})]
    (when-not (zero? (:exit result))
      (throw (ex-info (str "Failed to pull repository: " (:err result))
                      {:repo-dir repo-dir})))))

(defn- sync-repo!
  "Ensures the repository exists locally, cloning it if necessary."
  [git-url clone-dir]
  ;; TODO: Handle dirty working dir; commit, pull w/ rebase, & push changes probably?
  (let [repo-dir (io/file clone-dir)]
    (if (.exists repo-dir)
      (git-pull repo-dir)
      (git-clone git-url clone-dir))
    repo-dir))

(defn identifier->git-url
  [identifier]
  (let [[alias & url-rest] (str/split identifier #":")]
    (if (contains? remote-aliases alias)
      (str (get remote-aliases alias) url-rest)
      ;; TODO: Probably have to do something more complicated in this case
      identifier)))

(defn identifier->local-dir
  [identifier]
  ;; TODO: This is probably too simple
  (-> identifier (str/split #"/") last))

(defrecord PluginLoaderGit
  [clone-root identifier]
  loader/PluginLoader
  (load-plugin [this]
    (let [path (loader/plugin-local-path this)]
      (sync-repo! (identifier->git-url identifier) path)
      (plugins/load-plugin path)))
  (loader/plugin-local-path [_this]
    (-> identifier identifier->local-dir (->> (io/file clone-root)))))

(defn new
  [identifier clone-root]
  (PluginLoaderGit. clone-root identifier))
