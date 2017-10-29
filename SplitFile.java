import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class splits the file into parts
 * 
 * @author Akshai Prabhu
 * @author Hari Prashanth
 *
 */
public class SplitFile {
	File file;
	int readInt;
	RandomAccessFile fileIn, fileOut;
	byte[] bytes;
	long e;
	int partNumber;
	double fileSize;
	int splitSize;

	/**
	 * To split the file in the server
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public SplitFile(String fileName) throws Exception {
		file = new File("local/" + fileName);
		bytes = new byte[2048];
		e = 0L;
		partNumber = 0;
		fileSize = file.length();
		splitSize = (int) (fileSize / 3);

		fileIn = new RandomAccessFile(file, "r");
		partition();
		fileIn.close();
	}

	/**
	 * Perform partition on the file
	 * 
	 * @throws IOException
	 */
	public void partition() throws IOException {
		fileOut = new RandomAccessFile(file.getPath() + "part" + partNumber++, "rw");

		while ((readInt = fileIn.read(bytes)) != -1) {
			fileOut.write(bytes, 0, readInt);
			e += readInt;

			if (e > splitSize) {
				e = 0L;
				fileOut.close();
				if (partNumber < 3) {
					partition();
				}
			}
		}
	}
}