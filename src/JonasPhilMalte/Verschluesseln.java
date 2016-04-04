package JonasPhilMalte;
/**
 * 
 */

/**
 * @author JonasH
 *
 */
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

public class Verschluesseln {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		// 1. SecretKey generieren
			generateSecret sKey = new generateSecret();
			SecretKey sKey_temp = sKey.neuesSecret();
		
		// 2. String mittels Secret verschluesseln & in Datei speichern
			byte[] plaintext = "Come on, look at me. No plan, no backup, no weapons worth a damn, oh, and something else I don't have: anything to lose! So, if you are sitting up there in your silly little spaceships with all your silly little guns, and you've got any plans on taking the Pandorica tonight, just remember who's standing in your way! Remember every black day I ever stopped you, and then, *and then* do the smart thing! Let somebody else try first.".getBytes();
			
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, sKey_temp);
			byte[] ciphertext = aesCipher.doFinal(plaintext);
			
			FileOutputStream ciphertextfos = new FileOutputStream("Encryptedtext");
			ciphertextfos.write(ciphertext);
			ciphertextfos.close();
			
			
			//byte[] key_byte = sKey_temp.getEncoded();
			//String encodedKey = Base64.getEncoder().encodeToString(sKey_temp.getEncoded());

			
		// 3. KeyPair für asymm. Verschluesselung generieren & 
		//    PublicKey in Datei speichern
			
			//byte[] key = XYZ.getEncoded();
			//FileOutputStream keyfos = new FileOutputStream(“Filename");
			//keyfos.write(key);
			//keyfos.close();
		
		// 4. SecretKey mittels PublicKey verschluesseln
		
		// 5. Verschluesselte Datei + verschluesselten SecretKey
		//    an Empfaenger übertragen
	}

}
