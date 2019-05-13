Compiling:
	javac *.java
For starting server
	java HTTPService <path to app-conf.cfg file>
For stopping server
	ctrl+c
API call
	curl -H "APIKEY:123"  "http://localhost:8080/hotels/search?cityId=Bangkok"
	curl -H "APIKEY:123"  "http://localhost:8080/hotels/search?cityId=Bangkok&sortOrder=DESC"
	curl -H "APIKEY:123"  "http://localhost:8080/hotels/search?cityId=Bangkok&sortOrder=ASCC"
