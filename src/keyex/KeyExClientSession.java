package keyex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.xml.bind.DatatypeConverter;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

/*
 * @author: Michael Stegemann <m.stegemann_beng-tki2009k@t-online.de>
 * @version 1.0
 */

public class KeyExClientSession implements Runnable{

    private KeyExConnection connectionObject;
    private KeyPair clientKpair;
    
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();
    	
    	System.out.println("\nIm a client doing client stuff");
        receive_handshake(); 
    }
    
//    private void genKeyPair() {
//        
//   	 System.out.println("Server: Generate DH keypair ...");
//   	 
//		try {
//			
//			KeyPairGenerator clientKpairGen;
//			clientKpairGen = KeyPairGenerator.getInstance("DH");
//			clientKpairGen.initialize(dhSpec);
//			clientKpair = clientKpairGen.generateKeyPair();
//	    	 
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidAlgorithmParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//   	 
//		//System.out.println("Client: PublicKey:  "+serverKpair.getPublic().toString());
//			
//   }

    private void receive_handshake() {
    	try{
    		byte[] serverPublicKeyEnc = ToByteArray(connectionObject.input.readLine());
    		
    		KeyFactory clientKeyFac = KeyFactory.getInstance("DH");
    		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublicKeyEnc);
    		PublicKey serverPublicKey = clientKeyFac.generatePublic(x509KeySpec);
    		
    		// DH parameter ableiten
    		 DHParameterSpec dhParamSpec = ((DHPublicKey) serverPublicKey).getParams();
    		 
    		// client creates his own DH key pair
    		 System.out.println("Client: Generate DH keypair ...");
    		 KeyPairGenerator clientKpairGen = KeyPairGenerator.getInstance("DH");
    		 clientKpairGen.initialize(dhParamSpec);
    		 KeyPair clientKpair = clientKpairGen.generateKeyPair();

    		 // client creates and initializes his DH KeyAgreement object
    		 System.out.println("BOB: Initialization ...");
    		 KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
    		 clientKeyAgree.init(clientKpair.getPrivate());

    		 // client encodes his public key, and sends it over to the server.
    		 connectionObject.output.println(ToHexString(clientKpair.getPublic().getEncoded()));
    		 
    		 clientKeyAgree.doPhase(serverPublicKey, true);
    		 
    		 byte[] clientSharedSecret = clientKeyAgree.generateSecret();
    		 System.out.println(ToHexString(clientSharedSecret));
    		 
    	} catch (Exception e){
    		e=e;
    		
    	}

    }

    //Hilfsfunktion vor versenden der Daten um keine Steuerzeichen o.Ä. im Pub-Key zu haben
    public static String ToHexString(byte[] array){
      return DatatypeConverter.printHexBinary(array);
    }
    public static byte[] ToByteArray(String s){
        return DatatypeConverter.parseHexBinary(s);
    }
    
    
}
