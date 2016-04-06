package JonasPhilMalte;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Crypto {
	public byte[] encryptSymmetric(byte[] plaintext, SecretKey sKey) throws Exception, NoSuchPaddingException{
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, sKey);
		
		return aesCipher.doFinal(plaintext);
	}
	public byte[] encryptAsymmetric(byte[] plaintext, PublicKey pubKey) throws Exception, BadPaddingException {
		Cipher rsaCipher = Cipher.getInstance("RSA");
		rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
		return rsaCipher.doFinal(plaintext);
	}
	
	public byte[] decryptSymmetric(byte[] ciphertext, SecretKey skey) throws Exception, BadPaddingException{
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.DECRYPT_MODE, skey);
		byte[] plaintext = aesCipher.doFinal(ciphertext);
		return plaintext;
	}
	
	public byte[] decryptAsymmetric(byte[] ciphertext, PrivateKey privKey) throws Exception, BadPaddingException {
		Cipher rsaCipher = Cipher.getInstance("RSA");
		rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
		return rsaCipher.doFinal(ciphertext);
	}
	
}
