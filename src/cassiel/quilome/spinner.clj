(ns cassiel.quilome.spinner
  "Main arc 'spin' application."
  (:require (cassiel.quilome [connect :as c]
                             [tools :as t])
            (cassiel.quilome.spinner [manifest :as m]
                                     [types :as ty]
                                     [incoming :as in]))
  (:import (net.loadbang.shado ArcVariableOSCOutputter VariableRenderer)))

(defn spin
  "Main entry point, given `CONNECTION-INFO`, returning `CONNECTION-CLIENT`."
  [info]

  (let [tx (c/get-transmitter info)
        renderer (VariableRenderer. (:monome-width m/MANIFEST)
                                    (:monome-height m/MANIFEST)
                                    (ArcVariableOSCOutputter. tx (c/get-prefix info)))]
    (reify c/CONNECTION-CLIENT
      (get-initial-state [this] (ty/initial-arc-state 4))

      (handle-message [this state address args]
        (case address
          "/enc/key" (let [[enc how] args]
                       (in/do-uncover (in/do-press state renderer enc how) enc how))

          "/enc/delta" (let [[enc delta] args]
                         (in/do-delta state enc delta))

          (do
            (println address args)
            state)))

      (shutdown [this state] nil)))

  )
