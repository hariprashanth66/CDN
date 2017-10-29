import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The class is an instance main server
 * @author Akshai Prabhu
 * @author Hari Prashanth
 *
 */
public class MainServer {

	public static Receiver receiver;

	public static Sender sender;
	public static List<Servers> servers;

	/**
	 * Constructor
	 */
	public MainServer() {
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
		MainServer mainServer = new MainServer();
		receiver = mainServer.new Receiver(40000);
		receiver.start();
		sender = mainServer.new Sender();
		System.out.println("Main Server Started.\n");
		System.out.println("Files being dispatched to respective Edge servers.\n");
		File folder = new File("local/");
		File[] fileList = folder.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].length() > 30000000) {
				try {
					SplitFile splitFile = new SplitFile(fileList[i].getName());
					for (int j = 0; j < 6; j++) {
						sender.sendMessage(servers.get(j).getIp(), 40000,
								"FileName from main: " + fileList[i].getName() + "part" + j % 3);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						sender.sendFile(servers.get(j).getIp(), 55000,
								new File("local/" + fileList[i].getName() + "part" + j % 3));
					}
				} catch (Exception e) {
				}
			} else {
				for (int j = 0; j < 6; j++) {
					if (j == 0 || j == 3) {
						sender.sendMessage(servers.get(j).getIp(), 40000,
								"FileName from main: " + fileList[i].getName());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						sender.sendFile(servers.get(j).getIp(), 55000, new File("local/" + fileList[i].getName()));
					}
				}

			}
		}
	}

	/**
	 * Given private ip find public ip
	 * @param privateIp
	 * @return
	 */
	public String findPublicIp(String privateIp) {
		for (int i = 0; i < 6; i++) {
			if (servers.get(i).getPvt_ip().equals(privateIp)) {
				return servers.get(i).getIp();
			}
		}

		return "";
	}

	/**
	 * Sender inner class to send message
	 * @author Akshai Prabhu
	 * \@author Hari Prashanth
	 *
	 */
	class Sender {

		public void sendMessage(String ip, int port, String message) {
			Socket socket;
			try {

				socket = new Socket(ip, port);
				OutputStream outToServer = socket.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF(message);
				socket.close();
			} catch (IOException e) {
			}
		}

		/**
		 * To get location of a server
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
		 * To send your file to another server
		 * @param ip
		 * @param port
		 * @param file
		 */
		public void sendFile(String ip, int port, File file) {
			Socket socket;
			try {
				socket = new Socket(ip, port);
				System.out.println("Sending file " + file.getName() + " to " + getLoc(ip));
				System.out.println();
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);

				OutputStream os = socket.getOutputStream();

				byte byteArray[];
				long count = 0;

				while (count != file.length()) {
					int n = 10000;
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
		 * Thread main method
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

					if (message.contains("Timestamp")) {
						System.out.println("Received TimeStamp request.\n");
						checkTimeStamp(message);
					}
				} catch (IOException e) {
				}

			}

		}

		/**
		 * To check time stamp of a file in the server
		 * @param message
		 */
		private void checkTimeStamp(String message) {
			String fileName = message.substring(message.indexOf(":") + 2, message.indexOf(";"));
			long timeStamp = Long.parseLong(message.substring(message.indexOf("=") + 2));
			String ip = message.substring(message.indexOf("@") + 2, message.indexOf(","));

			File file = new File("local/" + fileName);
			File file1 = new File("local/" + fileName.substring(0, fileName.length() - 1) + "1");
			File file2 = new File("local/" + fileName.substring(0, fileName.length() - 1) + "2");
			long mainTimeStamp = file.lastModified();
			if (mainTimeStamp > timeStamp) {
				System.out.println("Files in Edge server not up-to-date.\n");
				System.out.println("Sending updated file to Edge servers.\n");
				if (fileName.contains("part")) {
					// part0
					sender.sendMessage(servers.get(0).getIp(), 40000, "FileName from main: " + fileName);
					sender.sendMessage(servers.get(3).getIp(), 40000, "FileName from main: " + fileName);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					sender.sendFile(servers.get(0).getIp(), 50000, file);
					sender.sendFile(servers.get(3).getIp(), 50000, file);

					// part1
					sender.sendMessage(servers.get(1).getIp(), 40000,
							"FileName from main: " + fileName.substring(0, fileName.length() - 1) + "1");
					sender.sendMessage(servers.get(4).getIp(), 40000,
							"FileName from main: " + fileName.substring(0, fileName.length() - 1) + "1");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					sender.sendFile(servers.get(1).getIp(), 50000, file1);
					sender.sendFile(servers.get(4).getIp(), 50000, file1);

					// part2
					sender.sendMessage(servers.get(2).getIp(), 40000,
							"FileName from main: " + fileName.substring(0, fileName.length() - 1) + "2");
					sender.sendMessage(servers.get(5).getIp(), 40000,
							"FileName from main: " + fileName.substring(0, fileName.length() - 1) + "2");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					sender.sendFile(servers.get(2).getIp(), 50000, file2);
					sender.sendFile(servers.get(5).getIp(), 50000, file2);
				} else {

					sender.sendMessage(servers.get(0).getIp(), 40000, "FileName from main: " + fileName);
					sender.sendMessage(servers.get(3).getIp(), 40000, "FileName from main: " + fileName);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					sender.sendFile(servers.get(0).getIp(), 50000, file);
					sender.sendFile(servers.get(3).getIp(), 50000, file);
				}
			} else {
				System.out.println(fileName + " file in Edge server up-to-date.\n");
				sender.sendMessage(findPublicIp(ip), 40000, "Not Modified main: " + fileName);
			}
		}
	}

}
