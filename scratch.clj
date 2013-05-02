(ns user
  (:require (cassiel.quilome [network :as net]
                             [connect :as c]))
  (:import (net.loadbang.osc.data Message)))


(def A (atom {}))

(defn dispatch-fn [origin address args]
  (reset! A {:origin origin :address address :args args}))

(def receiver (net/start-receiver dispatch-fn))

(.getPort receiver)


(def transmitter (net/start-transmitter "localhost" 12002))

(def m (-> (Message. "/serialosc/list")
           (.addString "localhost")
           (.addInteger (.getPort receiver))))

(.transmit transmitter m)

(deref A)

(reset! A {})

(swap! A assoc :C 44)

(def ll
  (c/device-lister :host "localhost"
                   :me "localhost"
                   :port 12002
                   :callback (fn [& {:keys [id name host port]}]
                               (swap! A assoc id [name host port]))))

(c/get-devices ll)

(c/close-devices ll)

(def B (atom {}))

(def pp
  (c/property-listener :host "localhost"
                       :me "localhost"
                       :port 10279
                       :callback (fn [& {:keys [key value]}]
                                   (swap! B assoc key value))))

(c/get-properties pp)

(c/close-properties pp)

(deref B)

(reset! B {})

:serialosc-device

(def C (atom []))

(def cc (c/connect :host "localhost"
                   :me "localhost"
                   :port 10279
                   :prefix "/babble"
                   :callback (fn [address args]
                               (swap! C conj [address args]))))

;; (cc/get-transmitter cc)

(c/close-connection cc)

(deref C)
(reset! C [])
