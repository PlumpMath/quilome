;; This module isn't reusable because of the hard-wired counter.
;; (dyn-clock takes an offset instead.)

(ns spinner.clock
  (:require (spinner [incoming :as incoming])))

(def a (atom 0))

(defn dyn-clock [st r t midi-state]
  (incoming/dyn-refresh st r (mod t 128) midi-state))
