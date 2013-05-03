(ns cassiel.quilome.tools)

(defn do-after [ms f]
  (future (Thread/sleep ms)
          (f)))
