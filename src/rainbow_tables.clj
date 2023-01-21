;; # Regnbuetabeller
;;
;; https://en.wikipedia.org/wiki/Rainbow_table
;;
;; Hvis du skal cracke passord, kan du bruke en "rainbow table".
;; Jeg synes ordet "regnbuetabell" er kulere, så jeg kommer til å snakke om regnbuetabeller.
;;
;; En regnbuetabell er en tabell med to kolonner: passord og en kryptografisk hash av passordet.
;; For å beskytte seg mot regnbuetabell-angrep, bruker man en "salt".
;; Man "salter passordet sitt" vet ikke å hashe passordet, men å hashe passord + tilfeldig lang string, "salt".
;;
;;    mittpassord = "megabrøl"
;;    salt = "da39a3ee5e"
;;    hash(string_concat(mittpassord, salt))
;;
;; så lagrer man både salt og hash(passord + salt) i tabellen.
;;
;; Hvis man lagrer hash(passord) direkte, kan man finne ut passord ved en stor tabell over hashen av strenger.
;; Sånne tabeller kan du laste ned fra Internett.
;; Da får du feks en 5 GB stor tekstfil.
;;
;; Nå skal vi lage en liten regnbuetabell, og bruker den til å "knekke" et passord.
;; For å unngå masse venting, bruker vi svært korte passord (tre tegn) og en usikker men rask hashefunksjon, SHA-1.
;; Skulle vi gjort dette vanskelig å knekke, hadde vi gjort andre valg:
;;
;; 1. En tyngre kryptografisk hashfunksjon
;; 2. Passord på minst ti tegn med tegn og norske bokstaver, ikke bare latinske
;; 3. Lagret hash(passord+salt) i tabellen, ikke hash(passord)

^{:nextjournal.clerk/toc true}
(ns rainbow-tables
  (:require [next.jdbc]
            [nextjournal.clerk :as clerk]
            [babashka.fs]
            [babashka.process]
            [clojure.string :as str]))

;; ## En regnbuetabell er en database som mapper hash til passord.
;;
;; Jeg bruker sqlite. Så jeg lager meg en funksjon `datasource` som lar meg
;; referere til en SQLite-fil

(do
  (def ^:private db-file "rainbow-table.sqlite")

  (defn datasource []
    (next.jdbc/get-datasource {:dbtype "sqlite" :dbname db-file})))

;; , og en funksjon `reset-db!` som sletter databasen og lar meg starte fra
;; scratch.

(do
  (defn reset-db! []
    (babashka.fs/delete-if-exists db-file))

  (comment
    ;; Kjør denne for å slette databasen og starte på nytt:
    (reset-db!)
    ))

;; Jeg kommer til å bruke `hexdigest(sha1(passord))` som hash-funksjon.
;; `sha1sum` er ofte tilgjengelig som en systemkommando. Du kan bruke den sånn:
;;
;;    $ echo "abc" | sha1sum
;;    03cfd743661f07975fa2f1220c5194cbaff48451  -
;;    $ echo "cat" | sha1sum
;;    8f6abfbac8c81b55f9005f7ec09e32d29e40eb40  -
;;    $ echo "teo" | sha1sum
;;    afe5d9ddc17f73b8ecf9558daf0ab32c4d544208  -
;;
;; Jeg bruker [babashka/process][babashka-process] for å kalle systemprosesser
;; fra Clojure.
;;
;; [babashka-process]: https://github.com/babashka/process

(defn sha1sum-digest [s]
  (-> (slurp (:out (babashka.process/process
                    ["sha1sum"]
                    {:in s})))
      (str/split #" ")
      first))

;; Digresjon.
;;
;; Teknisk bruker vi en hex-digest av sha1-hashen til passordet. Men en
;; hex-digest er lettere å jobbe med enn binærdata. Og
;;
;;    (fn [password]
;;      (hex-digest (hash password)))
;;
;; er (faktisk) også en gyldig hash-funksjon! Bare at typen til hashen er
;; tekst (string), ikke binærdata. Og strings er lettere å vise fram.
;;
;; Digresjon slutt!
;;
;; Vi kan bruke hash-funksjonen vår sånn:

[(sha1sum-digest "abc")
 (sha1sum-digest "cat")
 (sha1sum-digest "teo")]

;; ## Vi lagrer passord og hash(passord) i en databasetabell.
;;
;; Tabellen trenger to kolonner:
;;
;; - `password`: passordet
;; - `sha1sum_digest`: hash av passordet
;;

(defn setup-schema []
  (let [ds (datasource)]
    (with-open [conn (next.jdbc/get-connection ds)]
      (next.jdbc/execute!
       conn
       [(str "CREATE TABLE IF NOT EXISTS rainbowtable"
             " (sha1sum_digest string UNIQUE, password string)")]))))

(setup-schema)

;; Nå kan vi lage oss en regnbuetabell.

(let [alphabet "abcdefghijklmnopqrstuvwxyz"]
  (with-open [conn (next.jdbc/get-connection (datasource))]
    (next.jdbc/with-transaction [tx conn]
      (doseq [a alphabet
              b alphabet
              c alphabet]
        (let [abc (str a b c)
              digest (sha1sum-digest abc)]
          (next.jdbc/execute!
           tx
           [(str "INSERT INTO rainbowtable (password, sha1sum_digest) VALUES (?, ?)"
                 " ON CONFLICT (sha1sum_digest) DO UPDATE SET sha1sum_digest=?")
            abc digest digest]))))))

;; ## Vi kan bruke regnbuetabellen som en funksjon fra hash til passord!

;; Her er de første verdiene fra tabellen:

(let [_invalidate-cache 7258
      ds (datasource)]
  (with-open [conn (next.jdbc/get-connection ds)]
    (clerk/table (next.jdbc/execute!
                  conn
                  ["SELECT * FROM rainbowtable ORDER BY sha1sum_digest LIMIT 20"]))))

;; Hvor mange passord har vi regnet ut hashen til?

(let [_invalidate-cache 7006
      ds (datasource)]
  (with-open [conn (next.jdbc/get-connection ds)]
    (let [rainbow-table-size (next.jdbc/execute-one!
                              conn
                              ["SELECT count(*) AS rows FROM rainbowtable"])]
      (clerk/html
       [:p "Vi har "
        [:em (:rows rainbow-table-size)]
        " elementer i regnbuetabellen vår :)"]))))

(defn guess-password [{:keys [sha1sum-digest]}]
  (let [_invalidate-cache 7432
        ds (datasource)]
    (with-open [conn (next.jdbc/get-connection ds)]
      (next.jdbc/execute-one!
       conn
       ["SELECT password FROM rainbowtable WHERE sha1sum_digest=?" sha1sum-digest]))))

(clerk/table
 (for [h ["6fee74066d6f9452b311669272b91809504534c5"
          "9d989e8d27dc9e0ec3389fc855f142c3d40f0c50"
          "437a1c14efaa8e9881ef6bb077411dc1d24cb4c0"]]
   {"hash(passord)" h
    "passord" (:rainbowtable/password (guess-password {:sha1sum-digest h}))}))

;; Voilà! Vi kan nå slå opp passordet til folk hvis vi har hashen.
;;
;; Men det gjelder bare når:
;;
;; 1. Passordet er tre bokstaver langt
;; 2. Passordet inneholder bare små latinske bokstaver: abcdefghijklmnopqrstuvw
;; 3. Hashfunksjonen er hexdigest(sha1sum(passordet))
;; 4. Og passord saltes ikke.
;;
;; Det er vanskelig å ikke gjøre noe feil når man finner opp et ad-hoc system
;; for sikring av brukerkontoer, uten å kunne informasjonssikkerhet. Og det er
;; ikke bare disse feilene man kan gjøre heller.
;;
;; Men nå vet du i det minste litt om hva som kan gå galt hvis du peiser på uten
;; å tenke på hvordan du sikrer dataen til brukerne dine!
