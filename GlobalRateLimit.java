import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.Math;

public class GlobalRateLimit {

	private static GlobalRateLimit instance;

        final private int numRequest; // allowed no. of api calls
        final private int timeInterval; // time window in seconds
        private volatile long tokenCount;
        private volatile long lastUpdate; // in seconds, token bucket last update time

        private GlobalRateLimit(int numRequest, int timeInterval) {
                this.numRequest = numRequest;
                this.timeInterval = timeInterval;
                tokenCount = numRequest;
                lastUpdate = System.currentTimeMillis() / 1000;
        }

	public static GlobalRateLimit getInstance() {
	    if (instance == null) {
		Properties prop = new Properties();
		try {
		FileInputStream in = new FileInputStream(HTTPService.confPath);
                prop.load(in);
                in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
                int globalNumRequest = Integer.parseInt(prop.getProperty("GLOBAL_NUM_REQUESTS"));
		int globalTimeInterval = Integer.parseInt(prop.getProperty("GLOBAL_TIME_INTERVAL"));
	        synchronized(GlobalRateLimit.class) {
		    if (instance == null) {
		        instance = new GlobalRateLimit(globalNumRequest, globalTimeInterval);
		    }
		}
	    }
	    return instance;
	}

	public synchronized boolean isRateLimitExceeded() {

	    // update token bucket and check rate limit exceeded.
            long currentTime = System.currentTimeMillis() / 1000;

            long timeElapsed = currentTime - lastUpdate;
	    lastUpdate = currentTime;
	    float rate = (float)numRequest/timeInterval;
	    long refillValue = (long)(timeElapsed * rate);
            tokenCount += refillValue;
	    System.out.println("Global rate:" + rate +",timeElapsed: " + timeElapsed + ", refillValue: " + refillValue + ", tokenCount: " + tokenCount);
            if(tokenCount > numRequest) {
                tokenCount = numRequest;
            }

            boolean ret = false;
            if(tokenCount < 1) {
                ret = true;;
            } else {
                tokenCount--;
            }

            return ret;
	}
}
