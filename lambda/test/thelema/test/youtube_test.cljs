(ns thelema.test.youtube-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.core.async :as async]
            [thelema.test.util :refer-macros [deftest-async]]
            [glossop.util :as g.util]
            [thelema.util :refer [log]]
            [glossop.core :refer-macros [<? go-catching]]
            [thelema.youtube :as youtube]))

(defn any-video! [& [arg]]
  (youtube/search! "horseback chanting out"
                   (merge {:results 1 :formats false} arg)))

(deftest-async ^:integration search!
  (go-catching
    (let [[{:keys [title url thumbnail] :as video} & videos]
          (<? (g.util/into []
                (youtube/search! "the fall tempo house"
                                 {:results 5 :formats false})))]
      (is (= (- 5 1) (count videos)))
      (is (and (not-empty title)
               (not-empty url)
               (not-empty thumbnail)) (keys video)))))

(deftest-async ^:integration get-video-formats!
  (go-catching
    (let [video   (<? (any-video!))
          formats (<? (async/into []
                        (youtube/get-video-formats! video {:limit 5})))]
      (is (= 5 (count formats)))
      (let [[{:keys [encoding type container url bitrate]}] formats]
        (is (and bitrate
                 (not-empty encoding)
                 (not-empty type)
                 (not-empty container)
                 (not-empty url)) formats)))))

(deftest-async ^:integration ^:benchmark bench-search!
  (go-catching
    (let [start   (.getTime (js/Date.))
          results (<? (g.util/into []
                        (youtube/search!
                         "lord of this world"
                         {:results 10 :formats 5})))
          finish  (.getTime (js/Date.))]
      (log "search! call took "(- finish start) "msecs"))))
