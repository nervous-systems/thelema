(ns thelema.http
  (:require [cemerick.url :as url]
            [cljs.core.async :as async :refer [>!]]
            [cljs.nodejs :as nodejs]
            [glossop.core :as glossop :refer-macros [go-catching <?]]
            [glossop.util :refer [close-with!]]
            [thelema.util :as util]))

(def http  (nodejs/require "http"))
(def https (nodejs/require "https"))

(defn concretize-port [{:keys [protocol port] :as u}]
  (if-not (= port -1)
    u
    (assoc u :port
      (case protocol
        "http" 80
        "https" 443))))

(defn req->node [{{:keys [query host port path]} :endpoint :keys [headers method] :as req}]
  (cond->
      {:host   host
       :path   (cond-> path query (str "?" (url/map->query query)))
       :method method
       :headers headers}
    port (assoc :port port)))

(defn request! [{body :body {:keys [protocol] :as u} :endpoint :as req} & [{:keys [chan]}]]
  (let [ch       (or chan (async/chan 10))
        node-req (.request
                  (case protocol "http" http "https" https)
                  (-> req (assoc :endpoint (concretize-port u)) req->node clj->js))]
    (.on node-req "response"
         (fn [resp]
           (if (glossop/error? resp)
             (close-with! ch resp)
             (let [headers (aget resp "headers")
                   status  (aget resp "statusCode")]
               (async/put! ch {:headers (js->clj headers :keywordize-keys true)
                               :status status})
               (.on resp "data"  (partial async/put! ch))
               (.on resp "error" (partial close-with! ch))
               (.on resp "end"   #(async/close! ch))))))
    (.on node-req "error" (partial close-with! ch))
    (.end node-req body)
    ch))

(defn channel-request! [req & [{:keys [chan close?]}]]
  (cond-> (go-catching
            (try
              (let [ch    (request! req)
                    resp  (<? ch)
                    body  (<? (glossop.util/reduce str "" ch))]
                (assoc resp :body body))
              (catch js/Error e
                ;; The eulalie code wants {:error e} - figure this out before
                ;; chopping this into an external library
                e)))
    chan (async/pipe chan close?)))

(defn get! [url]
  (util/log "HTTP GET" (str url))
  (channel-request!
   {:method :get
    :endpoint (cond-> url (string? url) url/url)}
   {:chan (async/chan
           1
           (map #(cond-> % (not (glossop/error? %)) :body)))}))
