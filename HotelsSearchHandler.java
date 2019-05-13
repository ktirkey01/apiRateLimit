import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class HotelsSearchHandler implements HttpHandler {

	public static Properties prop;
	public static HashMap<String, PerUserRateLimit> apiKeyRateLimitMap;
	public static ConcurrentHashMap<String, Long> suspendedAPIKeyMap;

	static {
	    apiKeyRateLimitMap = new HashMap<String, PerUserRateLimit>();
	    suspendedAPIKeyMap = new ConcurrentHashMap<String, Long>();

            try {
		prop = new Properties();
                FileInputStream in = new FileInputStream(HTTPService.confPath);
                prop.load(in);
                in.close();
                BufferedReader br = new BufferedReader(new FileReader(prop.getProperty("PER_APIKEY_RATE_LIMIT_FILE")));
                String line = br.readLine();
		while(line != null) {
		    String[] keyStr = line.split("=");
		    String apiKey = keyStr[0];
		    int apiKeyRateLimit = Integer.parseInt(keyStr[1]);
		    apiKeyRateLimitMap.put(apiKey, new PerUserRateLimit(apiKeyRateLimit, 10));
		    line = br.readLine();
		}
            } catch (IOException e) {
                e.printStackTrace();
            }
	}

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
    		if (requestMethod.equalsIgnoreCase("GET")) {
      		    Headers requestHeaders = exchange.getRequestHeaders();
			
		    HashMap<String, String> queryParam = convertQueryStringToMap(exchange.getRequestURI().getQuery());	
		    String apiKey = requestHeaders.getFirst("APIKEY");
		    String cityId = queryParam.get("cityId");
		    String sortOrder = queryParam.get("sortOrder");

		    String response = "";
		    boolean isSuspended = false;

		    // check if suspended
		    Long suspendedTimestamp = suspendedAPIKeyMap.get(apiKey);
		    if (suspendedTimestamp != null) {
			if ((System.currentTimeMillis() - suspendedTimestamp) > 300*1000) {
			    suspendedAPIKeyMap.remove(apiKey);
			    System.out.println("removed key from susended list");
			} else {
			    isSuspended = true;
			    response = "API limit exceeded. Please try after 5 mins.";
      			    Headers responseHeaders = exchange.getResponseHeaders();
      			    responseHeaders.set("Content-Type", "application/text");
      			    exchange.sendResponseHeaders(429, response.getBytes().length);
			    System.out.println("key is still in susended list");
		   	}
		    }

		boolean isLimitExceeded = false;
		if (isSuspended == false) {
		    PerUserRateLimit apiKeyRateLimitObj = apiKeyRateLimitMap.get(apiKey);
		    if (apiKeyRateLimitObj == null) {
		        GlobalRateLimit globalRateLimitObj = GlobalRateLimit.getInstance();
		        isLimitExceeded = globalRateLimitObj.isRateLimitExceeded();
		        System.out.println("GlobalRateLimit: " + isLimitExceeded);
		    } else {
		        // if per API KEY rate limit present then update its token bucket and use this rate limit 
		        isLimitExceeded = apiKeyRateLimitObj.isRateLimitExceeded();
			System.out.println("PerUserRateLimit: " + isLimitExceeded);
		  	if (isLimitExceeded == false) {
			    // to update the global tocken bucket
			    boolean globalRateLimit = GlobalRateLimit.getInstance().isRateLimitExceeded();
			}
		    }
		 
	   	    if(isLimitExceeded == false) {
		        HotelsDB hdb = HotelsDB.getInstance();
			ArrayList<Hotel> l = null;
			if (cityId != null) {
				l = hdb.getHotels(cityId);
			}

			if (sortOrder != null) {
			    if (sortOrder.equals("ASC")) {
			        Collections.sort(l);
			    } else if (sortOrder.equals("DESC")) {
			        Collections.sort(l, Collections.reverseOrder());
			    }
			}	

			for (Hotel h: l) {
			    response += h.toString();
			}
      			Headers responseHeaders = exchange.getResponseHeaders();
      			responseHeaders.set("Content-Type", "application/text");
      			exchange.sendResponseHeaders(200, response.getBytes().length);

    		    } else {
			// suspend token and send retry response.
			suspendedAPIKeyMap.put(apiKey, System.currentTimeMillis());
			response = "API limit exceeded. Please try after 5 mins.";
      			Headers responseHeaders = exchange.getResponseHeaders();
      			responseHeaders.set("Content-Type", "application/text");
      			exchange.sendResponseHeaders(429, response.getBytes().length);
		    }
		}
      		OutputStream responseBody = exchange.getResponseBody();
        	responseBody.write(response.getBytes());
      		responseBody.close();
	    }
	}

	public static HashMap<String, String> convertQueryStringToMap(String queryStr) {
    	    HashMap<String, String> result = new HashMap<String, String>();
    	    for (String param : queryStr.split("&")) {
        	String pair[] = param.split("=");
        	if (pair.length>1) {
            	    result.put(pair[0], pair[1]);
        	}else{
            	    result.put(pair[0], "");
        	}
    	    }
    	    return result;
  	}
}
