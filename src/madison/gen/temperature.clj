(ns madison.gen.temperature
  (:require [clojure.test.check.generators :as gen]))

(def scale (gen/elements [:C :F :K]))
(def degrees (gen/choose 0 100))
(def reading (gen/hash-map :scale scale :degrees degrees))
(def measurements (gen/vector reading 10))
; Base Temperatures
(gen/generate measurements)

(def fahrenhiet (gen/return :F))
(def celsius (gen/return :C))
(def kelvin (gen/return :K))
(def dist-scale (gen/frequency [[6 fahrenhiet] [3 celsius] [1 kelvin]]))
(def dist-reading (gen/hash-map :scale dist-scale :degrees degrees))
(def dist-measurements (gen/vector dist-reading 10))
; Realistically Distibuted Scale
(gen/generate dist-measurements)

(def scale-degrees
  {:F (gen/choose 0 100)
   :C (gen/choose 0 32)
   :K (gen/choose 280 310)})
(def realistic-reading
  (gen/bind dist-scale
            #(gen/hash-map :scale (gen/return %) :degrees (% scale-degrees))))
(def realistic-measurements (gen/vector realistic-reading 10))
; Realistic Temperatures
(gen/generate realistic-measurements)

(def accurate-reading
  (gen/fmap
    (fn [[precision reading]] (update reading :degrees #(+ % precision)))
    (gen/tuple (gen/fmap #(/ % 100.0) (gen/choose 0 100)) realistic-reading)))
(def accurate-measurements (gen/vector accurate-reading 10))
;Accurate Temperatures
(gen/generate accurate-measurements)

(def one-k-measurements
  (gen/such-that
    (fn [measurements]
      (some #(= :K (% :scale)) measurements))
    accurate-measurements))
; Final Temperatures
(gen/generate one-k-measurements)