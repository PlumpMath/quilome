(ns cassiel.quilome.spinner.incoming
  (:require (cassiel.quilome.spinner [types :as types]
                                     [flash :as f]
                                     [manifest :as m]))
  (:import (cassiel.quilome.spinner.types EncoderState ArcState)))

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

(defn do-uncover [state enc how]
  (println "do-uncover" enc how)
  (if (zero? how)
    state
    (ch-uncover state enc)))

(defn do-press [state renderer enc how]
  (println "do-press" enc how)
  (f/dyn-flash renderer how)
  (ch-button state enc (nth [:off :on] how)))

(defn do-delta [state enc distance]
  (println "do-delta" enc distance)
  (ch-delta state enc distance))
