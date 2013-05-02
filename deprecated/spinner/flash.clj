(ns spinner.flash
  (:require (spinner [renderer :as r]))
  (:import (net.loadbang.shado Frame Block))
  (:import (net.loadbang.shado.types LampState)))

(defn dyn-flash [r how]
  (if (zero? how)
    (.render r (Frame.))
    (let
        [block (Block. 64 4)]
      (do
        (doseq [y (range 4) x (range 64)]
          (.setLamp block x y (LampState. (mod (/ x 16) 1.0) 0.0)))
        (.render r block)))))
