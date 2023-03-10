(ns bb-cli-auto-coerce-weirdness
  (:require [babashka.cli :as cli]
            [nextjournal.clerk :as clerk]))

(clerk/table
 (for [example ["true" "false" ":abc" ":abc-def"]]
   {"example" example
    "(auto-coerce example)" (clerk/code (pr-str (cli/auto-coerce example)))
    "(type (auto-coerce example))" (type (cli/auto-coerce example))}))
