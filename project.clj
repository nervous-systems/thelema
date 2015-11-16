(defproject thelema "0.1.0-SNAPSHOT"
  :license {:name "Unlicense" :url "http://unlicense.org/UNLICENSE"}
  :source-paths ["frontend" "lambda"]
  :dependencies [[org.clojure/clojure        "1.7.0"]
                 [org.clojure/core.async     "0.2.374"]
                 [org.clojure/clojurescript  "1.7.170"]
                 [io.nervous/eulalie     "0.6.3"]
                 [io.nervous/cljs-lambda "0.1.2"]
                 [camel-snake-kebab      "0.3.2"]

                 [cljsjs/react  "0.14.0-1"]
                 [reagent       "0.5.1" :exclusions [cljsjs/react]]
                 [secretary     "1.2.3"]
                 [cljs-http     "0.1.37"]]
  :exclusions [org.clojure/clojure]
  :plugins [[lein-npm "0.6.0"]
            [io.nervous/lein-cljs-lambda "0.2.4"]
            [lein-cljsbuild "1.1.1-SNAPSHOT"]
            [lein-figwheel "0.5.0-1"]]
  :npm {:dependencies [[ytdl-core "0.7.6"]
                       [xmlhttprequest "1.8.0"]]}
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/thelema.js"
                                    :target-path]
  :cljs-lambda
  {:cljs-build-id "lambda-prod"
   :defaults
   {:role "arn:aws:iam::510355070671:role/cljs-lambda-default"
    :create true
    :timeout 60}
   :functions
   [{:name   "audio-search"
     :invoke thelema.lambda/audio-search}]}
  :cljsbuild
  {:builds [{:id "frontend-dev"
             :source-paths ["frontend"]
             :figwheel {:on-jsload "thelema.core/mount-root"}
             :compiler {:output-to "resources/public/js/compiled/thelema.js"
                        :output-dir "resources/public/js/compiled/out-dev"
                        :asset-path "js/compiled/out-dev"
                        :main thelema.core
                        :optimizations :none
                        :source-map true}}
            {:id "lambda-dev"
             :source-paths ["lambda/src"]
             :compiler {:output-to "target/dev/thelema.js"
                        :output-dir "target/dev"
                        :target :nodejs
                        :optimizations :none
                        :source-map true}}
            {:id "lambda-prod"
             :source-paths ["lambda/src"]
             :compiler {:output-to "target/prod/thelema.js"
                        :output-dir "target/prod"
                        :target :nodejs
                        :optimizations :advanced}}
            {:id "lambda-test-dev"
             :source-paths ["lambda/src" "lambda/test"]
             :notify-command ["node" "target/test-dev/thelema-test.js"]
             :compiler {:output-to "target/test-dev/thelema-test.js"
                        :output-dir "target/test-dev"
                        :target :nodejs
                        :optimizations :none
                        :main "thelema.test.runner"}}]}
  :figwheel {:css-dirs ["resources/public/css"]}
  :profiles {:dev
             {:repl-options
              {:nrepl-middleware
               [cemerick.piggieback/wrap-cljs-repl]}
              :dependencies
              [[com.cemerick/piggieback "0.2.1"]
               [org.clojure/tools.nrepl "0.2.12"]
               [figwheel "0.5.0-1"]]
              :source-paths ["lambda/src" "lambda/test" "frontend"]}})
