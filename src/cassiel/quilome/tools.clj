(ns cassiel.quilome.tools)

(defn do-after [ms f]
  (future (Thread/sleep ms)
          (f)))

(defn strip-prefix [s]
  (clojure.string/replace-first s #"/[^/]*" ""))

(defn replace-segment
  "Traverse a source list, replacing a chunk of it with a replacement list segment."
  [source start replacement result]
  (cond
   (empty? source)
   (reverse result)

   (empty? replacement)
   (recur (next source)
          0
          nil
          (cons (first source) result))

   (pos? start)
   (recur (next source)
          (dec start)
          replacement
          (cons (first source) result))

   :else
   (recur (next source)
          0
          (next replacement)
          (cons (first replacement) result))))
