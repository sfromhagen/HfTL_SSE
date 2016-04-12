package keyexchange_crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Server {
	

	 private static int port; /* port to listen on */
	 private static BufferedReader stdIn;
	  
	    public static void main (String[] args) throws IOException {
	    	stdIn = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Bitte gewünschten Port des Servers eingeben: ");
			port = Integer.parseInt(stdIn.readLine());
			
	        ServerSocket server = null;
	        try {
	            server = new ServerSocket(port); /* start listening on the port */
	        } catch (IOException e) {
	            System.err.println("Could not listen on port: " + port);
	            System.err.println(e);
	            System.exit(1);
	        }
	  
	        Socket client = null;
	        System.out.println("Waiting for Client-Connections...");
	        while(true) {
	            try {
	                client = server.accept();
	            } catch (IOException e) {
	                System.err.println("Accept failed.");
	                System.err.println(e);
	                System.exit(1);
	            }
	            /* start a new thread to handle this client */
	            Thread t = new Thread(new ClientConn(client));
	            t.start();
	        }
	    }
}

class ClientConn implements Runnable {
    private Socket client;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private byte[] publicKey;
    private byte[] privateKey;
    byte[] receivedencryptedsKey;
    byte[] decryptedsKey_byte;
  
    ClientConn(Socket client) {
        this.client = client;
        try {
            /* obtain an input stream to this client ... */
            in = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
            /* ... and an output stream to the same client */
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
    }
    
  
    public void run() {
        String msg, response;
       
        try {
        	// Waiting on KeyExchange Request from Client
        	msg = in.readLine();
        	if (msg.matches("ReqKeyExchange")){
        		System.out.println("KeyExchange from Client requested. KeyExchange beginning...");
        		
        		// Server generiert RSA KeyPair
        		GenerateKeypair keyPairGen = new GenerateKeypair();
				KeyPair keyPair = keyPairGen.neuesKeypair();
				
				publicKey = keyPair.getPublic().getEncoded();
				privateKey = keyPair.getPrivate().getEncoded();
				
				// Server schickt RSA Public Key an Client
				out.println(DatatypeConverter.printHexBinary(publicKey));
				System.out.println("Public Key sent to Client...");
				
				// Server nimmt verschlüsselten symmetrischen Secret Key entgegen und entschlüsselt diesen mittels PrivateKey.
				String test = in.readLine();
				receivedencryptedsKey = DatatypeConverter.parseHexBinary(test);
				Crypto decrypter = new Crypto();
				decryptedsKey_byte = decrypter.decryptAsymmetric(receivedencryptedsKey, keyPair.getPrivate());
				SecretKey decryptedsKey = new SecretKeySpec(decryptedsKey_byte, 0, decryptedsKey_byte.length, "AES");
				System.out.println("Secret Key erhalten. Ready for Secure Messaging.");
		

            	ClientConnSecureSend secureSending = new ClientConnSecureSend(out, decryptedsKey);
            	Thread sendingThread = new Thread(secureSending);
            	sendingThread.start();
            	
            	ClientConnSecureReceive secureReceiving = new ClientConnSecureReceive(in, decryptedsKey);
            	Thread receivingThread = new Thread(secureReceiving);
            	receivingThread.start();
            	
            	
        	} else {
        		out.println("Error: Request for KeyExchange expected.");
        	};
        	
        } catch (Exception e) {
            System.err.println(e);
        }
    }
  
}

class ClientConnSecureSend implements Runnable{
	private PrintWriter out = null;
	private SecretKey sKey = null;
	private static BufferedReader stdIn;
	private String msg;
	byte[] ciphertext;
	public ClientConnSecureSend(PrintWriter out, SecretKey sKey){
		this.out = out;
		this.sKey = sKey;
	}
	
	public void run(){
		stdIn = new BufferedReader(new InputStreamReader(System.in));
        /* loop reading messages from stdin and sending them to the Client */
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

class ClientConnSecureReceive implements Runnable{
	private BufferedReader in = null;
	private SecretKey sKey = null;
	private String msg;
	byte[] ciphertext;
	
	public ClientConnSecureReceive(BufferedReader in, SecretKey sKey){
		this.in = in;
		this.sKey = sKey;
	}
	
	public void run(){
		
    	/* loop reading messages from the Client and show them 
         * on stdout */
        	 try {
				while ((msg = in.readLine()) != null) {
					Crypto decrypter = new Crypto();
					ciphertext = decrypter.decryptSymmetric(DatatypeConverter.parseHexBinary(msg), sKey);
				    System.out.println("Client: " + new String(ciphertext));
				    }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            } 
	}