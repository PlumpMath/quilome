(ns spinner.midi-out)

(def CTRL-BASES [16 80])                ; For two layers, ctrl numbers 16.. and 80..
(def LAYER-BASE 20)                     ; Layer switch: ctrl numbers 20..

(defn- put [state ctrl val]
  (if
      (not= (get state ctrl 0) val)
    (.outletHigh max/maxObject 1 [val ctrl])))

(defn- maybe-out [midi-state {val :value-stack idx :selected-idx} i]
  (let
      [prev-state @midi-state
       base-for-selected (nth CTRL-BASES idx)   ; 16 or 80, depending on stack selection.
       enc-ctrl (+ i base-for-selected)             ; actual controller number.
       new-enc-val (nth val idx)                    ; incoming value.
       layer-ctrl (+ i LAYER-BASE)
       new-layer-val (if (pos? idx) 127 0)
       next-state (assoc prev-state
                    enc-ctrl new-enc-val
                    layer-ctrl new-layer-val)]
    (reset! midi-state next-state)      ; First, in case MIDI is fed back in this thread.
    (dotimes
        [i 4]
      (put prev-state enc-ctrl new-enc-val)
      (put prev-state layer-ctrl new-layer-val))))

(defn dyn-ctrl-out [state midi-state]
  (let [encoders (:encoders state)]
    (dotimes
        [i 4]
      (maybe-out midi-state (nth encoders i) i))))
