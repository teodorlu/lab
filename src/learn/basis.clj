(ns learn.basis)

(defrecord R [a b c])
(def r (->R 1 2 3))
(def c (resolve 'R))
(.. c (getMethod "getBasis" nil) (invoke nil nil))

(:arglists (meta #'->R))

(keys (map->R {}))
