(ns user
  (:require (cassiel.quilome [network :as net]
                             [connect :as c]))
  (:import (net.loadbang.osc.data Message)))


(def A (atom nil))

(defn dispatch-fn [origin address args]
  (reset! A {:origin origin :address address :args args}))

(def receiver (net/start-receiver dispatch-fn))

(.getPort receiver)


(def transmitter (net/start-transmitter "localhost" 12002))

(def m (-> (Message. "/serialosc/list")
           (.addString "localhost")
           (.addInteger (.getPort receiver))))

(.transmit transmitter m)

@A

(reset! A nil)

:serialosc/device

(defn ff [& {:keys [a b]}] {:A a :B b})

(ff :a 1 :b 3)



(c/list-devices "localhost" 12002 (fn [& args] (reset! A args)))
