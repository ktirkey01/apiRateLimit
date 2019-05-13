import java.util.concurrent.atomic.AtomicLong;
import java.lang.Math;

// per APIKey rate limit
public class PerUserRateLimit {

	final private int numRequest; // allowed no. of api calls
	final private int timeInterval; // time window in seconds
	private volatile long tokenCount;
	private volatile long lastUpdate; // in seconds, token bucket update time

	public PerUserRateLimit(int numRequest, int timeInterval) {
		this.numRequest = numRequest;
		this.timeInterval = timeInterval;
		this.tokenCount = numRequest;
		this.lastUpdate = System.currentTimeMillis() / 1000;
	}

	public synchronized boolean isRateLimitExceeded() {

	    // update token bucket and check rate limit.
	    long currentTime = System.currentTimeMillis() / 1000;

	    long timeElapsed = currentTime - lastUpdate;
	    lastUpdate = currentTime;
	    float rate = (float)numRequest/timeInterval;
	    long refillValue = (long)(timeElapsed * rate);
	    tokenCount += refillValue;
	    System.out.println("PerUser: numRequest:" + numRequest + ", timeInterval:" + timeInterval + ", rate:" + rate + " timeElapsed: " + timeElapsed + ", refillValue: " + refillValue + ", tokenCount: " + tokenCount);
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
