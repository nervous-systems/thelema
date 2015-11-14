(ns thelema.youtube
  (:require [cljs.core.async :as async]
            [cljs.nodejs :as nodejs]
            [glossop.core :as g :refer-macros [go-catching <?]]
            [glossop.util :as g.util]
            [thelema.http :as http]
            [thelema.util :as util]
            [plumbing.core :as plumbing :refer-macros [for-map]]
            [thelema.youtube.util :refer
             [parse-format video-search-url parse-search-results]]))

(def ytdl (nodejs/require "ytdl-core"))

(defn get-video-formats!
  "Video map -> channel of format maps"
  [{:keys [url]} & [{:keys [chan close? limit] :or {close? true}}]]
  (let [chan (or chan (async/chan))]
    (.getInfo
     ytdl (str url) #js {:filter "audioonly"}
     (fn [e m]
       (if e
         (util/channel-error e chan close?)
         (let [formats (-> m
                           util/clean-map
                           :formats
                           (cond->> limit (take limit)))]
           (async/onto-chan
            chan
            (map parse-format formats)
            close?)))))
    chan))

(defn get-formats!
  "Sequence of videos -> channel of [video formats-or-error] pairs"
  [videos & [{:keys [chan close? limit] :or {close? true}}]]
  (let [chan        (or chan (async/chan))
        chan->video (for-map [v videos]
                      (g.util/into [] (get-video-formats! v {:limit limit})) v)]
    (async/pipe (g.util/keyed-merge chan->video) chan close?)
    chan))

(defn formatted!
  "Sequence of videos -> channel of videos with :formats keys"
  [videos & [{:keys [chan close? formats] :or {close? true}}]]
  (util/log "formatted!" "Retrieving formats for" (count videos) "videos")
  (let [xform  (map (fn [[v fs]] (assoc v :formats fs)))
        videos (get-formats!
                videos
                {:chan (async/chan 1 xform)
                 :limit formats})]
    (cond-> videos chan (async/pipe chan close?))))

(defonce last-fetch (atom nil))

(defn search! [q & [{:keys [results chan close? formats]
                     :or   {formats 5 results 10 close? true}}]]
  (let [chan (or chan (async/chan))]
    (go-catching
      (try
        (-> q
            (video-search-url {:max-results results})
            http/http-get!
            <?
            :body
            util/json->map
            parse-search-results
            (formatted! {:chan chan :close? close? :formats formats}))
        (catch :default e
          (util/channel-error e chan close?))))
    chan))

(defn smash [ch & [many]]
  (let [a (atom nil)]
    (go-catching
      (try
        (reset! a (<? (cond->> ch many (async/into []))))
        (catch :default e
          (reset! a e))))
    a))