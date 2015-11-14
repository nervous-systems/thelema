(ns thelema.util
  (:require [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [clojure.string :as str]
            #?@ (:clj
                 [[clojure.core.async :as async]
                  [clojure.pprint :as pprint]]
                 :cljs
                 [[cljs.core.async :as async]
                  [cljs.pprint :as pprint]
                  [cljs.nodejs :as nodejs]]))
  #? (:cljs (:require-macros [thelema.util])))

#? (:clj
    (defmacro compile-env [env-var]
      (get (System/getenv) env-var)))

(def ->kebab-map (partial transform-keys csk/->kebab-case-keyword))
(def ->camel-map (partial transform-keys csk/->camelCaseKeyword))

(def indexed (partial map-indexed vector))

(defn channel-error [e chan & [close?]]
  (async/put! chan e)
  (when (or close? (nil? close?))
    (async/close! chan)))

#? (:cljs (def clean-map (comp ->kebab-map js->clj)))
#? (:cljs (def json->map #(->> % (.parse js/JSON) clean-map)))

(defn pprint-str [x]
  (str/trimr (with-out-str (pprint/pprint x))))

(defn log [& args]
  (apply
   println
   (map #(cond-> % (not (string? %)) pprint-str) args)))