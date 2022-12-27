(ns pandoc-edn
  (:require [teodorlu.pandoc.alpha2 :as pandoc]
            [babashka.process]
            [babashka.process.pprint]))

pandoc/!exec-fn

(defn pandoc-exec-fn [s args]
  (slurp (:out (apply babashka.process/process {:in s}
                      "pandoc" args))))

(reset! pandoc/!exec-fn pandoc-exec-fn)

(comment
  (pandoc-exec-fn "Good morning!" ["--from" "markdown" "--to" "html"])

  (clojure.repl/doc babashka.process/process)

  )
