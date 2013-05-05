(ns cassiel.quilome.spinner.midi-out
  (:import (net.loadbang.osc.data Message)))

(def CTRL-BASES [16 80])                ; For two layers, ctrl numbers 16.. and 80..
(def LAYER-BASE 20)                     ; Layer switch: ctrl numbers 20..

(defn- put [tx state ctrl val]
  (if
      (not= (get state ctrl 0) val)
    (.transmit tx (-> (Message. "/ctrl")
                      (.addInteger ctrl)
                      (.addInteger val)))))

(defn- maybe-out [tx midi-state {val :value-stack idx :selected-idx} i]
  (let
      [base-for-selected (nth CTRL-BASES idx)   ; 16 or 80, depending on stack selection.
       enc-ctrl (+ i base-for-selected)             ; actual controller number.
       new-enc-val (nth val idx)                    ; incoming value.
       layer-ctrl (+ i LAYER-BASE)
       new-layer-val (if (pos? idx) 127 0)
       next-state (assoc midi-state
                    enc-ctrl new-enc-val
                    layer-ctrl new-layer-val)]

    (dotimes
        [i 4]
      (put tx midi-state enc-ctrl new-enc-val)
      (put tx midi-state layer-ctrl new-layer-val))

    next-state))

(defn dyn-ctrl-out [tx device midi-state]
  (let [encoders (:encoders device)]
    (reduce (fn [m i] (maybe-out tx m (nth encoders i) i))
            midi-state
            (range 4))))
