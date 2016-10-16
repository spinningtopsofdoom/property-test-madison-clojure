(ns madison.test.latinify
  (:require [clojure.string :as string]
            [clojure.pprint :as pprint]
            [madison.gen.latinify :as lat-gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]))

(defn latinify [plain] (string/replace plain #"\bcat\b" "Felinus"))

(def only-replace-words
  (prop/for-all
    [sentence lat-gen/cat-sentence]
    (let [feline-sentence (latinify sentence)]
      (= (count (re-seq #"Felinus" feline-sentence)) (count (re-seq #"\bFelinus\b" feline-sentence))))))

(def matching-word-count
  (prop/for-all
    [sentence lat-gen/cat-sentence]
    (let [feline-sentence (latinify sentence)]
      (= (count (re-seq #"\bcat\b" sentence)) (count (re-seq #"\bFelinus\b" feline-sentence))))))

(def word-ordering
  (prop/for-all
    [sentence lat-gen/cat-sentence]
    (let [feline-sentence (latinify sentence)]
      (= (string/replace sentence #"\bcat\b" "") (string/replace feline-sentence #"\bFelinus\b" "")))))

(defn fails-every-case [_] "We have Felinusies here")
(defn fails-order-case [plain] (string/replace plain #"\bcat\b" "Felinus behnid us"))
(defn fails-replace-words [plain] (string/replace plain #"cat" "Felinus"))
(defn fails-matching-word-count [plain] (string/replace plain #"\bcat\b" ""))

(with-redefs [latinify latinify]
  (print "Only Replace words\n")
  (pprint/pprint (tc/quick-check 100 only-replace-words))
  (print "Matching Word Count\n")
  (pprint/pprint (tc/quick-check 100 matching-word-count))
  (print "Word Ordering\n")
  (pprint/pprint (tc/quick-check 100 word-ordering)))
