(ns cassiel.quilome.spinner
  "Main arc 'spin' application."
  (:require (cassiel.quilome [network :as net]
                             [connect :as c]
                             [tools :as t])
            (cassiel.quilome.spinner [manifest :as m]
                                     [types :as ty]
                                     [incoming :as in]
                                     [midi-out :as midi]))
  (:import (net.loadbang.shado ArcVariableOSCOutputter VariableRenderer Frame)
           (net.loadbang.osc.data Message)))

(defn spin
  "Main entry point, given `CONNECTION-INFO`, returning `CONNECTION-CLIENT`.

   Arguments here are for the OSC 'side-port' for communication with the media
   host."
  [& {:keys [in-port out-host out-port]}]

  (fn
    [info]

    (let [tx (c/get-transmitter info)
          renderer (VariableRenderer. (:monome-width m/MANIFEST)
                                      (:monome-height m/MANIFEST)
                                      (ArcVariableOSCOutputter. tx (c/get-prefix info)))

          osc-tx (net/start-transmitter out-host out-port)

          incoming-osc
          (fn [state address args]
            (case address
              "/tick"                   ; [0..127] - responsible for all MIDI output
              (let [tick (nth args 0)
                    {:keys [midi device]} state
                    midi' (midi/ctrl-out osc-tx device midi)]
                (in/flush-display renderer device tick)
                (assoc state :midi midi'))

              "/uncover"                ; [encoder-index] [how]
              (let [[enc how] args]
                (assoc state :device
                       (in/do-uncover (:device state) enc how)))

              "/set-layer"               ; [0..n-1]
              (let [layer (nth args 0)]
                (assoc state :device
                       (in/set-layer (:device state) layer)))

              (do
                (println "other" address args)
                state)))

          osc-rx (net/start-receiver in-port
                                     (fn [_ address args]
                                       (c/swap-state
                                        info
                                        #(incoming-osc % address args))))]

      (reify c/CONNECTION-CLIENT
        (get-initial-state [this] {:device (ty/initial-arc-state 4)
                                   :midi {}})

        (handle-grid-key [this state x y how]
          state)

        (handle-enc-key [this state enc how]
          (assoc state :device
                 (in/do-uncover (in/do-press (:device state) enc how)
                                enc how)))

        (handle-enc-delta [this state enc delta]
          (assoc state :device (in/do-delta (:device state) enc delta)))

        (shutdown [this state]
          (.render renderer (Frame.))
          (.close osc-tx)
          (.close osc-rx))))))
