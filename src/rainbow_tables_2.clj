;; # Rainbow tables 2
;;
;; > DISCLAIMER: Rainbow tables are complicated.
;; > For precise details, please read the [Wikipedia article][wikipedia-article].
;; > For a simplified introduction with specific examples, keep reading.
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
;; 1. A slower hash function made for hashing passwords
;; 2. Passord must be at least ten characters, and can contain non-ascii letters
;; 3. Store hash(passord+salt), not hash(passord)

^{:nextjournal.clerk/toc true}
(ns rainbow-tables-2
  (:require
   [next.jdbc]
   [nextjournal.clerk :as clerk]
   [babashka.fs]
   [babashka.process]
   [clojure.string :as str]))

;; ## A table of passwords and hashes
;;
;; A rainbow table can be used to look up `password` from `hash(password)`.
;;
;; First, we need a hash function.
;; We'll choose `base64_encode(sha1(password))`:

(defn sha1-str [password]
  (.encodeToString (java.util.Base64/getEncoder)
                   (.digest
                    (doto (java.security.MessageDigest/getInstance "SHA-1")
                      (.update (.getBytes password "UTF-8"))))))

;; We can use our hash function like this:

(clerk/example
 (sha1-str "abc")
 (sha1-str "cat")
 (sha1-str "teo"))

;; To create a lookup table, we enumerate multiple three letter passwords:

(defn alphabet->lookup-table [alphabet]
  (into {}
        (for [a alphabet
              b alphabet
              c alphabet]
          (let [password (str a b c)]
            [(sha1-str password) password]))))

(def rainbow-table
  (alphabet->lookup-table "abceot"))

(clerk/html
       [:p "We have an index of "
        [:em (count rainbow-table)]
        " passwords in our rainbow table :)"])

;; The first ten pairs of hash(password), hash are:

(->> rainbow-table
     (sort-by first)
     (take 10)
     (map (fn [[hash password]]
            {"hash(password)" hash
             "password" password}))
     (clerk/table))

;; ## A function from hash to password
;;
;; Our function from hash to password is now a lookup in a map!

(defn guess-password [sha1-digest]
  (get rainbow-table sha1-digest))

(clerk/table
 (for [h ["qZk+NkcGgWq6PiVxeFDCbJzQ2J0="
          "nZiejSfcng7DOJ/IVfFCw9QPDFA="
          "Q3ocFO+qjpiB72uwd0EdwdJMtMA="]]
   {"hash(passord)" h
    "passord" (guess-password h)}))

;; Voilà! We can now lookup people's passwords if we have the password hash.
;;
;; But there are limitations:
;;
;; 1. The password must be three characters long
;; 2. Passwords can only be created out of these letters: abceot
;; 3. The hash function is base64_encode(sha1(password))
;; 4. Passwords are not salted.
;;
;; It's easy to make mistakes when you roll your own system for securing user accounts without experience in information security.
;; And there are plenty of pitfalls we haven't touched.
;;
;; But at least you now know some examples of what can go wrong when you push ahead without considering how to secure user data!

;; ## Using common passwords
;;
;; Above, we computed the hash of all three letter passwords using the letters `abceot`.
;; When humans create their passwords, we can do better!
;; For instance, we can start with a list of common passwords:
;;
;; https://en.wikipedia.org/wiki/Wikipedia:10,000_most_common_passwords
;;
;; Try it yourself! 😊
;;
;; ---

^{:nextjournal.clerk/visibility {:code :hide}
  :nextjournal.clerk/no-cache true}
(let [garden-url "https://github.clerk.garden"
      github-user "teodorlu"
      github-repo "lab"
      href-rebuild (str garden-url "/" github-user "/" github-repo "?update=1")
      current-git-hash (-> (babashka.process/shell "git rev-parse HEAD" {:out :string}) :out (str/trim))]
  (clerk/html [:p "Document out of date? Force a " [:a {:href href-rebuild} "rebuild"] "!"
               " or " [:a {:href (str garden-url "/" github-user "/" github-repo "/commit/" current-git-hash "/src/rainbow_tables_2.html")} "view this document"] "."]))
