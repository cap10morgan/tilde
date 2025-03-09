(ns tilde.plugins.output)

(defprotocol OutputPlugin
  "Output plugins must implement a `write!` fn that writes the updated file(s)
  based on the `cfg` config map. It MUST do this atomically."
  (plugin-name [this])
  (output-files [this])
  (output-generators [this])
  (output-preambles [this])
  (latest-output-update [this output-dir])
  (write! [this dest-dir cfg]))
