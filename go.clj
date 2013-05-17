(ns user
  (:require (cassiel.quilome [spinner :as sp]
                             [flatscreen :as scr]
                             [network :as net]
                             [connect :as c]
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
                                                  :out-port 9109)})]
    (q/sketch :title "Music Tech Fest"
              :setup (fn [] (q/frame-rate 1))
              :draw (fn [])
              :on-close (fn [] (c/shutdown-all all)))))
