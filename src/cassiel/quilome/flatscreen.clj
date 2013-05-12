(ns cassiel.quilome.flatscreen
  "Main grid display."
  (:require (cassiel.quilome [network :as net]
                             [connect :as c]
                             [tools :as t])
            (cassiel.quilome.flatscreen [manifest :as m]))
  (:import (net.loadbang.shado Frame Block ViewPort
                               SerialOSCBinaryOutputter BinaryRenderer)
           (net.loadbang.shado.types LampState)
           (net.loadbang.osc.data Message)))

(defn screen
  "Main entry point, given `CONNECTION-INFO`, returning `CONNECTION-CLIENT`.

   Arguments here are for the OSC 'side-port' for communication with the media
   host. (Currently we only do input.)"
  [& {:keys [in-port out-host out-port]}]

  (fn
    [info]

    (let [tx (c/get-transmitter info)
          width (:monome-width m/MANIFEST)
          renderer (BinaryRenderer. width
                                    (:monome-height m/MANIFEST)
                                    (SerialOSCBinaryOutputter. tx width (c/get-prefix info)))

          osc-tx (net/start-transmitter out-host out-port)

          incoming-osc
          (fn [state address args]
            (case address
              "/shard"
              ;; ["A"/"B"] f0 f1 f2 f3, where the fs are notionally between 0.0 and 1.0, but
              ;; not limited/normalised.
              (let [[label & fs] args
                    row0 (get {"A" 0 "B" 4} label)
                    ;; Update the state with the appropriate chunk of new values:
                    state' (t/replace-segment state row0 fs nil)
                    assemble (fn [frame row val]
                               (let [val' (mod val 1)
                                     index (int (+ 0.5 (* val' 15)))]
                                 (if (> val' 0.01)
                                   (.add frame (Block. "1") index row)
                                   frame)))]
                (.render renderer
                         (first
                          (reduce (fn [[frame row] val] [(assemble frame row val) (inc row)])
                                  [(Frame.) 0]
                                  state')))
               state')

              "/flash"                   ; [0/1]
              (do
                (.render renderer
                         (if (pos? (nth args 0))
                           (.fill (Block. 16 8) LampState/ON)
                           (Frame.)))
                state)

              "/position"               ; [0.0..1.0]
              (let [pos (nth args 0)
                    index (int (+ 0.5 (* pos 15)))]
                (.render renderer (-> (Frame.)
                                      (.add (Block. "1") index 0)))
                state)

              (do
                (println "other" address args)
                state)))

          osc-rx (net/start-receiver in-port
                                     (fn [_ address args]
                                       (c/swap-state
                                        info
                                        #(incoming-osc % address args))))]

      (reify c/CONNECTION-CLIENT
        ;; Initial state is eight raw positions (nominally 0.0..1.0).
        (get-initial-state [this] (repeat 8 0.0))

        (handle-grid-key [this state x y how]
          state)

        (handle-enc-key [this state enc how]
          state)

        (handle-enc-delta [this state enc delta]
          state)

        (shutdown [this state]
          (.render renderer (Frame.))
          (.close osc-tx)
          (.close osc-rx))))))
