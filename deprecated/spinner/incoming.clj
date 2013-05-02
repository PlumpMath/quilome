(ns spinner.incoming
  (:require (spinner [types :as types]
                     [process :as p]
                     [manifest :as m]))
  (:import (spinner.types EncoderState ArcState)))

(defprotocol TARGET
  (press [this i how])                ; Button press: encoder#, 0/1
  (delta [this i distance])           ; Delta turn: encoder#, distance
  (uncover [this i])                 ; Uncover by "rotating" stack of encoders at i
  (layer [this pos])                    ; Switch all encoders to one layer
  )


(defn- enc-do-button [{v :value-stack
                       idx :selected-idx
                       _ :button} button]
  (EncoderState. v idx button))

(defn- enc-do-delta [{v :value-stack
                      idx :selected-idx
                      b :button} delta]
  (let
      [limited (max 0 (min 127 (+ (nth v idx) delta)))]
    (EncoderState. (assoc v idx limited) idx b)))

(defn- enc-do-uncover [{v :value-stack
                        idx :selected-idx
                        b :button}]
  (let
      [new-idx (mod (inc idx) (:encoder-stack-size m/MANIFEST))]
    (EncoderState. v new-idx b)))

(defn- enc-set-pos [{v :value-stack
                     idx :selected-idx
                     b :button} pos]
  (EncoderState. v pos b))

(defn- ch-button [{e :encoders} n b]
  (ArcState. (assoc e n (enc-do-button (nth e n) b))))

(defn- ch-delta [{e :encoders} n delta]
  (ArcState. (assoc e n (enc-do-delta (nth e n) delta))))

(defn- ch-uncover [{e :encoders} n]
  (ArcState. (assoc e n (enc-do-uncover (nth e n)))))

(defn- ch-layer [{e :encoders} pos]
  (ArcState. (vec (map #(enc-set-pos % pos) e))))

;; This is a really funny way of coding in Clojure. Why did I do this?

(deftype FnTarget
    []
  TARGET
  (press [this i how]
    "Return a function which changes a state to reflect this button press."
    (fn [state] (ch-button state i (nth [:off :on] how))))

  (delta [this i distance]
    "Return a function which changes a state to reflect this knob turn."
    (fn [state] (ch-delta state i distance)))

  (uncover [this i]
    "Uncover the next encoder state down."
    (fn [state] (ch-uncover state i)))

  (layer [this pos]
    "Set all to this layer."
    (fn [state] (ch-layer state pos))))

(def target (FnTarget.))

(def state (atom (types/initial-arc-state 4)))

(defn dyn-do-press
  [st i how]
  (swap! st (press target i how)))

(defn dyn-do-delta
  [st i distance]
  (swap! st (delta target i distance)))

(defn dyn-do-uncover
  [st i how]
  (if (not= how 0) (swap! st (uncover target i))))

(defn dyn-do-layer
  [st pos]
  (swap! st (layer target pos)))

(defn dyn-refresh [st r t midi-state]
  "Called from the incoming MIDI: the controller refresh is done via incoming metro."
  (p/dyn-process st r (mod t 128) midi-state))
