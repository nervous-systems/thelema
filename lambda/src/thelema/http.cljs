(ns thelema.http
  (:import [goog.net XmlHttp XmlHttpFactory])
  (:require [cljs.nodejs :as nodejs]))

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
