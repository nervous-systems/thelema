(ns thelema.http
  (:import [goog.net XmlHttp XmlHttpFactory])
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async]))

(def xml-http-request
  (-> (nodejs/require "xmlhttprequest")
      (.. -XMLHttpRequest)))

(defn NodeXhrFactory []
  (this-as this (.call XmlHttpFactory this)))

(goog/inherits NodeXhrFactory XmlHttpFactory)

(set!
 (.. NodeXhrFactory -prototype -createInstance)
 #(xml-http-request.))

(set!
 (.. NodeXhrFactory -prototype -internalGetOptions)
 (constantly #js {}))

(defn set-global-xhr-factory! []
  (.setGlobalFactory XmlHttp (NodeXhrFactory.)))

(defn unwrap-response [{:keys [body status] :as resp}]
  (if (<= 200 status 299)
    body
    (ex-info "HTTP error" resp)))

(def response-chan (partial async/chan 1 (map unwrap-response)))
