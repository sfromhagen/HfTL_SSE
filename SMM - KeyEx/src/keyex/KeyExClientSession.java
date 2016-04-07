package keyex;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.X509EncodedKeySpec;

/**
 * This class implements the client side behaviour of the KeyEx system. 
 * @author  Michael Stegemann, Sören Fromhagen, Manfred Kops
 * @version 1.0, April 2016
 */
public class KeyExClientSession implements Runnable{
	
    private KeyExConnection connectionObject;
    private KeyPair clientKpair;
    private SecretKey clientSharedSecret;
    private String text = "Come on, look at me. No plan, no backup, "
    		+ "no weapons worth a damn, oh, and something else I "
    		+ "don't have: anything to lose! So, if you are sitting "
    		+ "up there in your silly little spaceships with all your "
    		+ "silly little guns, and you've got any plans on taking the "
    		+ "Pandorica tonight, just remember who's standing in your way! "
    		+ "Remember every black day I ever stopped you, and then, "
    		+ "*and then* do the smart thing! Let somebody else try first.";
    
    /**
	 * Sets the connection this client shall communicate with
	 * @param   object   the connection object to be used by this client session.
	 */
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    
    /**
	 * Main method of the client session
	 */
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();
    	
        receive_handshake(); 
        sendPayload(text);
    }
    
    
    /**
	 * Receives the handshake by the server, transfers own public key back and derives the clientSharedSecret resulting from the Key Agreement
	 */
    private void receive_handshake() {
    	try{
    		byte[] serverPublicKeyEnc = toByteArray(connectionObject.input.readLine());
    		
    		KeyFactory clientKeyFac = KeyFactory.getInstance("DH");
    		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublicKeyEnc);
    		PublicKey serverPublicKey = clientKeyFac.generatePublic(x509KeySpec);
    		
    		// DH parameter ableiten
    		 DHParameterSpec dhParamSpec = ((DHPublicKey) serverPublicKey).getParams();
    		 
    		// client creates his own DH key pair
    		 if(KeyEx.debugLevel >=1){
    			 System.out.println("Client: Generate DH keypair ...");
    		 }
    		 KeyPairGenerator clientKpairGen = KeyPairGenerator.getInstance("DH");
    		 clientKpairGen.initialize(dhParamSpec);
    		 clientKpair = clientKpairGen.generateKeyPair();

    		 // client encodes his public key, and sends it over to the server.
    		 connectionObject.output.println(toHexString(clientKpair.getPublic().getEncoded()));
    		 
    		 // client creates and initializes his DH KeyAgreement object
    		 if(KeyEx.debugLevel >=1){
    			 System.out.println("Client: Initialization ...");
    		 }
    		 KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
    		 clientKeyAgree.init(clientKpair.getPrivate());
    		 
    		 clientKeyAgree.doPhase(serverPublicKey, true);
    		 
    		// Trim the resulting key to 128 bit (16 Byte) to make it work with AES
    		 clientSharedSecret = clientKeyAgree.generateSecret("AES");
    		 clientSharedSecret = new SecretKeySpec(clientSharedSecret.getEncoded(),0,16,"AES");
    		 
    		 if(KeyEx.debugLevel >=2){
    			 System.out.println(toHexString(clientSharedSecret.getEncoded()));
    		 }
    	    		 
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
	 * Uses the shared secret to encrypt the plaintext and transfer it to the server
	 */
    private void sendPayload (String plaintext) {
    	
    	Cipher aesCipher;
    	byte[] ciphertext=null;
    	byte[] digest = null;
    	try {
			aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aesCipher.init(Cipher.ENCRYPT_MODE, clientSharedSecret);
	    	ciphertext = aesCipher.doFinal(plaintext.getBytes());
			
		} catch ( Exception e) {
			e.printStackTrace();
		} 

    	finally{
    		connectionObject.output.println(toHexString(ciphertext));
    	}
    	
    	try {
    		MessageDigest md = MessageDigest.getInstance("SHA-256");
    		digest = md.digest(plaintext.getBytes());		
		} catch ( Exception e) {
			e.printStackTrace();
		} 

    	finally{
    		connectionObject.output.println(toHexString(digest));
    	}
    	
    	
    }
    
    /**
	 * Helper function to encode a byte array into Hex String 
	 * @param   array   the byte array to be converted to a Hex String
	 * @return  The input array encoded in Hex
	 */
    public static String toHexString(byte[] array){
      return DatatypeConverter.printHexBinary(array);
    }
    /**
   	 * Helper function to encode a String into byte array 
   	 * @param   s   the Hex String to be converted to a byte array
   	 * @return  The input Hex String decoded into a byte array
   	 */
    public static byte[] toByteArray(String s){
        return DatatypeConverter.parseHexBinary(s);
    }
    
}
