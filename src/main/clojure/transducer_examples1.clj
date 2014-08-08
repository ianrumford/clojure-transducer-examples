
(ns transducer-examples1
  (:require 
   [clojure.string :as string]
   [clojure.core.reducers :as r]))

;; The Families in the Village

(def village
  [{:home :north :family "smith" :name "sue" :age 37 :sex :f :role :parent}
   {:home :north :family "smith" :name "stan" :age 35 :sex :m :role :parent}
   {:home :north :family "smith" :name "simon" :age 7 :sex :m :role :child}
   {:home :north :family "smith" :name "sadie" :age 5 :sex :f :role :child}
   
   {:home :south :family "jones" :name "jill" :age 45 :sex :f :role :parent}
   {:home :south :family "jones" :name "jeff" :age 45 :sex :m :role :parent}
   {:home :south :family "jones" :name "jackie" :age 19 :sex :f :role :child}
   {:home :south :family "jones" :name "jason" :age 16 :sex :f :role :child}
   {:home :south :family "jones" :name "june" :age 14 :sex :f :role :child}

   {:home :west :family "brown" :name "billie" :age 55 :sex :f :role :parent}
   {:home :west :family "brown" :name "brian" :age 23 :sex :m :role :child}
   {:home :west :family "brown" :name "bettie" :age 29 :sex :f :role :child}
   
   {:home :east :family "williams" :name "walter" :age 23 :sex :m :role :parent}
   {:home :east :family "williams" :name "wanda" :age 3 :sex :f :role :child}])

;; Example 1a - using a reducer to add up all the mapped values

(def ex1a-map-children-to-value-1 (r/map #(if (= :child (:role %)) 1 0)))

(r/reduce + 0 (ex1a-map-children-to-value-1 village))
;;=>
8

;; Example 1b - using a transducer to add up all the mapped values

;; create the transducers using the new arity for map that
;; takes just the function, no collection

(def ex1b-map-children-to-value-1 (map #(if (= :child (:role %)) 1 0)))

;; now use transduce (c.f r/reduce) with the transducer to get the answer 
(transduce ex1b-map-children-to-value-1 + 0 village)
;;=>
8

;; Example 2a - using a reducer to count the children in the Brown family

;; create the reducer to select members of the Brown family
(def ex2a-select-brown-family (r/filter #(= "brown" (string/lower-case (:family %)))))

;; compose a composite function to select the Brown family and map children to 1
(def ex2a-count-brown-family-children (comp ex1a-map-children-to-value-1 ex2a-select-brown-family))

;; reduce to add up all the Brown children
(r/reduce + 0 (ex2a-count-brown-family-children village))
;;=>
2

;; Example 2b - using a transducer to count the children in the Brown family

;; create the transducer filter to select members of the Brown family
(def ex2b-select-brown-family (filter #(= "brown" (string/lower-case (:family %)))))

;; compose a composite function to select the Brown family and map children to 1
;; NOTE: transducer comp functions are applied left-to-right
(def ex2b-count-brown-family-children (comp ex2b-select-brown-family ex1b-map-children-to-value-1))

;; transduce to add up all the Brown children
(transduce ex2b-count-brown-family-children + 0 village)
;;=>
2

;; Example 3a - using a reducer to count children with names beginning with J

;; select (filter) just the children
(def ex3a-select-children (r/filter #(= :child (:role %))))

;; select names beginning with "j"
(def ex3a-select-names-beginning-with-j (r/filter #(= "j" (string/lower-case (first (:name %))))))

;; In Example 1 we created the _map_ function
;; ex1a-map-children-to-value-1 to enable reduce to count the number
;; of children.

;; But the need to count the number of entries in a collection using
;; reduce, after a pipeline possibly involving many filters and
;; mappers, is a common one. This is straightforward to do, in the final
;; stage of the pipeline use a map function to transform each entry to
;; value 1.

;; map entries in a collection to 1
(def ex0a-map-to-value-1 (r/map (fn [v] 1)))

;; create the three step count-children-with-names-beginning-j function
(def ex3a-count-children-with-names-beginning-j (comp ex0a-map-to-value-1
                                                      ex3a-select-names-beginning-with-j
                                                      ex3a-select-children))

;; reduce the village with the ex32-count-children-with-names-beginning-j function
(r/reduce + 0 (ex3a-count-children-with-names-beginning-j village))
;; =>
3

;; Example 3b - using a transducer to count children with names beginning with J

;; select (filter) just the children
(def ex3b-select-children (filter #(= :child (:role %))))

;; select names beginning with "j"
(def ex3b-select-names-beginning-with-j (filter #(= "j" (string/lower-case (first (:name %))))))

;; map entries in a collection to 1
(def ex0b-map-to-value-1 (map (fn [v] 1)))

;; create the three step count-children-with-names-beginning-j function
;; note the left-to-right order in which the indivudal transducers
(def ex3b-count-children-with-names-beginning-j (comp ex3b-select-children
                                                      ex3b-select-names-beginning-with-j
                                                      ex0b-map-to-value-1))

;; transduce the village with the ex3b-count-children-with-names-beginning-j transducer
(transduce ex3b-count-children-with-names-beginning-j + 0 village)
;; =>
3

;; Example 4a - a reducer to create a collection of children whose names start with J?

;; create a reducing function to select (filter) children with names starting with "J"
(def ex4a-select-children-with-names-beginning-with-j (comp ex3a-select-names-beginning-with-j
                                                            ex3a-select-children))

;; use into to create a vector of the "J" children
(into [] (ex4a-select-children-with-names-beginning-with-j village))
;; =>
[{:age 19, :home :south, :name "jackie", :sex :f, :family "jones", :role :child}
 {:age 16, :home :south, :name "jason", :sex :f, :family "jones", :role :child}
 {:age 14, :home :south, :name "june", :sex :f, :family "jones", :role :child}]

;; Example 4b - a transducer to create a collection of children whose names start with J?

;; create a reducing function to select (filter) children with names starting with "J"t
(def ex4b-select-children-with-names-beginning-with-j (comp ex3b-select-names-beginning-with-j
                                                            ex3b-select-children))

;; use into to create a vector of the "J" children
(into [] ex4b-select-children-with-names-beginning-with-j village)
;; =>
[{:age 19, :home :south, :name "jackie", :sex :f, :family "jones", :role :child}
 {:age 16, :home :south, :name "jason", :sex :f, :family "jones", :role :child}
 {:age 14, :home :south, :name "june", :sex :f, :family "jones", :role :child}]

;; Example 5a - using a reducer to calculate the average age of children on or below the equator

;; map :home to latitude and longitude
(def ex5a-map-home-to-latitude-and-longitude
  (r/map
   (fn [v]
     (condp = (:home v)
       :north (assoc v :lat 90 :lng 0)
       :south (assoc v :lat -90 :lng 0)
       :west (assoc v :lat 0 :lng -180)
       :east (assoc v :lat 0 :lng 180)))))


;; select people on or below the equator i.e. latitude <= 0
(def ex5a-select-people-on-or-below-equator (r/filter #(>= 0 (:lat %))))

;; To find the average age, we need to add up all the children's ages and
;; divide by how many children.

;; Note, rather than creating a composite pipeline function, in this example
;; the individual stages of the pipeline are used explicitly.

;; count the number of children on or below the equator
(def ex5a-no-children-on-or-below-the-equator
  (r/reduce + 0
          (ex0a-map-to-value-1
           (ex5a-select-people-on-or-below-equator
            (ex5a-map-home-to-latitude-and-longitude
             (ex3a-select-children village))))))


;; sum the ages of children
(def ex5a-select-age (r/map #(:age %)))

(def ex5a-sum-of-ages-of-children-on-or-below-the-equator
  (r/reduce + 0
          (ex5a-select-age
           (ex5a-select-people-on-or-below-equator
            (ex5a-map-home-to-latitude-and-longitude
             (ex3a-select-children village))))))


;; calculate the average age of children on or below the equator
(def ex5a-averge-age-of-children-on-or-below-the-equator
  (float (/ ex5a-sum-of-ages-of-children-on-or-below-the-equator ex5a-no-children-on-or-below-the-equator )))
;; =>
17.3

;; Example 5b - using a transducer to calculate the average age of children on or below the equator

;; map :home to latitude and longitude
(def ex5b-map-home-to-latitude-and-longitude
  (map
   (fn [v]
     (condp = (:home v)
       :north (assoc v :lat 90 :lng 0)
       :south (assoc v :lat -90 :lng 0)
       :west (assoc v :lat 0 :lng -180)
       :east (assoc v :lat 0 :lng 180)))))


;; select people on or below the equator i.e. latitude <= 0
(def ex5b-select-people-on-or-below-equator (filter #(>= 0 (:lat %))))

;; create a "utility" transducer to select children on or below the equator
(def ex5b-select-children-on-or-below-the-equator
  (comp ex3b-select-children
        ex5b-map-home-to-latitude-and-longitude
        ex5b-select-people-on-or-below-equator)) 

;; create a transducer to count the number of children on or below the equator
(def ex5b-count-children-on-or-below-the-equator
  (comp ex5b-select-children-on-or-below-the-equator
        ex0b-map-to-value-1))

;; now count the number of children on or below the equator
(def ex5b-no-children-on-or-below-the-equator
  (transduce ex5b-count-children-on-or-below-the-equator + 0 village))

;; create a transducer to extract the age
(def ex5b-select-age (map #(:age %)))

;; create a transducer to extract the ages of all chilren
;; on or below the equator
(def ex5b-extract-ages-of-children-on-or-below-thew-equator
  (comp
   ex5b-select-children-on-or-below-the-equator
   ex5b-select-age))

;; now sum the ages
(def ex5b-sum-of-ages-of-children-on-or-below-the-equator
  (transduce ex5b-extract-ages-of-children-on-or-below-thew-equator + 0 village ))

;; calculate the average age of children on or below the equator
(def ex5b-averge-age-of-children-on-or-below-the-equator
  (float (/ ex5b-sum-of-ages-of-children-on-or-below-the-equator ex5b-no-children-on-or-below-the-equator)))
;; =>
17.3

;; Example 6a - time a reducer adding up Example 5's ages

;; time reducer adding up Example 5's ages
(time (dotimes [n 100000] (r/reduce +
                                  (ex5a-select-age
                                   (ex5a-select-people-on-or-below-equator
                                    (ex5a-map-home-to-latitude-and-longitude
                                     (ex3a-select-children village)))))))
;; =>
"Elapsed time: ~700 msecs"

;; time fold adding up Example 5's ages
(time (dotimes [n 100000] (r/fold +
                               (ex5a-select-age
                                (ex5a-select-people-on-or-below-equator
                                 (ex5a-map-home-to-latitude-and-longitude
                                  (ex3a-select-children village)))))))
;; =>
"Elapsed time: ~700 msecs"

;; Example 6b - time a transducer adding up Example 5's ages

(time (dotimes [n 100000] (transduce ex5b-extract-ages-of-children-on-or-below-thew-equator + 0 village)))
;; =>
"Elapsed time: ~700 msecs"

;; Example 7 - make some visitors

(def ex7-fn-random-name (fn [] (rand-nth ["chris" "jim" "mark" "jon" "lisa" "kate" "jay" "june" "julie" "laura"])))
(def ex7-fn-random-family (fn [] (rand-nth ["smith" "jones" "brown" "williams" "taylor" "davies"])))
(def ex7-fn-random-home (fn [] (rand-nth [:north :south :east :west])))
(def ex7-fn-random-sex (fn [] (rand-nth [:m :f])))
(def ex7-fn-random-role (fn [] (rand-nth [:child :parent])))
(def ex7-fn-random-age (fn [] (rand-int 100)))

(def ex7-visitor-template
  {:home ex7-fn-random-home
   :family ex7-fn-random-family
   :name ex7-fn-random-name
   :age ex7-fn-random-age
   :sex ex7-fn-random-sex
   :role ex7-fn-random-role})

(defn ex7-make-visitor [] (into {} (for [[k v] ex7-visitor-template] [k (v)])))

(defn ex7-make-visitors [n] (take n (repeatedly ex7-make-visitor)))

(def ex7-visitors (into [] (ex7-make-visitors 10000000)))

;; Example 7 - make some visitors

(def ex7-fn-random-name (fn [] (rand-nth ["chris" "jim" "mark" "jon" "lisa" "kate" "jay" "june" "julie" "laura"])))
(def ex7-fn-random-family (fn [] (rand-nth ["smith" "jones" "brown" "williams" "taylor" "davies"])))
(def ex7-fn-random-home (fn [] (rand-nth [:north :south :east :west])))
(def ex7-fn-random-sex (fn [] (rand-nth [:m :f])))
(def ex7-fn-random-role (fn [] (rand-nth [:child :parent])))
(def ex7-fn-random-age (fn [] (rand-int 100)))

(def ex7-visitor-template
  {:home ex7-fn-random-home
   :family ex7-fn-random-family
   :name ex7-fn-random-name
   :age ex7-fn-random-age
   :sex ex7-fn-random-sex
   :role ex7-fn-random-role})

(defn ex7-make-visitor [] (into {} (for [[k v] ex7-visitor-template] [k (v)])))

(defn ex7-make-visitors [n] (take n (repeatedly ex7-make-visitor)))

(def ex7-visitors (into [] (ex7-make-visitors 10000000)))

;; Example 7a - using reducers count the visiting Brown children

;; count the visiting Brown children using reduce
(time (r/reduce + 0 (ex2a-count-brown-family-children ex7-visitors)))
;; =>
"Elapsed time: ~1600 msecs"

;; count the visiting Brown children using fold
(time (r/fold + (ex2a-count-brown-family-children ex7-visitors)))
;; =>
"Elapsed time: ~555 msecs"

;; Example 7b - using reducers count the visiting Brown children

;; count the visiting Brown children using transduce
(time (transduce ex2b-count-brown-family-children + 0 ex7-visitors))
;; =>
"Elapsed time: ~1640 msecs"

;; Example 7c - using core map, filter and reduce to count the visiting Brown children

;; count the visiting Brown children using core map, filter and reduce
(time (reduce + 0
              (map #(if (= :child (:role %)) 1 0)
                   (filter #(= "brown" (string/lower-case (:family %))) ex7-visitors))))

;; =>
"Elapsed time: ~2000 msecs"

(defn -main
  [& args])
