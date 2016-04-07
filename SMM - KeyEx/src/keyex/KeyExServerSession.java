package keyex;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;


/**
 * This class implements the server side behaviour of the KeyEx system. 
 * @author  Michael Stegemann, Soeren Fromhagen, Manfred Kops
 * @version 1.0, April 2016
 */
public class KeyExServerSession implements Runnable{

    private KeyExConnection connectionObject;
    private DHParameterSpec dhSpec;
    private KeyPair serverKpair; 
    private PublicKey clientPublicKey;
    private SecretKey serverSharedSecret;
  
    
    /**
	 * Sets the connection this server shall communicate with
	 * @param   object   the connection object to be used by this server session.
	 */
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    
    /**
	 * Main method of the server session. Invoked as thread to allow multiple connections.
	 */
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();

    	// We need to come up with fresh Diffie-Hellman (DH) Parameters 
    	initialize_DH();
        
     	genKeyPair();
    	keyAgreement(); 
    	
    	String plaintext = receivePayload();
    
    	if (plaintext != ""){
    		System.out.println("Plaintext: "+ plaintext);
    	}
    	// Close all sockets and make sure connectionObject is ready to close
        clean_up(); 
    }
    
    /**
	 * Used to generate Diffie-Hellman Parameters P,G and L
	 */
    private void initialize_DH() {
	
	    int mode = 0;
	    try {
		    if (mode == 0) {
			    // Some central authority creates new DH parameters
		    	if(KeyEx.debugLevel >=1){
		    		System.out.println("Creating Diffie-Hellman parameters ...");
		    	}
		    	
			    AlgorithmParameterGenerator paramGen =
			    AlgorithmParameterGenerator.getInstance("DH");
			    paramGen.init(512);
			    AlgorithmParameters params = paramGen.generateParameters();
			    dhSpec =
			    (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
			    
			    if(KeyEx.debugLevel >=2){
			    	System.out.println("" + dhSpec.getP() + "," + dhSpec.getG() + "," + dhSpec.getL());	
			    }
		    }
	    }
	    catch (Exception e) {
	    	System.out.println("Error while generating DH Parameters");
	    	e.printStackTrace();
	    }
	    finally{
	    	if(KeyEx.debugLevel >=1){
	    		System.out.println("DH parameters successfully generated.");
	    	}
	    }
	    
    }
   
    /**
	 * Used to generate Diffie-Hellman keypair, based on parameters of dhSpec 
	 */
    private void genKeyPair() {
    
    	if(KeyEx.debugLevel >=1){
    		System.out.println("Server: Generate DH keypair ...");
    	}
		try {
			
			KeyPairGenerator serverKpairGen;
			serverKpairGen = KeyPairGenerator.getInstance("DH");
			serverKpairGen.initialize(dhSpec);
			serverKpair = serverKpairGen.generateKeyPair();
	    	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(KeyEx.debugLevel >=2){
			System.out.println("Server: PublicKey:  "+serverKpair.getPublic().toString());
		}
			
    }
    
    /**
	 * Transfers own Public Key to the client and performs the Key Agreement resulting in a secret key for usage with symmetric ciphers
	 */
    private void keyAgreement() {

        try {
        	
            connectionObject.output.println(toHexString(serverKpair.getPublic().getEncoded()));
            
            
            // receive PublicKey from the Client (Byte data in one line)
            byte[] clientPublicKeyEnc = toByteArray(connectionObject.input.readLine());
    		
            // decode the PublicKey
    		KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
    		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKeyEnc);
    		clientPublicKey = serverKeyFac.generatePublic(x509KeySpec);
            
    		// KeyAgreement nach DH. derive the SharedSecret from own PrivKey and remote PubKey
    		if(KeyEx.debugLevel >=1){ 
    			System.out.println("Server: Initialization ...");
    		} 
    		 KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
    		 serverKeyAgree.init(serverKpair.getPrivate());

    		serverKeyAgree.doPhase(clientPublicKey, true);
    		
    		// save shared sec as byte value, for AES
    		serverSharedSecret = serverKeyAgree.generateSecret("AES");
    		serverSharedSecret = new SecretKeySpec(serverSharedSecret.getEncoded(),0,16,"AES");
    		
    		if(KeyEx.debugLevel >=2){
    			System.out.println(toHexString(serverSharedSecret.getEncoded()));
    		}
            
        }
        catch (Exception e) {
            System.out.println("error!" + e.getMessage());
        }  
    }
    
    /**
	 * Receives (symmetrically) encrypted payload from the client and deciphers it with the serverSharedSecret 
	 */
    private String receivePayload() {
    	
    	byte[] decipheredText=null;
    	Cipher aesCipher;
    	String plainText = "";
    	
    	try {
			
    		aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aesCipher.init(Cipher.DECRYPT_MODE, serverSharedSecret);
			
	    	byte[] cipherTextEnc = toByteArray(connectionObject.input.readLine());
	    	decipheredText = aesCipher.doFinal(cipherTextEnc);
	    	
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	byte[] computedDigest = md.digest(decipheredText); 
	    	
	    	
	    	byte[] receivedDigest = toByteArray(connectionObject.input.readLine());
	    	
	    	if(KeyEx.debugLevel >=2){
    			System.out.println(toHexString(computedDigest));
    			System.out.println(toHexString(receivedDigest));
    		}
	    	
    		if (Arrays.equals(computedDigest, receivedDigest)){
    			System.out.println("Signature for the following message is valid:");
    			plainText = new String(decipheredText);
    		}else{
    			System.out.println("Signature invalid. Dropping message as faked.");
    			plainText = "";
    		}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	return plainText;
    }
    
    /**
	 * Closes the connection associated with this server session 
	 */
    private void clean_up() {

        connectionObject.prepareStreams();
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