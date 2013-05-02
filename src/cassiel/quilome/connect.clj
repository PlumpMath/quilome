(ns cassiel.quilome.connect
  (:require [cassiel.quilome [network :as net]])
  (:import (net.loadbang.osc.data Message)))

(defn list-devices
  "Open a channel and list devices, calling `callback` with argument map of
   `:id`, `:name`, `:host`, `:port`."
  [host port callback]
  (letfn [(dispatch
            [{host :host} address [id name port]]
            (when (= address :serialosc/device)
              (callback :id id
                        :name name
                        :host host
                        :port port)))]
    (let [transmitter (net/start-transmitter host port)
          receiver (net/start-receiver dispatch)
          message (-> (Message. "/serialosc/list")
                      (.addString "localhost")
                      (.addInteger (.getPort receiver)))]
      (.transmit transmitter message)
      ;; TODO: close transmitter and receiver.
      )))
