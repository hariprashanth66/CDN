/**
 * POJO class to store server instance details
 * @author Akshai Prabhu
 * @author Hari Prashanth
 *
 */
public class ServerTable {
	private String ip;
	private double latitude;
	private double longitute;
	private String location;
	
	public ServerTable() {
		ip = new String();
		location = new String();
		latitude = 0.0;
		longitute = 0.0;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitute() {
		return longitute;
	}

	public void setLongitute(double longitute) {
		this.longitute = longitute;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
