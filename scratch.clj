(ns user
  (:require (cassiel.quilome [network :as net]
                             [connect :as c]))
  (:import (net.loadbang.osc.data Message)))


(def ff (fn [] (println "A")
          (println "B")))

(ff)


(def A (atom {}))

(deref A)

(reset! A {})


(c/list-devices :host "localhost"
                :me "localhost"
                :port 12002
                :callback (fn [& {:keys [id name host port]}]
                            (swap! A assoc id [name host port])))

(def B (atom {}))

(c/list-properties :host "localhost"
                   :me "localhost"
                   :port 10279
                   :callback (fn [& {:keys [key value]}]
                               (swap! B assoc key value)))

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
