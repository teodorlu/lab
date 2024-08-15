(ns commentary.detektimen)

;; Reading https://parenteser.mattilsynet.io/detektimen/, I wanted explore macros and metadata.

(comment

  (defmacro with-span [_bindings & body]
    ~body)

  (defmacro defn🕵️‍♂️ [fn-name & forms]
    (let [[docstring arg-list & body] (if (string? (first forms))
                                        forms
                                        (cons "" forms))]
      `(defn ~fn-name
         ~docstring
         ~arg-list
         (with-span [~(str *ns* "/" fn-name)]
           ~@body))))

  (defn ^:nice-fn f [x] (* x 100))
  (:nice-fn (meta #'f))
  ;; => true

  (defn🕵️‍♂️ ^:nice-fn f [x] (* x 100))
  ;; => Syntax error macroexpanding teodorlu.appsec-2914.notes-2024-08-15/with-span at (repl/teodorlu/appsec_2914/notes_2024_08_15.clj:66:3).
  ;;    Attempting to call unbound fn: #'clojure.core/unquote

  (require 'clojure.spec.alpha)
  (require 'clojure.core.specs.alpha)

  (clojure.spec.alpha/describe :clojure.core.specs.alpha/defn-args)
  ;; => (cat
  ;;     :fn-name
  ;;     simple-symbol?
  ;;     :docstring
  ;;     (? string?)
  ;;     :meta
  ;;     (? map?)
  ;;     :fn-tail
  ;;     (alt
  ;;      :arity-1
  ;;      :clojure.core.specs.alpha/params+body
  ;;      :arity-n
  ;;      (cat
  ;;       :bodies
  ;;       (+ (spec :clojure.core.specs.alpha/params+body))
  ;;       :attr-map
  ;;       (? map?))))

  (clojure.spec.alpha/conform :clojure.core.specs.alpha/defn-args
                              '(^:nice-fn f [x] (* x 100)))

  (defmacro defn2 [& body]
    (clojure.spec.alpha/conform :clojure.core.specs.alpha/defn-args
                                body))

  (macroexpand-1 '(defn2 ^:nice-fn f [x] (* x 100)))
  ;; => {:fn-name f,
  ;;     :fn-tail
  ;;     [:arity-1 {:params {:params [[:local-symbol x]]}, :body [:body [(* x 100)]]}]}

  ;; => {:fn-name f,
  ;;     :fn-tail
  ;;     [:arity-1 {:params {:params [[:local-symbol x]]}, :body [:body [(* x 100)]]}]}


  (defmacro defn3 [& body]
    (let [conformed
          (clojure.spec.alpha/conform :clojure.core.specs.alpha/defn-args
                                      body)]
      (meta (:fn-name conformed))))

  (macroexpand-1 '(defn3 ^:nice-fn f [x] (* x 100)))
  ;; => {:nice-fn true}

  (let [x ^{:awesome :YES} 'x]
    (meta x))

  (meta
   (with-meta 'x {:awesome :YES}))

  ,)
