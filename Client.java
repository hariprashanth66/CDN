import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Client to demonstrate the project
 * 
 * @author Akshai Prabhu
 * @author Hari Prashanth
 *
 */
public class Client {
	public static Receiver receiver;
	public static Receiver receiver0;
	public static Receiver receiver1;
	public static Receiver receiver2;
	public static FileReceiver fileReceiver;
	public static FileReceiver fileReceiver0;
	public static FileReceiver fileReceiver1;
	public static FileReceiver fileReceiver2;
	public static Sender sender;
	public static List<ServerTable> serverTableList;
	public static GetLocation latLng;
	public static double[] currentLatLong;
	public static String myEdgeServer;
	public static int serverSetFlag;
	public static String fileName;
	public static String receivingFileName;
	public static String receivingFileName0;
	public static String receivingFileName1;
	public static String receivingFileName2;
	public static int count;
	public static int timeout;

	/**
	 * Constructor
	 */
	public Client() {
		count = 0;
		serverSetFlag = 0;
		timeout = 0;
		receivingFileName = new String();
		receivingFileName0 = new String();
		receivingFileName1 = new String();
		receivingFileName2 = new String();
		serverTableList = new ArrayList<ServerTable>();
		ServerTable temp = new ServerTable();
		temp = new ServerTable();
		// east0
		temp.setIp("52.91.70.190");
		temp.setLatitude(37.4783967);
		temp.setLongitute(-76.4530772);
		temp.setLocation("East server");
		serverTableList.add(temp);
		temp = new ServerTable();
		// west0
		temp.setIp("54.153.114.239");
		temp.setLatitude(38.8375215);
		temp.setLongitute(-120.8958242);
		temp.setLocation("West server");
		currentLatLong = new double[2];
		myEdgeServer = new String();
		latLng = new GetLocation();
		serverTableList.add(temp);
	}

	/**
	 * To update the count of file split received
	 */
	public void updateCount() {
		synchronized (this) {
			count++;
			if (count == 3) {
				try {

					JoinFile joinFile = new JoinFile(
							receivingFileName0.substring(0, receivingFileName0.indexOf("part")));

					File folder = new File(InetAddress.getLocalHost().getHostName().toString() + "/");
					File[] fileList = folder.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						if (fileList[i].getName().contains("part")) {
							fileList[i].delete();
						}
					}
					count = 0;
					System.out.println(
							"Received file: " + receivingFileName0.substring(0, receivingFileName0.indexOf("part")));
					System.out.println("***************************************************************************");
					System.out.println("\n");
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Client client = new Client();
		receiver = client.new Receiver(40000);
		receiver.start();
		receiver0 = client.new Receiver(41000);
		receiver0.start();
		receiver1 = client.new Receiver(42000);
		receiver1.start();
		receiver2 = client.new Receiver(43000);
		receiver2.start();
		fileReceiver = client.new FileReceiver(50000);
		fileReceiver.start();
		fileReceiver0 = client.new FileReceiver(60000);
		fileReceiver0.start();
		fileReceiver1 = client.new FileReceiver(61000);
		fileReceiver1.start();
		fileReceiver2 = client.new FileReceiver(62000);
		fileReceiver2.start();
		sender = client.new Sender();
		sender.start();
		System.out.println("Client started.\n");
	}

	/**
	 * The sender is used to send messages and find location of the server.
	 * 
	 * @author Akshai Prabhu
	 * @author Hari Prashanth
	 *
	 */
	class Sender extends Thread {

		public void run() {
			while (true) {
				System.out.println("***************************************************************************");
				System.out.println("Enter filename: ");
				Scanner sc = new Scanner(System.in);
				fileName = sc.nextLine();
				try {
					currentLatLong = latLng.returnLatLong(InetAddress.getLocalHost().getHostName());
				} catch (Exception e) {

				}
				myEdgeServer = sender.getServerToSend(fileName);

				try {
					sender.sendMessage(myEdgeServer, 40000, "Get Load: " + InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {

				}

			}
		}

		/**
		 * Get the nearest server
		 * 
		 * @param fileName
		 * @return
		 */
		public String getServerToSend(String fileName) {
			double min = Double.MAX_VALUE;
			myEdgeServer = new String();
			for (int i = 0; i < serverTableList.size(); i++) {
				double distance = getDistance(serverTableList.get(i));
				if (min > distance) {
					myEdgeServer = serverTableList.get(i).getIp();
					min = distance;
				}
			}
			System.out.println(getLoc(myEdgeServer) + " is the nearest server!\n");
			return myEdgeServer;
		}

		/**
		 * To send message to servers in th network
		 * 
		 * @param ip
		 * @param port
		 * @param message
		 */
		public void sendMessage(String ip, int port, String message) {
			Socket socket;
			try {
				if (message.contains("FileName")) {

					System.out.println("Sending File Request to " + getLoc(ip));
				}
				socket = new Socket(ip, port);
				OutputStream outToServer = socket.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF(message);
				socket.close();

			} catch (ConnectException e) {
				try {
					if (myEdgeServer.equals(serverTableList.get(1).getIp())) {
						myEdgeServer = serverTableList.get(0).getIp();
						sender.sendMessage(serverTableList.get(0).getIp(), 40000,
								"Get Load: " + InetAddress.getLocalHost().getHostAddress());
					} else {
						myEdgeServer = serverTableList.get(1).getIp();
						sender.sendMessage(serverTableList.get(1).getIp(), 40000,
								"Get Load: " + InetAddress.getLocalHost().getHostAddress());
					}
				} catch (UnknownHostException e1) {

				}
			} catch (IOException e) {
			}
		}

		/**
		 * Get the location of the servers
		 * 
		 * @param ip
		 * @return
		 */
		private String getLoc(String ip) {

			for (int i = 0; i < serverTableList.size(); i++) {
				if (serverTableList.get(i).getIp().equals(ip)) {
					return serverTableList.get(i).getLocation();
				}
			}
			return null;
		}

		/**
		 * Calculate distance between two server
		 * 
		 * @param serverTable
		 * @return
		 */
		private double getDistance(ServerTable serverTable) {
			return Math.pow(serverTable.getLatitude() - currentLatLong[0], 2)
					+ Math.pow(serverTable.getLongitute() - currentLatLong[1], 2);
		}

		/**
		 * To return the second nearest route
		 * 
		 * @return
		 */
		public String getNextClosest() {
			double min = Double.MAX_VALUE;
			for (int i = 0; i < serverTableList.size(); i++) {
				double distance = getDistance(serverTableList.get(i));
				if (min > distance && !serverTableList.get(i).getIp().equals(myEdgeServer)) {
					myEdgeServer = serverTableList.get(i).getIp();
					min = distance;
				}
			}
			return myEdgeServer;
		}

	}

	/**
	 * Receive messages from other servers
	 * 
	 * @author AkshaiPrabhu
	 * @author Hari Prashant
	 *
	 */
	class Receiver extends Thread {
		ServerSocket messageServerSocket;
		int port;

		/**
		 * Constructor
		 */
		public Receiver() {

		}

		public Receiver(int port) {
			this.port = port;
		}

		/**
		 * Thread run method
		 */
		public void run() {
			Socket socket;
			String message = new String();
			while (true) {
				try {
					messageServerSocket = new ServerSocket(port);
					socket = messageServerSocket.accept();
					DataInputStream in = new DataInputStream(socket.getInputStream());
					message = in.readUTF();
					socket.close();
					messageServerSocket.close();

					if (message.contains("Load")) {
						checkLoad(message);
					} else if (message.contains("FileName from edge")) {
						if (port == 40000) {
							receivingFileName = message.substring(message.indexOf(":") + 2);
						} else if (port == 41000) {
							receivingFileName0 = message.substring(message.indexOf(":") + 2);
						} else if (port == 42000) {
							receivingFileName1 = message.substring(message.indexOf(":") + 2);
						} else if (port == 43000) {
							receivingFileName2 = message.substring(message.indexOf(":") + 2);
						}

					} else if (message.contains("File not found!!!")) {
						System.err.println("File not found in server!!!");
					}
				} catch (IOException e) {
				}

			}

		}

		/**
		 * To check the load on a server
		 * 
		 * @param message
		 */
		private void checkLoad(String message) {
			int load = Integer.parseInt(message.substring(message.indexOf(":") + 2));
			if (load < 100) {
				try {
					sender.sendMessage(myEdgeServer, 40000,
							"FileName from Client= " + fileName + ": " + InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {

				}
			} else {
				try {
					sender.sendMessage(serverTableList.get(1).getIp(), 40000,
							"FileName from Client= " + fileName + ": " + InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
				}
			}
		}
	}

	/**
	 * Receive file from other servers
	 * 
	 * @author AkshaiPrabhu
	 * @author Hari Prashant
	 *
	 */
	class FileReceiver extends Thread {
		ServerSocket fileServerSocket;
		File file;
		int port;

		/**
		 * Constructor
		 */
		public FileReceiver() {

		}

		public FileReceiver(int port) {
			this.port = port;
		}

		/**
		 * Thread run method
		 */
		public void run() {
			Socket socket;
			while (true) {
				try {
					fileServerSocket = new ServerSocket(port);
					socket = fileServerSocket.accept();

					if (port == 50000) {
						file = new File(InetAddress.getLocalHost().getHostName().toString() + "/" + receivingFileName);
						System.out.println("Received file: " + file.getName());
						System.out
								.println("***************************************************************************");
						System.out.println("\n");
					} else if (port == 60000) {
						file = new File(InetAddress.getLocalHost().getHostName().toString() + "/" + receivingFileName0);
					} else if (port == 61000) {
						file = new File(InetAddress.getLocalHost().getHostName().toString() + "/" + receivingFileName1);
					} else if (port == 62000) {
						file = new File(InetAddress.getLocalHost().getHostName().toString() + "/" + receivingFileName2);
					}

					byte[] byteArray = new byte[1000000];
					FileOutputStream fos = new FileOutputStream(file);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					InputStream is = socket.getInputStream();

					int count = 0;

					while ((count = is.read(byteArray)) != -1) {
						bos.write(byteArray, 0, count);
					}
					bos.flush();
					bos.close();
					fos.close();
					is.close();
					socket.close();
					fileServerSocket.close();
					if (port == 61000 || port == 62000 || port == 60000) {
						updateCount();
					}
				} catch (IOException e) {
				}
			}
		}
	}

}
