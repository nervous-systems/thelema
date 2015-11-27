(ns thelema.test.lambda-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.core.async :as async]
            [thelema.test.util :refer-macros [deftest-async]]
            [glossop.util :as g.util]
            [thelema.util :refer [log]]
            [glossop.core :refer-macros [<? go-catching]]
            [cljs-lambda.util]
            [thelema.lambda :as lambda]))

(deftest-async ^:integration audio-formats
  (go-catching
    (let [ctx (cljs-lambda.util/mock-context)
          results   (<? (lambda/audio-formats
                         {:urls ["https://www.youtube.com/watch?v=EFSpuOgH8xQ"]}
                         ctx))]
      (is (= 1 (count results)))
      (is (not-empty (:formats (first results)))))))

