(ns madison.test.friendship
  (:require [clojure.string :as string]
            [clojure.pprint :as pprint]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]))

(defn create-friendship-db []
  (atom {}))
; Querying Helper Methods
(defn get-people [db]
  (keys @db))

(defn friends? [db person friend]
  ((get @db person #{}) friend))

(defn person-friends [db person]
  (get @db person #{}))
; Command Operation generators
(def people ["Damaris" "Liyam" "Wye" "Isbelle" "Rebexa" "Aebby"])
(def operation
  (gen/tuple
    (gen/elements [:add-friendship :remove-friendship])
    (gen/elements people)
    (gen/elements people)))

(def operations (gen/vector operation))

(declare add-friendship! remove-friendship!)
(defn apply-operations [operations]
  (let [db (create-friendship-db)]
    (doseq [op operations]
      (case (first op)
        :add-friendship (apply add-friendship! (cons db (rest op)))
        :remove-friendship (apply remove-friendship! (cons db (rest op)))))
    db))
; Properties
(def no-self-friend
  (prop/for-all
    [ops operations]
    (let [db (apply-operations ops)]
      (not-any? #(friends? db % %) (get-people db)))))

(def symetrical-friendship
  (prop/for-all
    [ops operations]
    (let [db (apply-operations ops)]
      (every?
        (fn [person]
          (every? #(friends? db % person) (person-friends db person)))
        (get-people db)))))
; Command (stateful) Operaions
(defn bad-add-friendship! [db person new-friend]
  (let [friends (conj (person-friends db person) new-friend)]
    (swap! db #(assoc % person friends))
    db))

(defn bad-remove-friendship! [db person friend]
  (let [friends (disj (person-friends db person) friend)]
    (swap! db #(assoc % person friends))
    db))

(defn good-add-friendship! [db person new-friend]
  (when (not= person new-friend)
    (let [friends (conj (person-friends db person) new-friend)
          persons (conj (person-friends db new-friend) person)]
      (swap! db #(assoc % person friends new-friend persons))))
  db)

(defn good-remove-friendship! [db person friend]
  (let [friends (disj (person-friends db person) friend)
        persons (disj (person-friends db friend) person)]
    (swap! db #(assoc % person friends friend persons))
    db))

(with-redefs [add-friendship! good-add-friendship!
              remove-friendship! good-remove-friendship!]
  (print "No Self Friending\n")
  (pprint/pprint (tc/quick-check 100 no-self-friend))
  (print "Symetrical Friendship\n")
  (pprint/pprint (tc/quick-check 100 symetrical-friendship)))