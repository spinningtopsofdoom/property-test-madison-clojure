(ns madison.gen.latinify
  (:require [clojure.test.check.generators :as gen]))

(def non-cat-word (gen/elements ["where" "is" "the" "fat" "with" "a" "on"]))
(def cat-word (gen/return "cat"))
(def cat-within-word (gen/elements ["hepcat" "catamaran"]))
(def words (gen/frequency [[10 non-cat-word] [1 cat-word] [1 cat-within-word]]))
(def cat-sentence (gen/fmap #(apply str (interpose  " " %)) (gen/vector words)))

(gen/sample cat-sentence)