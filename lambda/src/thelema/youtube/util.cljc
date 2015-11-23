(ns thelema.youtube.util
  "Platform-neutral data transformations, mostly"
  (:require [clojure.set :as set]
            [cemerick.url :refer [url]]
            [thelema.util :refer [->camel-map log]]))

(def API-KEY (thelema.util/compile-env "THELEMA_YT_API_KEY"))

(def url-query #(assoc %1 :query (->camel-map %2)))

(def search-url (partial url-query (url "https://www.googleapis.com/youtube/v3/search")))
(def video-url  (partial url-query (url "https://www.youtube.com/watch")))

(def search-params {:part "snippet" :max-results 50 :type "video" :key API-KEY})
(defn video-search-url [q & [attrs]]
  (let [search-url (search-url (merge search-params (assoc attrs :q q)))]
    (log "Video search URL" search-url)
    search-url))

(defn parse-format [m]
  (-> m
      (set/rename-keys
       {:audio-encoding :encoding
        :audio-bitrate :bitrate})
      (select-keys #{:encoding :bitrate :type :container :url})))

(defn parse-search-result
  [{{:keys [video-id]} :id
    {:keys [title description] {{thumbnail :url} :high} :thumbnails} :snippet}]
  {:id video-id
   :url (str (video-url {:v video-id}))
   :title title
   :description description
   :thumbnail thumbnail})

(defn parse-search-results
  [{:keys  [next-page-token items]
    {:keys [total-results]} :page-info}]
  (with-meta
    (map-indexed
     (fn [i r]
       (assoc (parse-search-result r) :thelema/index i))
     items)
    {:token next-page-token
     :count total-results}))

