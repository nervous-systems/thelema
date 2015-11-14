(defproject thelema "0.1.0-SNAPSHOT"
  :license {:name "Unlicense" :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[org.clojure/clojure        "1.7.0-beta2"]
                 [org.clojure/core.async     "0.2.371"]
                 [org.clojure/clojurescript  "1.7.170"]
                 [io.nervous/eulalie     "0.6.3"]
                 [io.nervous/cljs-lambda "0.1.2"]
                 [camel-snake-kebab      "0.3.2"]]
  :plugins [[lein-npm "0.6.0"]
            [io.nervous/lein-cljs-lambda "0.2.4"]
            [lein-cljsbuild "1.1.1"]]
  :npm {:dependencies [[ytdl-core "0.7.6"]]}
  :cljs-lambda
  {:cljs-build-id "dev"
   :defaults
   {:role "arn:aws:iam::151963828411:role/cljs-lambda-default"
    :create true
    :timeout 60}
   :functions
   [{:name   "audio-search"
     :invoke thelema.lambda/audio-search}]}
  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src"]
             :compiler {:output-to "target/dev/thelema.js"
                        :output-dir "target/dev"
                        :target :nodejs
                        :optimizations :none
                        :source-map true}}]}
  :profiles {:dev
             {:repl-options
              {:nrepl-middleware
               [cemerick.piggieback/wrap-cljs-repl]}
              :dependencies
              [[com.cemerick/piggieback "0.2.1"]
               [org.clojure/tools.nrepl "0.2.10"]]}
             :source-paths ["src" "test"]})
