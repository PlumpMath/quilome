(ns cassiel.quilome.draw
  (:require (quil [core :as q])))

(defn do-draw [settings]
  (q/background 0)
  (doseq [i (range (count settings))]
    (let [bearing1 (* q/PI 2.0 (nth settings i))
          bearing2 (+ bearing1 q/PI)
          spread (* q/PI 0.25)
          gap (* (min (q/width) (q/height))
                 0.12)
          j (inc i)]

      (q/arc (* (q/width) 0.5) (* (q/height) 0.5) (* j gap) (* j gap) (- bearing1 spread) (+ bearing1 spread))
      (q/arc (* (q/width) 0.5) (* (q/height) 0.5) (* j gap) (* j gap) (- bearing2 spread) (+ bearing2 spread)))))
