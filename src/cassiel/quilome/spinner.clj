(ns cassiel.quilome.spinner
  "Main arc 'spin' application."
  (:require (cassiel.quilome [network :as net]
                             [connect :as c]
                             [tools :as t])
            (cassiel.quilome.spinner [manifest :as m]
                                     [types :as ty]
                                     [incoming :as in]))
  (:import (net.loadbang.shado ArcVariableOSCOutputter VariableRenderer)
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
              "/tick"
              (assoc state :midi
                     (in/flush-display renderer
                                       osc-tx
                                       (:device state)
                                       (:midi state)
                                       (nth args 0)))

              (do
                (println "other" address args)
                state)
              ))

          osc-rx (net/start-receiver in-port
                                     (fn [_ address args]
                                       (c/swap-state
                                        info
                                        #(incoming-osc % address args))))]

      (reify c/CONNECTION-CLIENT
        (get-initial-state [this] {:device (ty/initial-arc-state 4)
                                   :midi {}})

        (handle-enc-key [this state enc how]
          (assoc state :device
                 (in/do-uncover (in/do-press (:device state) enc how)
                                enc how)))

        (handle-enc-delta [this state enc delta]
          (assoc state :device (in/do-delta (:device state) enc delta)))

        (shutdown [this state]
          (.close osc-tx)
          (.close osc-rx))))))
