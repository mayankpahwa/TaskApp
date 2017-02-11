(ns taskapp.core
	(:require [ring.adapter.jetty :as jetty]
			  [ring.util.response :as response]
			  [compojure.core :refer [defroutes GET POST]]
              [compojure.route :refer [not-found resources]]
              [database :as db]))

(use 'hiccup.core)
(use 'ring.middleware.params
	 'ring.middleware.session)

(defn home
	[request]
	(html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
		  [:ul [:li [:a {:href "#"} "Task App"]]] 
		  [:a {:href "/login" :class "button"} "Login"] [:br] [:a {:href "/signup" :class "button"} "Signup"]))

(defn login
	[request]
	(html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
		  [:ul [:li [:a {:href "#"} "Task App"]]]
		  [:h1 "Log In"] 
		  [:form {:method "post" :action "/login"}
			"Email Id:" [:br]
			[:input {:type "email" :name "email" :required "yes"}] [:br]
			"Password:" [:br]
			[:input {:type "password" :name "password" :required "yes"}] [:br]
			[:input {:type "submit" :value "Enter"}]]))

(defn signup
	[request]
	(html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
		[:ul [:li [:a {:href "#"} "Task App"]]]
		[:h1 "Get Started"] 
		[:form {:method "post" :action "/signup"}
	"First Name:" [:br]
	[:input {:type "text" :name "firstname" :required "yes"}] [:br]
	"Last Name:" [:br]
	[:input {:type "text" :name "lastname" :required "yes"}] [:br]
	"Email ID:" [:br]
	[:input {:type "email" :name "email" :required "yes"}] [:br]
	"Password" [:br]
	[:input {:type "password" :name "password" :required "yes"}] [:br]
	[:input {:type "submit" :value "Sign Up"}]]))


(defn handler-signup
	[request]
	(let [x (db/signup (vals (:params request)))]
		(if (empty? x)
			(response/redirect "/signup")
			(response/redirect "/login"))))

(defn handler-login
	[request]
	(let [x (db/login (vals (:params request)))]
		(if (empty? x)
			(response/redirect "/login")
			(assoc (response/redirect "/dashboard")
				 :session (nth (vals (:params request)) 0)))))

(defn render-html [body]
		(html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
			[:ul [:li [:a {:href "#"} "Task App"]]
				 [:li [:a {:href "/logout" :style "float:right"} "Log Out"]]
				 [:li [:a {:href "/updateuser" :style "float:right"} "Update User"]]]
			[:table {:align "center"}
				  [:tr
				  	[:th "Taskname"]
				  	[:th "Task Details"]
				  	[:th "Assigned By"]
				  	[:th "Due Date"]
				  	[:th "Status"]
				  	[:th "Change Status"]
				  	[:th "Delete Task"]]
				(for [x (range 0 (count body))]
					(let [row (nth body x)]
						[:tr
				  			[:td (:task_name row)]
				  			[:td (:task_description row)]
				  			[:td (let [fullname (db/userbyid (db/getemailid (:assigned_by_id row)))] 
				  					(str (first fullname) " " (last fullname)))]
				  			[:td (:due_date row)]
				  			[:td (:status row)]
				  			[:td [:a {:href (str "/updatetask/" (:task_id row)) :class "updatebutton"}  "Update Task"]]
				  			[:td [:a {:href (str "/dashboard" "?key=" (:task_id row)) :class "deletebutton"}  "Delete Task"]]]))] 
				[:a {:href "/create" :class "button"}  "Create Task"]))

(defn dashboard
  "A ring handler to handle dashboard"
  [request]
  (if (empty? (:session request))
  	  (html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
  	  		[:ul [:li [:a {:href "#"} "Task App"]]]
  	  		[:h2 "Cannot view dashboard without signing in"] 
  	  		[:a {:href "/login" :class "button"} "Login"]
			[:a {:href "/signup" :class "button"} "Signup"])
  	  (if (not (empty? (:query-params request)))
  	  	  (let [x (db/deletetask (vals (:query-params request)))]
  	  	  		(assoc (response/redirect "/dashboard") :query-params {}))
  	  	  (render-html (db/showtasks (:session request))))))

(defn updateuser
	[request]
	(if (empty? (:session request))
		(response/redirect "/")
		(let [details (db/userbyid (:session request))]
			 (html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
				  [:ul [:li [:a {:href "/dashboard"} "Task App"]]]
			[:form {:method "post" :action "/updateuser"}
			"First Name:" [:br]
			[:input {:type "text" :name "firstname" :value (first details) :required "yes"}] [:br]
			"Last Name:" [:br]
			[:input {:type "text" :name "lastname" :value (second details) :required "yes"}] [:br]
			[:input {:type "submit" :value "Update"}]]))))

(defn handler-updateuser
	[request]
	(let [x (db/updateuser (vals (:params request)) (:session request))]
		(if (empty? x)
			(response/redirect "/updateuser")
			(response/redirect "/dashboard"))))

(defn create
	[request]
	(if (empty? (:session request))
		(response/redirect "/")
		(html [:head [:link {:rel "stylesheet" :href "styles.css"}]]
			[:ul [:li [:a {:href "/dashboard"} "Task App"]]
				 [:li [:a {:href "/logout" :style "float:right"} "Log Out"]]
				 [:li [:a {:href "/updateuser" :style "float:right"} "Update User"]]]
		[:form {:method "post" :action "/create"}
		"Assign to:" [:br]
		[:select {:name "id"}
			(let [body (db/allusers)]
				(for [x (range 0 (count body))]
						(let [row (nth body x)]
							[:option {:value (:emailid row)}
				  				(str (:firstname row) " " (:lastname row))])))] [:br]
		"Task Name:" [:br]
		[:input {:type "text" :name "taskname" :required "yes"}] [:br]
		"Task Details:" [:br]
		[:input {:type "text" :name "taskdetails" :required "yes"}] [:br]
		"Due Date" [:br]
		[:input {:type "text" :name "date" :placeholder "YYYY-MM-DD Format" :required "yes"}] [:br]
		"Status" [:br]
		[:select {:name "status"} [:option "Not Started"] [:option "In Progress"] [:option "Completed"]] [:br]
		[:input {:type "submit" :value "Submit"}]])))

(defn handler-create
	[request]
	(if (empty? (:session request))
		(response/redirect "/")
		(let [x (db/inserttask (vals (:params request)) (:session request))]
			(if (empty? x)
				(response/redirect "/create")
				(response/redirect "/dashboard")))))

(defn updatetask
	[request]
	(if (empty? (:session request))
		(response/redirect "/")
		(html [:head [:link {:rel "stylesheet" :href "../styles.css"}]]
			[:ul [:li [:a {:href "/dashboard"} "Task App"]]
				 [:li [:a {:href "/logout" :style "float:right"} "Log Out"]]
				 [:li [:a {:href "/updateuser" :style "float:right"} "Update User"]]]
			[:form {:method "post" :action (str "/updatetask/" (:id (:params request)))}
			[:input {:type "radio" :name "status" :value "Not Started" :checked "yes"} "Not Started"] [:br]
			[:input {:type "radio" :name "status" :value "In Progress"} "In Progress"] [:br]
			[:input {:type "radio" :name "status" :value "Completed"} "Completed"] [:br]
			[:input {:type "submit" :value "Update Status"}]])))

(defn handler-updatetask
	[request]
	(if (empty? (:session request))
		(response/redirect "/")
		(let [x (db/updatetask (vals (:params request)))]
			(response/redirect "/dashboard"))))

(defn logout
	[request]
	(assoc (response/redirect "/")
				 :session nil))

(defroutes handler
  (GET "/" [] home)
  (GET "/login" [] login)
  (POST "/login" [] handler-login)
  (GET "/signup" [] signup)
  (POST "/signup" [] handler-signup)
  (GET "/dashboard" [] dashboard)
  (GET "/create" [] create)
  (POST "/create" [] handler-create)
  (GET "/logout" [] logout)
  (GET "/updateuser" [] updateuser)
  (POST "/updateuser" [] handler-updateuser)
  (GET "/updatetask/:id" [] updatetask)
  (POST "/updatetask/:id" [] handler-updatetask)
  (resources "/")
  (not-found "<h1>This is not the page you are looking for</h1> 
              <p>Sorry, the page you requested was not found!</p>"))

(def app
  (wrap-session (wrap-params handler)))

(defn -main
  []
  (jetty/run-jetty app {:port 3030}))