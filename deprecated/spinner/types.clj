(ns spinner.types
  (:require (spinner [manifest :as m])))

;; An encoder has a value (unbound integer as a counter) and button (:off, :on).
;; CHANGE: we now have a stack of values, and a pointer to the current one.

(defrecord EncoderState
    [value-stack selected-idx button])

(defrecord ArcState
    [encoders])

(defn initial-arc-state [n]
  (ArcState. (vec (repeat n (EncoderState. (vec (repeat (:encoder-stack-size m/MANIFEST) 0))
                                           0
                                           :off)))))
