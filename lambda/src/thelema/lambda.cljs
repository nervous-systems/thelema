(ns thelema.lambda
  (:require [cljs-lambda.util :refer [async-lambda-fn]]
            [glossop.core :as g :refer-macros [go-catching <?]]
            [cljs.core.async :as async]
            [thelema.youtube :as youtube]
            [thelema.util    :as util]))

(when-not *main-cli-fn*
  (set! *main-cli-fn* identity))

(def ^:export audio-search
  (async-lambda-fn
   (fn [{:keys [term] :as args} context]
     (let [args (merge {:formats false} args)]
       (util/log "audio-search" (str "'" term "'") args)
       (async/into [] (youtube/search! term args))))))

(def ^:export audio-formats
  (async-lambda-fn
   (fn [{:keys [urls] :as args} context]
     (let [args (merge {:limit 5} args)]
       (util/log "audio-formats" args)
       (-> (for [u urls] {:url u})
           async/to-chan
           (youtube/format-many! args)
           (as-> ch (async/into [] ch)))))))
