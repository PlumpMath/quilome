(defproject eu.cassiel/quilome "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[quil "1.7.0-SNAPSHOT"]
                 [net.loadbang/net.loadbang.shado "2.1.0"]
                 [net.loadbang/net.loadbang.osc "1.5.0"]
                 ;; Hack because we have to manually install some Maven entries:
                 [net.loadbang/net.loadbang.lib "1.9.0"]
                 [org.clojure/clojure "1.5.1"]])
