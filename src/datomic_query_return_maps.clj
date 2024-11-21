;; # `datomic.api/q`: hvordan funker return maps?

(ns datomic-query-return-maps
  (:require
   [datomic.api :as d]
   [nextjournal.clerk :as clerk]))

(set! *print-namespace-maps* false)

;; ## En lokal database å jobbe mot

(def revision 2) ; Increase revision when a fresh db is required.

(def schema-tx
  [{:db/ident :user/name
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/string}
   {:db/ident :user/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}
   {:db/ident :user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}])

{:db/ident :user/lollercoaster
 :db/valueType :db.type/string
 :db/cardinality :db.cardinality/one}

(def personer-tx
  [{:user/name "Teodor"
    :user/username "teodorlu"}
   {:user/name "Christian"
    :user/username "cjohansen"}])

(defn fresh-conn []
  (let [url (str "datomic:mem://" revision (d/squuid))]
    (d/create-database url)
    (d/connect url)))

(defn fresh-db []
  (let [conn (fresh-conn)]
    (d/transact conn schema-tx)
    (d/transact conn personer-tx)
    (d/sync conn)
    (d/db conn)))

;; ## Database som en verdi

(def db (fresh-db))

(d/entity db [:user/username "teodorlu"])

;; ## Spørringer med og uten return maps

;; Vanlige spørringer gir tilbake et set av tupler.

(d/q '[:find ?name ?username
       :where
       [?e :user/name ?name]
       [?e :user/username ?username]]
     db)

;; Spørringer med return maps gir tilbake et set av maps.

(d/q '[:find ?name ?username
       :keys name username
       :where
       [?e :user/name ?name]
       [?e :user/username ?username]]
     db)

;; `:keys` lager nøkkelord, `:syms` lager symboler og `:strs` lager strenger.

(d/q '[:find ?name ?username
       :syms name username
       :where
       [?e :user/name ?name]
       [?e :user/username ?username]]
     db)

(d/q '[:find ?name ?username
       :strs name username
       :where
       [?e :user/name ?name]
       [?e :user/username ?username]]
     db)

;; Det er lov å ha nøkkelord og strenger i stedet for symboler i selve spørringen.

(d/q '[:find ?name ?username
       :strs "Personens fornavn" :username
       :where
       [?e :user/name ?name]
       [?e :user/username ?username]]
     db)

;; ## Return maps og Clerk-tabeller
;;
;; Med return maps passer resultatet fra datomic.api/q rett inn i nextjournal.clerk/table.

(->>
 (d/q '[:find ?name ?username
        :strs Navn Brukernavn
        :where
        [?e :user/name ?name]
        [?e :user/username ?username]]
      db)
 (clerk/table))

^{::clerk/visibility {:code :hide :result :hide}}
(comment
  (clerk/serve! {:browse true})
  (clerk/halt!)
  )
