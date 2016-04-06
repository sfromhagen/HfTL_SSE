package keyex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import javax.xml.bind.DatatypeConverter;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

public class KeyExServerSession implements Runnable{

    private KeyExConnection connectionObject;
    private DHParameterSpec dhSpec;
    private KeyPair serverKpair; 
    
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
        	
            connectionObject.output.println(ToHexString(serverKpair.getPublic().getEncoded()));
            
            
            // receive PublicKey from the Client (Byte data in one line)
            byte[] clientPublicKeyEnc = ToByteArray(connectionObject.input.readLine());
    		
            // decode the PublicKey
    		KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
    		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKeyEnc);
    		PublicKey clientPublicKey = serverKeyFac.generatePublic(x509KeySpec);
            
    		// KeyAgreement nach DH. derive the SharedSecret from own PrivKey and remote PubKey
    		 System.out.println("ALICE: Initialization ...");
    		 KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
    		 serverKeyAgree.init(serverKpair.getPrivate());

    		serverKeyAgree.doPhase(clientPublicKey, true);
    		
    		// save shared sec as byte value
    		byte[] serverSharedSecret = serverKeyAgree.generateSecret();
   		    System.out.println(ToHexString(serverSharedSecret));
   		 
            
        }
        catch (Exception e) {
            System.out.println("error!" + e.getMessage());
        }  
    }
    private void clean_up() {

        connectionObject.prepareStreams();
    }
   
    //Hilfsfunktion vor versenden der Daten um keine Steuerzeichen o.Ä. im Pub-Key zu haben
    public static String ToHexString(byte[] array){
      return DatatypeConverter.printHexBinary(array);
    }
    public static byte[] ToByteArray(String s){
        return DatatypeConverter.parseHexBinary(s);
    }
    
}