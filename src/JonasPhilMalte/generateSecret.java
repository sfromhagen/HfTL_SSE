package JonasPhilMalte;
/**
 * 
 */

/**
 * @author JonasH
 * Klasse zum Generieren eines Secrets mittels AES 128 Bit.
 */


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class generateSecret {
	
	public SecretKey neuesSecret() throws Exception {
	
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey secKey = keyGen.generateKey();
		byte[] key = secKey.getEncoded();
		String encodedKey = Base64.getEncoder().encodeToString(secKey.getEncoded());
		System.out.println(encodedKey);
		System.out.println(key);
		return secKey;
	}
}
