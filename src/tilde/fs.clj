(ns tilde.fs)

(defn filename-extension
  [file]
  (let [filename (.getName file)
        idx      (.lastIndexOf filename ".")]
    (.substring filename (inc idx))))
