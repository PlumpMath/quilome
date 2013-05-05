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
  [in-port out-host out-port]

  (fn
    [info]

    (let [tx (c/get-transmitter info)
          renderer (VariableRenderer. (:monome-width m/MANIFEST)
                                      (:monome-height m/MANIFEST)
                                      (ArcVariableOSCOutputter. tx (c/get-prefix info)))
          osc-tx (net/start-transmitter out-host out-port)
          osc-rx (net/start-receiver in-port
                                     (fn [origin address args]
                                       (.transmit osc-tx (Message. "/got-it"))))]
      (reify c/CONNECTION-CLIENT
        (get-initial-state [this] {:encoders (ty/initial-arc-state 4)})

        (handle-message [this state address args]
          (case address
            "/enc/key" (let [[enc how] args]
                         (assoc state :encoders
                                (in/do-uncover (in/do-press (:encoders state) renderer enc how)
                                               enc how)))

            "/enc/delta" (let [[enc delta] args]
                           (.transmit osc-tx
                                      (-> (Message. "/delta")
                                          (.addInteger delta)))
                           (assoc state :encoders (in/do-delta (:encoders state) enc delta)))

            (do
              (println address args)
              state)))

        (shutdown [this state]
          (.close osc-tx)
          (.close osc-rx))))))
