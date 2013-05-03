(ns cassiel.quilome.connect
  (:require ( cassiel.quilome [network :as net]
                              [tools :as t]))
  (:import (net.loadbang.osc.data Message)))

(defn list-devices
  "Open a channel to list devices, calling `callback` with argument map of
   `:id`, `:name`, `:host`, `:port`."
  [& {:keys [host me port callback]}]
  (letfn [(dispatch
            [_ address [id name port]]
            (when (= address :serialosc/device)
              (callback :id id
                        :name name
                        :host host
                        :port port)))]
    (let [transmitter (net/start-transmitter host port)
          receiver (net/start-receiver dispatch)
          message (-> (Message. "/serialosc/list")
                      (.addString me)
                      (.addInteger (.getPort receiver)))]
      (.transmit transmitter message)
      (t/do-after 1000 (fn []
                         (.close transmitter)
                         (.close receiver))))))

(defn list-properties
  "Open a channel to list properties, calling `callback` with argument map of
   `:key`, `:value`. `:key` is keyword-converted. `:value` can be a vector
   (e.g. for `/sys/size`). I don't think `/sys/host` is very useful: as far as
   I know, it'll be the same as the host we're interrogating - or, at worst,
   incorrectly hardwired to `localhost`."
  [& {:keys [host me port callback]}]
  (letfn [(dispatch
            [_ address args]
            (callback :key address
                      :value args))
          (dispatchx [x y z] (callback [x y z]))]
    (let [transmitter (net/start-transmitter host port)
          receiver (net/start-receiver dispatch)
          message (-> (Message. "/sys/info")
                      (.addString me)
                      (.addInteger (.getPort receiver)))]
      (.transmit transmitter message)
      (t/do-after 1000 (fn []
                         (.close transmitter)
                         (.close receiver))))))

(defprotocol DEVICE-CONNECTION
  (get-transmitter [this] "Return the transmitter to the device.")
  (close-connection [this] "Close connection to device."))

(defn connect
  "Connect to device."
  [& {:keys [host me port prefix callback]}]
  (letfn [(dispatch
            [_ address args]
            (callback address args))]

    (let [transmitter (net/start-transmitter host port)
          receiver (net/start-receiver dispatch)
          port-message (-> (Message. "/sys/port")
                           (.addInteger (.getPort receiver)))
          prefix-message (-> (Message. "/sys/prefix")
                             (.addString prefix))]
      (.transmit transmitter port-message)
      (.transmit transmitter prefix-message)

      (reify DEVICE-CONNECTION
        (get-transmitter [this] transmitter)

        (close-connection
          [this]
          (.close transmitter)
          (.close receiver))))))

(defprotocol CONNECTION-CLIENT
  "A client matching this protocol must be provided for each device."
  (handle-message [this address args]
    "Handle OSC - address and args sanitised, prefix removed.")
  (shutdown [this] "Shut down any other connections or activity."))
