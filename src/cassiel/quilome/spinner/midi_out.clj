(ns cassiel.quilome.spinner.midi-out
  (:import (net.loadbang.osc.data Message)))

(def CTRL-BASES [16 20 24 28]) ; For four layers, ctrl numbers 16 upwards...
(def LAYER-BASE 80)            ; Layer switch: ctrl numbers 20..

(defn- put [midi-state tx ctrl val]
  (if
      (= (get midi-state ctrl 0) val)
    midi-state
    (do
      (.transmit tx (-> (Message. "/ctrl")
                        (.addInteger ctrl)
                        (.addInteger val)))
      (assoc midi-state ctrl val))))

(defn- maybe-out [tx midi-state {val :value-stack idx :selected-idx} i]
  (let
      [base-for-selected (nth CTRL-BASES idx)   ; 16 or 80, depending on stack selection.
       enc-ctrl (+ i base-for-selected)             ; actual controller number.
       enc-val (nth val idx)                    ; incoming value.
       layer-ctrl (+ i LAYER-BASE)
       layer-val (if (pos? idx) 127 0)      ; Not ideal once we have more than two layers!
       ]

    (-> midi-state
        (put tx layer-ctrl layer-val)
        (put tx enc-ctrl enc-val))))

(defn ctrl-out [tx device midi-state]
  (let [encoders (:encoders device)]
    (doall
     (reduce (fn [m i] (maybe-out tx m (nth encoders i) i))
             midi-state
             (range 4)))))
