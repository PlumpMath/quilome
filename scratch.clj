(ns user
  (:require (cassiel.quilome [spinner :as sp]
                             [network :as net]
                             [connect :as c]
                             [tools :as t]))
  (:import (net.loadbang.osc.data Message)))


(def ff (fn [] (println "A")
          (println "B")))

(ff)

(def a
  (let [_a (atom {})]
    (c/list-devices :host CLIENT
                    :me ME
                    :port 12002
                    :callback (fn [& {:keys [id name host port]}]
                                (swap! _a assoc id [name host port])))
    _a))

(deref a)

(def b
  (let [_b (atom {})]
    (c/list-properties :host CLIENT
                       :me ME
                       :port 15332
                       :callback (fn [& {:keys [key value]}]
                                   (swap! _b assoc key value)))
    _b))

(deref b)

:serialosc-device

(def C (atom []))

(def cc (c/connect :host CLIENT
                   :me ME
                   :port 15332
                   :prefix "/babble"
                   :callback (fn [address args]
                               (swap! C conj [address args]))))

(c/get-transmitter cc)

(c/close-connection cc)

(deref C)
(reset! C [])

;; Scan for all properties:

(def b
  (let [_b (atom {})]
    (c/list-devices :host CLIENT
                    :me ME
                    :port 12002
                    :callback
                    (fn [& {:keys [id name host port]}]
                      (c/list-properties :host CLIENT
                                         :me ME
                                         :port port
                                         :callback
                                         (fn [& {:keys [key value]}]
                                           (swap! _b assoc-in [id key] value)))))
    _b))

(deref b)

(def all
  (c/connect-all CLIENT ME
                 {"monome arc 4"
                  (fn [info]
                    (reify c/CONNECTION-CLIENT
                      (get-initial-state [this] nil)
                      (handle-message [this _ address args]
                        (println address)
                        (println args))

                      (shutdown [this _] nil)))}))

;; Running on MacBook:

(def all
  (c/connect-all :host "kazlicesme-wired.lan"
                 :me "sultanahmet.lan"
                 :handlers {"monome arc 4"
                            (sp/spin :in-port 9104
                                     :out-host "localhost"
                                     :out-port 9105)
                            "monome arc 2"
                            (sp/spin :in-port 9106
                                     :out-host "localhost"
                                     :out-port 9107)}))

;; Running on netbook:

(def all
  (c/connect-all :host "localhost"
                 :me "localhost"
                 :handlers {"monome arc 4"
                            (sp/spin :in-port 9104
                                     :out-host "sultanahmet.lan"
                                     :out-port 9105)
                            "monome arc 2"
                            (sp/spin :in-port 9106
                                     :out-host "sultanahmet.lan"
                                     :out-port 9107)}))


(c/get-state all)

(c/shutdown-all all)
