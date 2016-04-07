package keyex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;


/**
 * This class implements the server side behaviour of the KeyEx system. 
 * @author  Michael Stegemann, Sören Fromhagen, Manfred Kops
 * @version 1.0, April 2016
 */
public class KeyExServerSession implements Runnable{

    private KeyExConnection connectionObject;
    private DHParameterSpec dhSpec;
    private KeyPair serverKpair; 
    private SecretKey serverSharedSecret;
   // private Shared
    
    
    /**
	 * Pushes an item on to the top of this stack. 
	 * @param   object   the connection object to be used.
	 */
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    
    
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();
    	
    	System.out.println("\nIm a server doing server stuff");
    	
    	// We need to come up with fresh Diffie Hellman (DH) Parameters 
    	initialize_DH();
        
    	// 
    	genKeyPair();
    	
    	//
    	keyAgreement(); 
    	
    	String plaintext = receivePayload();
    	System.out.println("Plaintext: "+ plaintext);
        // Close all sockets and make sure connectionObject is ready to close
        clean_up(); 
    }
    
    private void initialize_DH() {
	
	    int mode = 0;
	    try {
		    if (mode == 0) {
			    // Some central authority creates new DH parameters
			    System.out.println("Creating Diffie-Hellman parameters ...");
			    AlgorithmParameterGenerator paramGen =
			    AlgorithmParameterGenerator.getInstance("DH");
			    paramGen.init(512);
			    AlgorithmParameters params = paramGen.generateParameters();
			    dhSpec =
			    (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
			    System.out.println("" + dhSpec.getP() + "," + dhSpec.getG() + "," + dhSpec.getL());	
		    }
	    }
	    catch (Exception e) {
	    	System.out.println("Error while generating DH Parameters");
	    	e.printStackTrace();
	    }
	    finally{
	    	System.out.println("DH parameters successfully generated.");
	    }
	    
    }
    
    private void genKeyPair() {
    
    	 System.out.println("Server: Generate DH keypair ...");
    	 
		try {
			
			KeyPairGenerator serverKpairGen;
			serverKpairGen = KeyPairGenerator.getInstance("DH");
			serverKpairGen.initialize(dhSpec);
			serverKpair = serverKpairGen.generateKeyPair();
	    	 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
		System.out.println("Server: PublicKey:  "+serverKpair.getPublic().toString());
			
    }
    
    private void keyAgreement() {

        try {
        	
            connectionObject.output.println(toHexString(serverKpair.getPublic().getEncoded()));
            
            
            // receive PublicKey from the Client (Byte data in one line)
            byte[] clientPublicKeyEnc = toByteArray(connectionObject.input.readLine());
    		
            // decode the PublicKey
    		KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
    		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKeyEnc);
    		PublicKey clientPublicKey = serverKeyFac.generatePublic(x509KeySpec);
            
    		// KeyAgreement nach DH. derive the SharedSecret from own PrivKey and remote PubKey
    		 System.out.println("Server: Initialization ...");
    		 KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
    		 serverKeyAgree.init(serverKpair.getPrivate());

    		serverKeyAgree.doPhase(clientPublicKey, true);
    		
    		// save shared sec as byte value, for AES
    		//byte[] serverSharedSecret = serverKeyAgree.generateSecret();
    		serverSharedSecret = serverKeyAgree.generateSecret("AES");
    		
    		
    		
    		System.out.println(toHexString(serverSharedSecret.getEncoded()));
    	//	SecretKeyFactory fac = SecretKeyFactory.getInstance("AES");
    	//	serverSharedSecret = fac.translateKey(serverSharedSecret);
    		
    		serverSharedSecret = new SecretKeySpec(serverSharedSecret.getEncoded(),0,16,"AES");
    		
    		System.out.println(toHexString(serverSharedSecret.getEncoded()));
   		 
            
        }
        catch (Exception e) {
            System.out.println("error!" + e.getMessage());
        }  
    }
    
    private String receivePayload() {
    	
    	byte[] decipheredText=null;
    	Cipher aesCipher;
    	
    	try {
			
    		aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aesCipher.init(Cipher.DECRYPT_MODE, serverSharedSecret);
			
	    	byte[] cipherTextEnc = toByteArray(connectionObject.input.readLine());
	    	decipheredText = aesCipher.doFinal(cipherTextEnc);
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	   	
    	String plaintext = new String(decipheredText);
    	
    	return plaintext;
    }
    
    
    private void clean_up() {

        connectionObject.prepareStreams();
    }
   
    //Hilfsfunktion vor versenden der Daten um keine Steuerzeichen o.Ä. im Pub-Key zu haben
    public static String toHexString(byte[] array){
      return DatatypeConverter.printHexBinary(array);
    }
    public static byte[] toByteArray(String s){
        return DatatypeConverter.parseHexBinary(s);
    }
    
}