(ns user
  (:require (cassiel.quilome [spinner :as sp]
                             [flatscreen :as scr]
                             [network :as net]
                             [connect :as c]
                             [draw :as d]
                             [tools :as t])
            (cassiel.quilome.spinner [types :as ty]
                                     [incoming :as in])
            (quil [core :as q]))
  (:import (net.loadbang.osc.data Message)))


(def ff (fn [] (println "A")
          (println "B")))

(ff)

(def CLIENT "localhost")
(def ME "localhost")

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
                       :port 11016
                       :callback (fn [& {:keys [key value]}]
                                   (swap! _b assoc key value)))
    _b))

(deref b)

:serialosc-device

(def C (atom []))

(def cc (c/connect :host CLIENT
                   :me ME
                   :port 10279
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


(defn system [& {:keys [monomes handlers media]}]
  (c/connect-all :host monomes
                 :me handlers
                 :handlers {"monome arc 4"
                            (sp/spin :in-port 9104
                                     :out-host media
                                     :out-port 9105)
                            "monome arc 2"
                            (sp/spin :in-port 9106
                                     :out-host media
                                     :out-port 9107)
                            "m128-183"
                            (scr/screen :in-port 9108
                                        :out-host media
                                        :out-port 9109)}))

;; Running on MacBook:

(def all (system :monomes "kazlicesme-wired.lan"
                 :handlers "sultanahmet.lan"
                 :media "localhost"))

;; Running on Netbook:

(def all (system :monomes "localhost"
                 :handlers "localhost"
                 :media "sultanahmet.lan"))


;; *** Local

(def all (system :monomes "localhost"
                 :handlers "localhost"
                 :media "localhost"))

(c/get-state  all)

( (:handler-state (get (c/get-state  all) "m128-183")))


(c/get-state all)

(c/shutdown-all all)

;; Quil-based, codebase on MacBook:

(def s
  (let [all (system :monomes "kazlicesme-wired.lan"
                    :handlers "sultanahmet.lan"
                    :media "localhost")]
    (q/sketch :title "Sultanahmet"
              :setup (fn [])
              :draw (fn [])
              :on-close (fn [] (c/shutdown-all all)))))

;; Quil-based, Netbook

(def s
  (let [all (system :monomes "localhost"
                    :handlers "localhost"
                    :media "sultanahmet.lan")]
    (q/sketch :title "Netbook"
              :setup (fn [] (q/frame-rate 10))
              :draw (fn [])
              :on-close (fn [] (c/shutdown-all all)))))

;; ---- Quil-based, all local, inlined:

(def s
  (let [all (c/connect-all :host "localhost"
                           :me "localhost"
                           :handlers {"monome arc 4"
                                      (sp/spin :in-port 9104
                                               :out-host "localhost"
                                               :out-port 9105)
                                      "monome arc 2"
                                      (sp/spin :in-port 9106
                                               :out-host "localhost"
                                               :out-port 9107)
                                      "m128-183"
                                      (scr/screen :in-port 9108
                                                  :out-host "localhost"
                                                  :out-port 9109)})]
    (q/sketch :title "Localhost"
              :setup (fn []
                       (q/frame-rate 25)
                       (q/smooth)
                       (q/stroke-weight 5)
                       (q/stroke 127 63 0 255)
                       (q/fill 255 127 0 30))
              :size [1000 1000]
              :draw (fn []
                      (d/do-draw (:points ( (-> (c/get-state all)
                                                (get "m128-183")
                                                (:handler-state)
                                                ))))
                      )
              :on-close (fn [] (c/shutdown-all all)))))

;; ---- END.

(q/sketch-close s)

(def start {:device (ty/initial-arc-state 4)
            :midi {}})

start

(in/do-uncover (:device start) 0 1)

(midi/dyn-ctrl-out )

(doall {16 1 17 3})

(int (/ 70 32))

(map #(+ 16 (* % 4)) (range 8))


(get {"A" 1 "B" 2} "A")

(min 3 4)
