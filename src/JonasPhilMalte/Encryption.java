package JonasPhilMalte;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Encryption {
	public byte[] encrypt(byte[] plaintext, SecretKey sKey) throws Exception, NoSuchPaddingException{
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, sKey);
		byte[] ciphertext = aesCipher.doFinal(plaintext);
		return ciphertext;
	}
}
