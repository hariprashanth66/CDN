/**
 * POJO class for server instances
 * @author Akshai Prabhu
 * @author Hari Prashanth
 *
 */
public class Servers {
	private int id;
	private String location;
	private String ip;
	private String pvt_ip;

	public Servers() {
		id = 0;
		location = new String();
		ip = new String();
		pvt_ip = new String();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPvt_ip() {
		return pvt_ip;
	}

	public void setPvt_ip(String pvt_ip) {
		this.pvt_ip = pvt_ip;
	}
}
