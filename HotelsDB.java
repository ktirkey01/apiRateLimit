import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;

public class HotelsDB {
	private static HotelsDB instance = null;
	private HashMap<String, ArrayList<Hotel>> cityToHotelMap;
	private HotelsDB() {
	    Properties prop = new Properties();

	    cityToHotelMap = new HashMap<String, ArrayList<Hotel>>();
	    try {
	    	FileInputStream in = new FileInputStream(HTTPService.confPath);
	    	prop.load(in);
	    	in.close();
		BufferedReader br = new BufferedReader(new FileReader(prop.getProperty("CSV_FILE_PATH")));
		String line = br.readLine();
		// skip column header
		line = br.readLine();
		while(line != null) {
		    String[] s = line.split(",");
		    String city = s[0];
		    if(cityToHotelMap.get(city) == null) {
			Hotel h = new Hotel(s[2], Integer.parseInt(s[3]));
			ArrayList<Hotel> l = new ArrayList<Hotel>();
			l.add(h);
			cityToHotelMap.put(city, l);
		    } else {
			ArrayList<Hotel> l = cityToHotelMap.get(city);
			l.add(new Hotel(s[2], Integer.parseInt(s[3])));
		    }
		    line = br.readLine();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	public static HotelsDB getInstance() {
	    if(instance == null) {
		synchronized(HotelsDB.class) {
		    if(instance == null) {
		    	instance = new HotelsDB();
		    }
		}
	    }
	    return instance;
	}

	public ArrayList<Hotel> getHotels(String cityId) {
		return cityToHotelMap.get(cityId);
	}
}
