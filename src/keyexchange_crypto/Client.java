package keyexchange_crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class Client {
	private static int port; /* port to connect to */
	private static String host;
	private static BufferedReader stdIn;
	
	 public static void main (String[] args) throws IOException, Exception {
		  stdIn = new BufferedReader(new InputStreamReader(System.in));
		  System.out.print("Bitte IP-Adresse des Host eingeben: ");
		  host = stdIn.readLine();
		  System.out.print("Bitte Port des Host eingeben: ");
		  port = Integer.parseInt(stdIn.readLine());
		  Socket server = null;
	        try {
	            server = new Socket(host, port);
	        } catch (UnknownHostException e) {
	            System.err.println(e);
	            System.exit(1);
	        }
	  
	        /* create a thread to asynchronously read messages from the server */
	        ServerConn sc = new ServerConn(server);
	        Thread t = new Thread(sc);
	        t.start();
	         
	 }
}




class ServerConn implements Runnable {
    private BufferedReader in = null;
    private PrintWriter out = null;
    public SecretKey sKey;
    byte[] receivedPublicKey;
    byte[] encryptedsKey;
  
    public ServerConn(Socket server) throws IOException {

        try {
            /* obtain an input stream from the server */
            in = new BufferedReader(new InputStreamReader(
                        server.getInputStream()));
            /* ... and an output stream to the same server */
            out = new PrintWriter(server.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
        
    }    
    
    public void run() {
        String msg;
       
		try {
		
			// Start of KeyExchange
        	out.println("ReqKeyExchange");
        	System.out.println("Key Exchange Request sent to Server.");
        	
        	// Reading Public Key from Server and save in PublicKey Object
        	msg = in.readLine();
        	receivedPublicKey = DatatypeConverter.parseHexBinary(msg);
        	
			KeyFactory kf_RSA = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(receivedPublicKey);
			PublicKey pubKey = kf_RSA.generatePublic(x509Spec);
			System.out.println("Public Key from Server received.");
			
			// Client generiert symmetrischen Secret Key (AES) und speichert diesen in Variable.
			GenerateSecret sKeyGen = new GenerateSecret();
			SecretKey sKey = sKeyGen.neuesSecret();
			System.out.println("Secret Key generated.");
			
			// Client verschlüsselt symmetrischen Secret Key mittels Public Key und sendet 
			// verschlüsselten symmetrischen Secret Key an Server.
			Crypto encrypter = new Crypto();
			encryptedsKey = encrypter.encryptAsymmetric(sKey.getEncoded(), pubKey);
			out.println(DatatypeConverter.printHexBinary(encryptedsKey));
			System.out.println("Encrypted Secret Key sent to Server.");
			System.out.println("Ready for Secure Messaging.");
			
        	ServerConnSecureSend secureSending = new ServerConnSecureSend(out, sKey);
        	Thread sendingThread = new Thread(secureSending);
        	sendingThread.start();
        	
        	ServerConnSecureReceive secureReceiving = new ServerConnSecureReceive(in, sKey);
        	Thread receivingThread = new Thread(secureReceiving);
        	receivingThread.start();
        	
        		
            	//System.out.println(msg);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
       
    }
}

class ServerConnSecureSend implements Runnable{
	private PrintWriter out = null;
	private SecretKey sKey = null;
	private static BufferedReader stdIn;
	private String msg;
	byte[] ciphertext;
	public ServerConnSecureSend(PrintWriter out, SecretKey sKey){
		this.out = out;
		this.sKey = sKey;
	}
	
	public void run(){
		stdIn = new BufferedReader(new InputStreamReader(System.in));
        /* loop reading messages from stdin and sending them to the server */
        try {
			while ((msg = stdIn.readLine()) != null) {
				Crypto encrypter = new Crypto();
				ciphertext = encrypter.encryptSymmetric(msg.getBytes(), sKey);
				out.println(DatatypeConverter.printHexBinary(ciphertext));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}

class ServerConnSecureReceive implements Runnable{
	private BufferedReader in = null;
	private SecretKey sKey = null;
	private String msg;
	byte[] ciphertext;
	
	public ServerConnSecureReceive(BufferedReader in, SecretKey sKey){
		this.in = in;
		this.sKey = sKey;
	}
	
	public void run(){
		
    	/* loop reading messages from the server and show them 
         * on stdout */
        	 try {
				while ((msg = in.readLine()) != null) {
					Crypto decrypter = new Crypto();
					ciphertext = decrypter.decryptSymmetric(DatatypeConverter.parseHexBinary(msg), sKey);
				    System.out.println("Server: " + new String(ciphertext));
				    }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            } 
	}