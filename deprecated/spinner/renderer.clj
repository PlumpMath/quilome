(ns spinner.renderer
  (:require (spinner [manifest :as m]))
  (:import (net.loadbang.shado ArcVariableOSCOutputter VariableRenderer)))

(def renderer (VariableRenderer. (:monome-width m/MANIFEST)
                                 (:monome-height m/MANIFEST)
                                 (ArcVariableOSCOutputter. (:monome-host m/MANIFEST)
                                                           (:monome-port m/MANIFEST)
                                                           (:monome-prefix m/MANIFEST))))

(defn dyn-renderer
  [transmitter prefix]
  (VariableRenderer. (:monome-width m/MANIFEST)
                                 (:monome-height m/MANIFEST)
                                 (ArcVariableOSCOutputter. transmitter prefix)))
