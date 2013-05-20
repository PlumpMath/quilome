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

(def gig
  (let [host "localhost"
        all (c/connect-all :host host
                           :me host
                           :handlers {"monome arc 4"
                                      (sp/spin :in-port 9104
                                               :out-host host
                                               :out-port 9105)
                                      "monome arc 2"
                                      (sp/spin :in-port 9106
                                               :out-host host
                                               :out-port 9107)
                                      "m128-183"
                                      (scr/screen :in-port 9108
                                                  :out-host host
                                                  :out-port 9109)})
        draw (fn []
               (let [m128 (get (c/get-state all) "m128-183")
                     points (if m128
                              (:points ((:handler-state m128)))
                              (map (partial * q/PI 0.125) (range 8)))]
                 (d/do-draw points)))]
    (q/sketch :title "Music Tech Fest"
              :size [1400 1000]
              :setup (fn []
                       (q/frame-rate 25)
                       (q/smooth)
                       (q/stroke-weight 5)
                       (q/stroke 127 95 63 255)
                       (q/fill 255 127 0 50))
              :draw draw
              :on-close (fn [] (c/shutdown-all all)))))
