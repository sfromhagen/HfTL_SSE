package keyexchange_crypto;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Filehandling {
	public void writeToFile(byte[] bytestream, String filename) throws IOException{
		FileOutputStream fos = new FileOutputStream(filename);
		fos.write(bytestream);
		fos.close();
	}
	
	public void writeToFile(String stringstream, String filename) throws IOException{
		FileWriter filewriter = new FileWriter(filename);
		filewriter.write(stringstream);
		filewriter.close();
	}
	
	public byte[] readFromFile(String filename) throws IOException{
		return Files.readAllBytes(Paths.get(filename));
	}
	
}
