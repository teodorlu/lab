;; # Rainbow tables
;;
;; > DISCLAIMER: Rainbow tables are more complicated then what I explain in this document.
;; > For a full depth introduction to rainbow tables, please read the [Wikipedia article][wikipedia-article].
;; > For a simplified introduction with specific examples, please keep reading.
;;
;; [wikipedia-article]: https://en.wikipedia.org/wiki/Rainbow_table
;;
;; To crack passwords, you can use a rainbow table.
;; A rainbow table has two columns: password, and cryptographic hash of the password.
;; To protect against rainbow table attacks, you can use a salt.
;;
;; By "salting the password", you don't store the hash of the password in the database.
;; The salt is a long, random string.
;;
;;    mypassword = "kaladinrocks"
;;    salt = "da39a3ee5e"
;;    hash(string_concat(mittpassord, salt))
;;
;; then store both salt and hash(password + salt) in your table.
;;
;; If you store hash(password) directly, you make it easier for a malicious actor to find user passwords from your table.
;; That malicious actor can precompute a bunch of password hashes, and look up whether there's a match.
;; You can find tables like that on the internet, for instance as a 5 GB text file.
;;
;; We're going to make a small rainbow table, and use it to "crack" a password.
;; To avoid lots of wainting, we're going to stick to passwords that are easy to crack:
;;
;; 1. Passwords are very short
;; 2. Passwords use only lowercase english character
;; 3. We use a quite fast hash function: SHA-1
;;
;; If we were to make our table harder to crack, we would make different choices:
;;
;; 1. En slower hash function made for hashing passwords
;; 2. Passord must be at least ten characters, and can contain non-ascii letters
;; 3. Store hash(passord+salt), not hash(passord)

^{:nextjournal.clerk/toc true}
(ns rainbow-tables
  (:require [next.jdbc]
            [nextjournal.clerk :as clerk]
            [babashka.fs]
            [babashka.process]
            [clojure.string :as str]))

;; ## A table of passwords and hashes
;;
;; I'm going to store my rainbow table in SQLite. To connect to the SQLite database, I need a datasource.

(do
  (def ^:private db-file "rainbow-table.sqlite")

  (defn datasource []
    (next.jdbc/get-datasource {:dbtype "sqlite" :dbname db-file})))

;; , and I'm making a `reset-db!` function for REPL usage -- it lets me delete
;; the database and start from scratch in case I mess up the schema.

(do
  (defn reset-db! []
    (babashka.fs/delete-if-exists db-file))

  (comment
    ;; Run to delete the database and start from scratch:
    (reset-db!)
    ))

;; I'm going to use `hexdigest(sha1(password))` as hash function.
;; `sha1sum` is often available as a system command.
;; You can use `sha1sum` like this:
;;
;;    $ echo -n "abc" | sha1sum
;;    a9993e364706816aba3e25717850c26c9cd0d89d  -
;;    $ echo "cat" | sha1sum
;;    9d989e8d27dc9e0ec3389fc855f142c3d40f0c50  -
;;    $ echo -n "teo" | sha1sum
;;    437a1c14efaa8e9881ef6bb077411dc1d24cb4c0  -
;;
;; We use `echo -n` to hash "abc", and not "abc\n".
;; Try it yourself, and see if you get a different hash!

;; We'll use [babashka/process][babashka-process] to call system processes from Clojure.
;;
;; [babashka-process]: https://github.com/babashka/process

(defn sha1sum-digest [s]
  (-> (slurp (:out (babashka.process/process
                    ["sha1sum"]
                    {:in s})))
      (str/split #" ")
      first))

;; Digression.
;;
;; Technically, we're using the hex digest of the sha 1 hash of the password.
;; But the hex digest is a string - and is easier to visualize than binary data.
;; And
;;
;;    (fn [password]
;;      (hex-digest (hash password)))
;;
;; is a valid hash function!
;; Just -- the type of the hash is a string, not binary data.
;; And strings are easy to show.
;;
;; Digresjon done!
;;
;; We can use our hash function like this:

[(sha1sum-digest "abc")
 (sha1sum-digest "cat")
 (sha1sum-digest "teo")]

;; ## Precomputing hashes
;;
;; The table has two columns:
;;
;; - `password`: the password
;; - `sha1sum_digest`: the password hash
;;

(defn setup-schema []
  (let [ds (datasource)]
    (with-open [conn (next.jdbc/get-connection ds)]
      (next.jdbc/execute!
       conn
       [(str "CREATE TABLE IF NOT EXISTS rainbowtable"
             " (sha1sum_digest string UNIQUE, password string)")]))))

(setup-schema)

;; We have a table!
;; Let's put in some values.

;; Nextjournal happs to be paying for the infrastructure that builds this document.
;; So we're going to avoid generating really big rainbow tables on each build.

(let [alphabet "abceot"]                    ; small alphabet -> fast
  (with-open [conn (next.jdbc/get-connection (datasource))]
    (next.jdbc/with-transaction [tx conn]   ; in a transaction -> fast
      (doseq [a alphabet
              b alphabet
              c alphabet]
        (let [abc (str a b c)               ; short passwords -> fast
              digest (sha1sum-digest abc)]
          (next.jdbc/execute!
           tx
           [(str "INSERT INTO rainbowtable (password, sha1sum_digest) VALUES (?, ?)"
                 " ON CONFLICT (sha1sum_digest) DO UPDATE SET sha1sum_digest=?")
            abc digest digest]))))))

;; ## A function from hash to password
;;
;; Here are the first couple of hash(password), password pairs:

(let [_invalidate-cache 7258
      ds (datasource)]
  (with-open [conn (next.jdbc/get-connection ds)]
    (clerk/table (next.jdbc/execute!
                  conn
                  ["SELECT * FROM rainbowtable ORDER BY sha1sum_digest LIMIT 20"]))))

;; How many password hashes have we stored?

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

;; Voilà! We can now lookup people's passwords if we have the password hash.
;;
;; But there's some limitations:
;;
;; 1. The password must be three characters long
;; 2. Passwords can only be created out of these letters: abceot
;; 3. The hash function is hexdigest(sha1(password))
;; 4. Passwords are not salted.
;;
;; It's easy to make a mistake when you roll your own system for securing user
;; accounts without experience in information security.
;; And there are plenty of pitfalls we haven't touched.
;;
;; But at least you now know some examples of what can go wrong when you push
;; ahead without considering how you're securing user data!

;; ## Using common passwords
;;
;; Above, we computed the hash of all three letter passwords using the letters `abceot`.
;; When humans create their passwords, we can do better!
;; For instance, we can start with a list of common passwords:
;;
;; https://en.wikipedia.org/wiki/Wikipedia:10,000_most_common_passwords
;;
;; Try it yourself! 😊

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:a {:href "https://github.clerk.garden/teodorlu/clerk-stuff?update=1"}
             "Rebuild this document."])
