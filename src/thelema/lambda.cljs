(ns thelema.lambda
  (:require [cljs-lambda.util :refer [async-lambda-fn]]
            [glossop.core :as g :refer-macros [go-catching <?]]
            [cljs.core.async :as async]
            [thelema.youtube :as youtube]
            [thelema.util    :as util]))

(set! *main-cli-fn* identity)

(def ^:export audio-search
  (async-lambda-fn
   (fn [{:keys [term] :as args} context]
     (util/log "audio-search" term (clj->js args))
     (async/into [] (youtube/search! term)))))