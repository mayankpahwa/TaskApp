(ns database
  (:gen-class)
  (:require [clojure.java.jdbc :as jdbc]
  			[crypto.password.pbkdf2 :as pass]))

(def db-spec {:dbtype "mysql"
               :dbname "taskapp"
               :user "root"
               :password "#######"})

(defn signup
	[params]
	(let [id (nth params 2)
		  fname (nth params 0)
		  lname (nth params 1)
		  password (nth params 3)]
		  (let [fquery (jdbc/query db-spec ["SELECT * FROM users WHERE emailid = ?" id])]
		  		(if (empty? fquery)
		  			(jdbc/insert! db-spec :users {:emailid id :firstname fname :lastname lname :password (pass/encrypt password)})))))

(defn login
	[params]
	(let [id (nth params 0) 
		  password (nth params 1)]
		  (let [fquery (first (jdbc/query db-spec ["SELECT * FROM users WHERE emailid = ?" id]))]
		  		(if (empty? fquery)
		  			nil
		  			(if (pass/check password (:password fquery))
		  				"true")))))

(defn getuserid
	[emailid]
	(let [result (first (jdbc/query db-spec ["SELECT userid FROM users where emailid = ?" emailid]))]
		(:userid result)))

(defn userbyid
	[id]
	(let [details (first (jdbc/query db-spec ["SELECT firstname,lastname FROM users where emailid = ?" id]))]
		[(:firstname details) (:lastname details)]))

(defn getemailid
	[userid]
	(let [result (first (jdbc/query db-spec ["SELECT emailid FROM users where userid = ?" userid]))]
		(:emailid result)))

(defn showtasks
	[id]
	(jdbc/query db-spec ["SELECT * FROM tasks where Assigned_To_ID = ?" (getuserid id)]))

(defn inserttask
	[params id]
	(let [toid (nth params 0)
		  tname (nth params 1)
		  tdesc (nth params 2)
		  date (nth params 3)
		  status (nth params 4)]
		(jdbc/insert! db-spec :tasks {:Task_Name tname :Task_Description tdesc :Assigned_To_ID (getuserid toid) 
									  :Assigned_By_ID (getuserid id) :Due_Date date :Status status})))

(defn allusers
	[]
	(jdbc/query db-spec ["SELECT * FROM users"]))

(defn updateuser
	[params id]
	(jdbc/update! db-spec :users
			{:firstname (first params) :lastname (second params)}
            ["emailid = ?" id]))

(defn updatetask
	[params]
	(jdbc/update! db-spec :tasks
			{:status (first params)}
            ["Task_ID = ?" (second params)]))

(defn deletetask
	[id]
	(jdbc/delete! db-spec :tasks
            ["Task_ID = ?" (first id)]))