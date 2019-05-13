import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.Properties;

public class HTTPService {
	
	public static String confPath;

	public static void main(String[] args) {
    
	    if (args.length != 1) {
		System.out.println("Usage: javac HTTPService <path to conf file>");
		System.exit(0);
	    }

	    confPath = args[0];
	    Properties appProperty = new Properties();
	
	    try {
	    	FileInputStream in = new FileInputStream(confPath);
	    	appProperty.load(in);
	    	in.close();

		InetSocketAddress serverAddr = new InetSocketAddress(Integer.parseInt(appProperty.getProperty("SERVER_PORT")));
    		HttpServer server = HttpServer.create(serverAddr, Integer.parseInt(appProperty.getProperty("SERVER_BACKLOG")));

		String ctxPath = appProperty.getProperty("CONTEXT_PATH");
    		server.createContext(ctxPath, new HotelsSearchHandler());
    		server.setExecutor(Executors.newFixedThreadPool(Integer.parseInt(appProperty.getProperty("NUM_THREADS"))));
    		server.start();
    		System.out.println("HTTP server started." );

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
    			public void run() {
       			    System.out.println("Shutting down.");
    			}
 		});
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
}
