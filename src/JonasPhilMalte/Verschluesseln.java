package JonasPhilMalte;
/**
 * 
 */

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.util.Base64;

public class Verschluesseln {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		// Person A: 1. KeyPair für asymm. Verschluesselung generieren & 
		//    PublicKey in Datei speichern für Schlüsselaustausch
			GenerateKeypair keyPairGen = new GenerateKeypair();
			KeyPair keyPair = keyPairGen.neuesKeypair();
			
			
			byte[] publicKey = keyPair.getPublic().getEncoded();
			byte[] privateKey = keyPair.getPrivate().getEncoded();
			
			Filehandling writer = new Filehandling();
			writer.writeToFile(publicKey, "publicKey_from_A");
			writer.writeToFile(privateKey, "privateKey_from_A");
		// Person A: 2. Versand des Public Key an Person B.
			
		// Person B: 1. SecretKey generieren
			GenerateSecret sKeyGen = new GenerateSecret();
			SecretKey sKey = sKeyGen.neuesSecret();
		
		// Person B: 2. PublicKey von A aus Datei auslesen
			Filehandling reader = new Filehandling();
			byte[] receivedPublicKey = reader.readFromFile("publicKey_from_A");
		
		// Person B: 2. SecretKey mittels PublicKey verschluesseln & in Datei speichern.
			Encryption encrypter = new Encryption();
			byte[] encryptedsKey = encrypter.encrypt(receivedPublicKey, sKey);
			writer.writeToFile(encryptedsKey, "EncryptedSecretKey");
		
		// Person B: 3. String mittels Secret verschluesseln & in Datei speichern
			byte[] plaintext = "Come on, look at me. No plan, no backup, no weapons worth a damn, oh, and something else I don't have: anything to lose! So, if you are sitting up there in your silly little spaceships with all your silly little guns, and you've got any plans on taking the Pandorica tonight, just remember who's standing in your way! Remember every black day I ever stopped you, and then, *and then* do the smart thing! Let somebody else try first.".getBytes();
			
			
			byte[] ciphertext = encrypter.encrypt(plaintext, sKey);
			writer.writeToFile(ciphertext, "EncryptedText_from_B");
			
		
		// Person B: 4. Verschluesselten SecretKey & verschluesselten String an Person A senden.
		//
			
		// Person A: 1. SecretKey mit PrivateKey entschluesseln.
			
		
		// Person A: 2. String mittels SecretKey entschluesseln.
	}

}
