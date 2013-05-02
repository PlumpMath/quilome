(ns spinner.process
  (:require (spinner [paint :as p]
                     [midi-out :as m])))

(defn- offset-thumb-info [{encoders :encoders} enc offset]
  "Divide encoder setting by two: 0..127(ish) across 64 LEDs."
  "Result is encoder stack pos * animation offset * thumb-width."
  (let [{vs :value-stack idx :selected-idx} (nth encoders enc)]
    {:i idx
     :o offset
     :t (+ (/ (nth vs idx) 2) 1)}))

(defn dyn-process [state r offset midi-state]
  "Offset is cosmetic, for animation."
  (do
    (m/dyn-ctrl-out state midi-state)       ; MUST ONLY be done when max/maxObject is bound
                                        ; (so we're OK here - beat clock/transport has come in)
    (p/dyn-paint r (map (fn [i] (offset-thumb-info state i offset)) (range 4)))))
