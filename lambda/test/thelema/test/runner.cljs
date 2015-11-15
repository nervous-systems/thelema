(ns thelema.test.runner
  (:require [cljs.test]
            [thelema.test.youtube-test]))

(defn run []
  (cljs.test/run-tests
   'thelema.test.youtube-test))

(enable-console-print!)
(set! *main-cli-fn* run)

