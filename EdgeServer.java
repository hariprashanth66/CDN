import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class is an edge server instance
 * @author Akshai Prabhu
 * @author Hari Prashanth
 *
 */
public class EdgeServer {

	public static Receiver receiver;
	public static FileReceiver fileReceiver;
	public static FileReceiver setupFileReceiver;
	public static Sender sender;
	public static List<Servers> peerServers;
	public static String mainServerIP;
	public static String requestedFileName;
	public static String currentClientIP;
	public static List<Servers> servers;

	/**
	 * Constructor
	 */
	public EdgeServer() {
		mainServerIP = "54.191.25.222";
		requestedFileName = new String();
		currentClientIP = new String();

		servers = new ArrayList<Servers>();
		Servers temp = new Servers();

		temp.setId(0);
		temp.setLocation("East-0");
		temp.setIp("52.91.70.190");
		temp.setPvt_ip("172.31.57.235");
		servers.add(temp);
		temp = new Servers();

		temp.setId(1);
		temp.setLocation("East-1");
		temp.setIp("54.235.226.19");
		temp.setPvt_ip("172.31.50.73");
		servers.add(temp);

		temp = new Servers();
		temp.setId(2);
		temp.setLocation("East-2");
		temp.setIp("54.174.2.213");
		temp.setPvt_ip("172.31.51.176");
		servers.add(temp);

		temp = new Servers();
		temp.setId(0);
		temp.setLocation("West-0");
		temp.setIp("54.153.114.239");
		temp.setPvt_ip("172.31.1.91");
		servers.add(temp);

		temp = new Servers();
		temp.setId(1);
		temp.setLocation("West-1");
		temp.setIp("52.53.189.114");
		temp.setPvt_ip("172.31.12.251");
		servers.add(temp);

		temp = new Servers();
		temp.setId(2);
		temp.setLocation("West-2");
		temp.setIp("54.67.16.150");
		temp.setPvt_ip("172.31.0.24");
		servers.add(temp);
	}

	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		EdgeServer edgeServer = new EdgeServer();
		receiver = edgeServer.new Receiver(40000);
		fileReceiver = edgeServer.new FileReceiver(50000);
		setupFileReceiver = edgeServer.new FileReceiver(55000);
		receiver.start();
		fileReceiver.start();
		setupFileReceiver.start();
		sender = edgeServer.new Sender();
		System.out.println("Edge server started.\n");
	}

	/**
	 * Sender inner class to send messages
	 * @author Akshai Prabhu
	 * @author Hari Prashanth
	 *
	 */
	class Sender {

		/**
		 * To send a message to another server
		 * @param ip
		 * @param port
		 * @param message
		 */
		public void sendMessage(String ip, int port, String message) {
			Socket socket;
			try {
				if (message.contains("Timestamp")) {
					System.out.println("Sending TimeStamp to Main Server");
					System.out.println();
				}
				socket = new Socket(ip, port);
				OutputStream outToServer = socket.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF(message);
				socket.close();
			} catch (IOException e) {
				if (ip.equals(mainServerIP)) {
					System.err.println("Main Server failure!!! \n\nSending the currently available file\n");
					if (requestedFileName.contains("part0")) {
						sender.sendMessage(currentClientIP, 41000, "FileName from edge: " + requestedFileName);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {

						}
						sender.sendFileRequest(1, servers.get(1).getIp(), 40000,
								"Send File= " + requestedFileName.substring(0, requestedFileName.length() - 1)
										+ "1, IP: " + currentClientIP);
						sender.sendFileRequest(2, servers.get(2).getIp(), 40000,
								"Send File= " + requestedFileName.substring(0, requestedFileName.length() - 1)
										+ "2, IP: " + currentClientIP);
						sender.sendFile(currentClientIP, 60000, new File("local/" + requestedFileName));
					} else {
						sender.sendMessage(currentClientIP, 40000, "FileName from edge: " + requestedFileName);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {

						}
						sender.sendFile(currentClientIP, 50000, new File("local/" + requestedFileName));
					}
				}
			}
		}

		/**
		 * To get location of the server
		 * @param ip
		 * @return
		 */
		private String getLoc(String ip) {

			for (int i = 0; i < servers.size(); i++) {
				if (servers.get(i).getIp().equals(ip)) {
					return servers.get(i).getLocation();
				}
			}
			return null;
		}

		/**
		 * To send a file across the group
		 * @param ip
		 * @param port
		 * @param file
		 */
		public void sendFile(String ip, int port, File file) {
			Socket socket;
			try {
				socket = new Socket(ip, port);
				System.out.println("Sending file " + file.getName() + "to client");

				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);

				OutputStream os = socket.getOutputStream();

				byte byteArray[];
				long count = 0;

				while (count != file.length()) {
					int n = 1000000;
					if (file.length() - count >= n) {
						count += n;
					} else {
						n = (int) (file.length() - count);
						count = file.length();
					}
					byteArray = new byte[n];
					bis.read(byteArray, 0, n);
					os.write(byteArray);
				}
				os.flush();
				os.close();
				bis.close();
				socket.close();
			} catch (IOException e) {

			}
		}

		/**
		 * To send file request to other servers
		 * @param index
		 * @param ip
		 * @param port
		 * @param message
		 */
		public void sendFileRequest(int index, String ip, int port, String message) {
			Socket socket;
			try {
				System.out.println("Notifying " + getLoc(ip) + " server to send its part");
				System.out.println();
				socket = new Socket(ip, port);
				OutputStream outToServer = socket.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF(message);
				socket.close();
			} catch (IOException e) {
				if (index < 6) {
					System.err.println("Redirecting notification due to server failure.\n");
					if (index < 3) {

						sender.sendFileRequest(index + 3, servers.get(index + 3).getIp(), port, message);
					} else {
						sender.sendFileRequest(index - 3, servers.get(index - 3).getIp(), port, message);
					}
				} else {
					System.out.println("All Servers Failed");
				}
			}

		}

	}

	/**
	 * Receiver to receive messages from other servers
	 * @author Akshai Prabhu
	 * @author Hari Prashanth
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

					if (message.contains("Get Load")) {
						System.out.println("Load request from Client");
						System.out.println();
						currentClientIP = message.substring(message.indexOf(":") + 2);
						replyLoad();
					} else if (message.contains("FileName from Client")) {
						requestedFileName = message.substring(message.indexOf("=") + 2, message.indexOf(":")).trim();
						System.out.println("File Request from Client for " + requestedFileName + "\n");
						checkFileTimeStamp(message);
					} else if (message.contains("FileName from main")) {

						requestedFileName = message.substring(message.indexOf(":") + 2);
						
					} else if (message.contains("Not Modified main")) {
						requestedFileName = message.substring(message.indexOf(":") + 2);
						System.out.println("File " + requestedFileName + " up-to-date\n");
						if (requestedFileName.contains("part0")) {
							sender.sendMessage(currentClientIP, 41000, "FileName from edge: " + requestedFileName);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {

							}
							sender.sendFileRequest(1, servers.get(1).getIp(), 40000,
									"Send File= " + requestedFileName.substring(0, requestedFileName.length() - 1)
											+ "1, IP: " + currentClientIP);
							sender.sendFileRequest(2, servers.get(2).getIp(), 40000,
									"Send File= " + requestedFileName.substring(0, requestedFileName.length() - 1)
											+ "2, IP: " + currentClientIP);
							sender.sendFile(currentClientIP, 60000, new File("local/" + requestedFileName));
						} else {
							sender.sendMessage(currentClientIP, 40000, "FileName from edge: " + requestedFileName);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {

							}
							sender.sendFile(currentClientIP, 50000, new File("local/" + requestedFileName));
						}

					} else if (message.contains("Send File")) {
						System.out.println("Notification received.\n");
						requestedFileName = message.substring(message.indexOf("=") + 2, message.indexOf(",")).trim();
						currentClientIP = message.substring(message.indexOf(":") + 2);

						if (getMyId() == 1 || getMyId() == 4) {
							sender.sendMessage(currentClientIP, 42000, "FileName from edge: " + requestedFileName);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {

							}
							sender.sendFile(currentClientIP, 61000, new File("local/" + requestedFileName));
						} else if (getMyId() == 2 || getMyId() == 5) {
							sender.sendMessage(currentClientIP, 43000, "FileName from edge: " + requestedFileName);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {

							}
							sender.sendFile(currentClientIP, 62000, new File("local/" + requestedFileName));
						}
					}

				} catch (IOException e) {

				}

			}

		}

		/**
		 * To get he id of an edge server
		 * @return
		 */
		private int getMyId() {
			for (int i = 0; i < servers.size(); i++) {
				try {
					if (InetAddress.getLocalHost().getHostAddress().equals(servers.get(i).getPvt_ip())) {
						return servers.get(i).getId();
					}
				} catch (UnknownHostException e) {

				}
			}
			return 0;
		}

		/**
		 * Checks for recent timestamp
		 * @param message
		 */
		private void checkFileTimeStamp(String message) {

			File file = new File("local/" + requestedFileName);
			File filePart = new File("local/" + requestedFileName + "part0");
			if (file.exists()) {
				try {
					sender.sendMessage(mainServerIP, 40000, "Filename: " + requestedFileName + "; IP@ "
							+ InetAddress.getLocalHost().getHostAddress() + ", Timestamp= " + file.lastModified());

				} catch (UnknownHostException e1) {

				}
			} else if (filePart.exists()) {
				try {
					sender.sendMessage(mainServerIP, 40000, "Filename: " + requestedFileName + "part0; IP@ "
							+ InetAddress.getLocalHost().getHostAddress() + ", Timestamp= " + filePart.lastModified());

				} catch (UnknownHostException e1) {

				}
			} else {
				System.out.println("Informing Client, File not found!!!\n");
				sender.sendMessage(currentClientIP, 40000, "File not found!!!");
			}
		}

		/**
		 * Reply with load information
		 */
		private void replyLoad() {
			System.out.println("Load sent to Client\n");

			sender.sendMessage(currentClientIP, 40000, "Load: 50");
		}
	}

	/**
	 * To receive files from the main serve
	 * @author Akshai Prabhu
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
			serverListen();
		}

		/**
		 * Listen to file transfer port
		 */
		public void serverListen() {
			Socket socket;
			while (true) {
				try {
					fileServerSocket = new ServerSocket(port);
					socket = fileServerSocket.accept();
					file = new File("local/" + requestedFileName);
					System.out.println("Received file " + file.getName() + " from Main Server");
					System.out.println();
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
					if (port == 50000) {
						sender.sendMessage(currentClientIP, 40000, "FileName from edge: " + requestedFileName);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {

						}
						if (requestedFileName.contains("part0")) {
							sender.sendFileRequest(1, servers.get(1).getIp(), 40000,
									"Send File= " + requestedFileName.substring(0, requestedFileName.length() - 1)
											+ "1, IP: " + currentClientIP);
							sender.sendFileRequest(2, servers.get(2).getIp(), 40000,
									"Send File= " + requestedFileName.substring(0, requestedFileName.length() - 1)
											+ "2, IP: " + currentClientIP);
							sender.sendFile(currentClientIP, 60000, new File("local/" + requestedFileName));
						} else {
							sender.sendFile(currentClientIP, 50000, new File("local/" + requestedFileName));
						}
					}
				} catch (IOException e) {

				}
			}
		}
	}

}
