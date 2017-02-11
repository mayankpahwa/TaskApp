(defproject taskapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
  				 [ring "1.4.0"]
  				 [compojure "1.3.4"]
  				 [mysql/mysql-connector-java "5.1.25"]
  				 [org.clojure/java.jdbc "0.3.5"]
           [hiccup "1.0.5"]
           [crypto-password "0.2.0"]]
  :main taskapp.core)