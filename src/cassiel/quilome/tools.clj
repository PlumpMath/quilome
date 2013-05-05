(ns cassiel.quilome.tools)

(defn do-after [ms f]
  (future (Thread/sleep ms)
          (f)))

(defn strip-prefix [s]
  (clojure.string/replace-first s #"/[^/]*" ""))
