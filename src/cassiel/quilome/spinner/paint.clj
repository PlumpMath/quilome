(ns cassiel.quilome.spinner.paint
  (:import (net.loadbang.shado Frame Block ViewPort))
  (:import (net.loadbang.shado.types LampState)))

(defn repeat-block [b width]
  "Create a frame which repeats b, of a given width, to make a strip 128 cells wide."
  (let
      [times (/ 128 width)
       f (Frame.)]
    (do
      (doseq [i (range times)]
        (let
            [local-frame (.add (Frame.) b 0 0)]
          (.add f local-frame (* width i) 0))
        )
      f)))

(def strip-16
  (let
      [block (Block. 16 1)]
    (do
      (doseq [y (range 1) x (range 16)]
        (.setLamp block x y (LampState. (mod (/ x 16) 1.0) 0.0)))
      block)))

(def strip-8
  (let
      [block (Block. 8 1)]
    (do
      (doseq [y (range 1) x (range 8)]
        (.setLamp block x y (LampState. (mod (/ x 8) 1.0) 0.0)))
      block)))

(defn quiet-n [n]
  (let
      [block (Block. n 1)]
    (do
      (doseq [x (range (dec n))] (.setLamp block x 0 (LampState. 0.25 0.0)))
      block)))

;; A set of 128-wide strips with some gradiated wallpaper (a slideshow that fits into 64 LEDs).

(def all-offs [(repeat-block (Block. "1") 64)
               (repeat-block (Block. "1.1") 64)
               (repeat-block (Block. "111") 64)
               (repeat-block (Block. "11.11") 64)])

;; A set of gradiated strips for the thumb.

(def all-ons [(repeat-block strip-16 16)
              (repeat-block (quiet-n 2) 4)
              (repeat-block strip-8 8)
              (repeat-block (quiet-n 4) 6)])

(defn- paint-into-frame [f enc idx pos thumb-width]
  "pos from 0 to 63, as per LEDs. Thumb-width is the amount to actually show in a window."
  (let
      [pos63 (mod pos 64)              ; wrap to LED ring
       vp (ViewPort. (nth all-ons idx) pos63 0 thumb-width 4)
       f2 (Frame.)]
    (do
      (.add f2 (nth all-offs idx) (- 0 pos63) enc)
      (.add f vp (- 0 pos63) enc)
      (.add f f2 0 0))))

(defn dyn-paint [r pos-thumb-infos]
  (let
      [f (Frame.)]
    (do
      (doseq [enc (range 4)]
        (let [params (nth pos-thumb-infos enc)
              idx (:i params)        ; Index (stack position) for encoder.
              pos (:o params)        ; Animation position.
              thumb (:t params)]     ; Width of thumb (i.e. MIDI value).
          (paint-into-frame f enc idx pos thumb)))
      (.render r f))))
