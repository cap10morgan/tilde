(ns tilde.plugins.loader)

(defprotocol PluginLoader
  "PluginLoader retrieves plugins from their sources (e.g. git repo) and
  caches them locally on the filesystem."
  (load-plugin [this] "load plugin to local filesystem and do any necessary initialization; returns the plugin")
  (plugin-local-path [this] "returns the local filesystem path where the plugin is cached"))
