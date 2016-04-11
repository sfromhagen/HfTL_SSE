package keyexchange_crypto;
/**
 * 
 */

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class LokalVerschluesselnViaFile {

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
			KeyFactory kf_RSA = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(receivedPublicKey);
			PublicKey pubKey = kf_RSA.generatePublic(x509Spec);
		
		// Person B: 2. SecretKey mittels PublicKey verschluesseln & in Datei speichern.
			Crypto encrypter = new Crypto();
			byte[] encryptedsKey = encrypter.encryptAsymmetric(sKey.getEncoded(), pubKey);
			writer.writeToFile(encryptedsKey, "EncryptedSecretKey");
		
		// Person B: 3. String mittels Secret verschluesseln & in Datei speichern
			byte[] plaintext = "Come on, look at me. No plan, no backup, no weapons worth a damn, oh, and something else I don't have: anything to lose! So, if you are sitting up there in your silly little spaceships with all your silly little guns, and you've got any plans on taking the Pandorica tonight, just remember who's standing in your way! Remember every black day I ever stopped you, and then, *and then* do the smart thing! Let somebody else try first.".getBytes();
			
			
			byte[] ciphertext = encrypter.encryptSymmetric(plaintext, sKey);
			writer.writeToFile(ciphertext, "EncryptedText_from_B");
			
		
		// Person B: 4. Verschluesselten SecretKey & verschluesselten String an Person A senden.
		//
			
		// Person A: 1. Encrypted SecretKey einlesen und mit PrivateKey entschluesseln.
			byte[] receivedencryptedsKey = reader.readFromFile("EncryptedSecretKey");
			Crypto decrypter = new Crypto();
			byte[] decryptedsKey_byte = decrypter.decryptAsymmetric(receivedencryptedsKey, keyPair.getPrivate());
			
			SecretKey decryptedsKey = new SecretKeySpec(decryptedsKey_byte, 0, decryptedsKey_byte.length, "AES");
			
		// Person A: 2. String mittels SecretKey entschluesseln.
			byte[] receivedencryptedString = reader.readFromFile("EncryptedText_from_B");
			byte[] decryptedString = decrypter.decryptSymmetric(receivedencryptedString, decryptedsKey);
			System.out.println(new String(decryptedString));
			
		
	}

}
