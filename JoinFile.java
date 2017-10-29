import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;

/**
 * Class is used to join split file
 * @author Akshai PRabhu
 * @author Hari Prabhant
 *
 */
public class JoinFile {

	File file;
	int end;
	RandomAccessFile fileIn, fileOut;
	byte[] bytes;
	long e;
	int partNumber;

	/**
	 * Configuring file join with given file name
	 * @param fileName
	 * @throws Exception
	 */
	public JoinFile(String fileName) throws Exception {
		file = new File(InetAddress.getLocalHost().getHostName() + "/" + fileName);
		bytes = new byte[2048];
		e = 0L;
		partNumber = 0;

		fileOut = new RandomAccessFile(InetAddress.getLocalHost().getHostName() + "/" + fileName, "rw");
		doJoin(new File(file.getPath() + "part0"));
	}

	/**
	 * To perform join on the split files
	 * @param fileName
	 * @throws IOException
	 */
	public void doJoin(File fileName) throws IOException {
		if (fileName.exists()) {
			fileIn = new RandomAccessFile(fileName, "r");

			while ((end = fileIn.read(bytes)) != -1) {
				fileOut.write(bytes, 0, end);
			}

			fileIn.close();
			doJoin(new File(file.getPath() + "part" + ++partNumber));
		}
	}
}
