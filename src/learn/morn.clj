(ns learn.morn)

(let [state (atom "")
      set-state (partial reset! state)
      get-state #(deref state)
      append-str #(set-state (str (get-state) %))]
  (append-str "Morn")
  (dotimes [_ 10]
    (append-str "!"))
  (get-state))

(eval `(do ~(for [c (str 'morn)]
              `
             )))


(eval `(do ~@(for [c (str 'morn)]
               `(print ~c))
           (println)))
