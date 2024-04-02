(ns learn.transducers)

;; "vanlig", allokerer en lazy-seq for hver mellomverdi

(->> (range 10)
     (map (partial * 3))
     (filter odd?)
     (into #{}))
;; => #{27 15 21 3 9}

;; med transducers, bør yte litt bedre (har vel ingenting å si med ti tall)

(into #{}
      (comp (map (partial * 3))
            (filter odd?))
      (range 10))
;; => #{27 15 21 3 9}

;; obs: argumentrekkefølgen er litt wonky når du bruker transducers og comp.
