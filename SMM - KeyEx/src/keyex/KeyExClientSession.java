package keyex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
    private SecretKey clientSharedSecret;
    private String Text = "Come on, look at me. No plan, no backup, "
    		+ "no weapons worth a damn, oh, and something else I "
    		+ "don't have: anything to lose! So, if you are sitting "
    		+ "up there in your silly little spaceships with all your "
    		+ "silly little guns, and you've got any plans on taking the "
    		+ "Pandorica tonight, just remember who's standing in your way! "
    		+ "Remember every black day I ever stopped you, and then, "
    		+ "*and then* do the smart thing! Let somebody else try first.";
    
    
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();
    	
    	System.out.println("\nIm a client doing client stuff");
        receive_handshake(); 
        
        
        sendPayload(Text);
    }
    
    
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
    		 clientKpair = clientKpairGen.generateKeyPair();

    		 // client encodes his public key, and sends it over to the server.
    		 connectionObject.output.println(ToHexString(clientKpair.getPublic().getEncoded()));
    		 
    		 // client creates and initializes his DH KeyAgreement object
    		 System.out.println("Client: Initialization ...");
    		 KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
    		 clientKeyAgree.init(clientKpair.getPrivate());
    		 
    		 clientKeyAgree.doPhase(serverPublicKey, true);
    		 
    		 //byte[] clientSharedSecret = clientKeyAgree.generateSecret("AES");
    		 
    		 clientSharedSecret = clientKeyAgree.generateSecret("AES");
    		 System.out.println(ToHexString(clientSharedSecret.getEncoded()));
    		 
    		 // Trim the resulting key to 128 bit (16 Byte) to make it work with AES
    		 clientSharedSecret = new SecretKeySpec(clientSharedSecret.getEncoded(),0,16,"AES");
    		 System.out.println(ToHexString(clientSharedSecret.getEncoded()));
    		 
    		 
    /*		 SecretKeyFactory skf = SecretKeyFactory.getInstance("AES￼");
    		            AESKeySpec desSpec = new AESKeySpec(secret);
    		            SecretKey key = skf.generateSecret(desSpec);
    		            System.out.println("[Alice] Step-6: generate a DES key using the symmetric secret.");
    		            System.out.println("[Alice] DES Key:" +toHexString(desSpec.getKey()));
    		 
    */		 
    		 
    	} catch (Exception e){
    		e=e;
    	}
    }
    
    private void sendPayload (String plaintext) {
    	
    	Cipher aesCipher;
    	byte[] ciphertext=null;
    	try {
			aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aesCipher.init(Cipher.ENCRYPT_MODE, clientSharedSecret);
	    	ciphertext = aesCipher.doFinal(plaintext.getBytes());
			
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	finally{
    		connectionObject.output.println(ToHexString(ciphertext));
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
