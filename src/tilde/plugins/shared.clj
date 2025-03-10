(ns tilde.plugins.shared)

(defprotocol CachedPluginConfig
  "CachedPlugin implementers must implement the `get-plugin` method which should
  load the plugin if it isn't already, but cache it and return that on subsequent
  calls."
  (plugin-config [this]))
