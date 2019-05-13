public class Hotel implements Comparable<Hotel> {
	String room;
	int price;

	public Hotel(String room, int price) {
		//this.hotelId = hotelId;
		this.room = room;
		this.price = price;
	}

	/*
	public int getHotelId() {
		return hotelId;
	}

	public void setHotelId(int hotelId) {
		this.hotelId = hotelId;
	}
	*/

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public int compareTo(Hotel h) {
		return this.price - h.price;
	}

	@Override 
	public String toString() {
		return "[" + room + "," + price + "]";
	}
}
