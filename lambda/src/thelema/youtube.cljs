(ns thelema.youtube
  (:require [cljs.core.async :as async]
            [cljs.nodejs :as nodejs]
            [glossop.core :as g :refer-macros [go-catching <?]]
            [glossop.util :as g.util]
            [thelema.http]
            [cljs-http.client :as http]
            [thelema.util :as util]
            [plumbing.core :as plumbing :refer-macros [for-map]]
            [thelema.youtube.util :refer
             [parse-format video-search-url parse-search-results]]))

(def ytdl (nodejs/require "ytdl-core"))

(thelema.http/set-global-xhr-factory!)

(defn get-video-formats!
  "Video map -> channel of format maps"
  [{:keys [url]} & [{:keys [chan close? limit] :or {close? true}}]]
  (util/log "get-video-formats!" (str url))
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

(defn with-formats!
  [video & [{:keys [chan close? limit] :or {close? true}}]]
  (cond-> (go-catching
            (assoc video :formats
              (<? (g.util/into []
                    (get-video-formats! video {:limit limit})))))
    chan (async/pipe chan close?)))

(defn format-many!
  [videos & [{:keys [limit chan close? batch] :or {close? true batch 5}}]]
  (let [chan (or chan (async/chan))]
    (async/pipeline-async
     batch
     chan
     (fn [v out] (with-formats! v {:chan out :limit limit}))
     videos
     close?)
    chan))

(defn search-term->videos!
  [term & [{:keys [results chan close?] :or {close? true}}]]
  (let [chan (or chan (async/chan))]
    (go-catching
      (try
        (let [url (video-search-url term {:max-results results})
              videos (-> (http/request
                          {:method :get
                           :url url
                           :channel (thelema.http/response-chan)})
                         <?
                         util/->kebab-map
                         parse-search-results)]
          (async/onto-chan chan videos close?))
        (catch :default e
          (util/channel-error e chan close?))))
    chan))

(defn search! [term & [{:keys [results formats chan close?]
                        :or   {formats 5 results 10 close? true}}]]
  (let [chan (or chan (async/chan))]
    (go-catching
      (try
        (let [videos (search-term->videos! term {:results results})]
          (if formats
            (format-many! videos {:limit formats :chan chan :close? close?})
            (async/pipe videos chan close?)))
        (catch :default e
          (util/channel-error e chan close?))))
    chan))
