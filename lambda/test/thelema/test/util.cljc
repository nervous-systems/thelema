(ns thelema.test.util
  #? (:clj
      (:require [glossop.core :refer [<? go-catching]])
      :cljs
      (:require-macros [thelema.test.util])))

#? (:clj
    (defmacro deftest-async [t-name & forms]
      `(cljs.test/deftest ~t-name
         (cljs.test/async
          done#
          (go-catching
            (try
              (<? (do ~@forms))
              (catch :default e#
                (println (.. e# -stack))
                (cljs.test/is (nil? e#))))
            (done#))))))
