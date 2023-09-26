(ns babashka-cli-intro.jukselapp
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [babashka.cli :as cli]
   [clojure.repl :refer [doc]]))

(doc fs/list-dir)

(doc fs/temp-dir)
(->> (fs/list-dir (fs/temp-dir))
     (map str)
     sort
     (take 5))

(doc process/shell)   ;; høynivå, gode defaults for shell-scripts

(doc process/process) ;; mer lavnivå, lar deg kontrollere diverse

(doc cli/parse-opts)
(cli/parse-opts ["--file" "f"
                 "--verbose"
                 "--lang" "clojure"
                 "--developers" "7"])

(doc cli/dispatch)
(cli/dispatch [{:cmds ["commit"] :fn identity}]
              ["commit" "-m" "Fix a bug I introduced yesterday"])
