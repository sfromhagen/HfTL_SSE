package keyexchange_crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import keyexchange_crypto.ClientConn.ServerProtocol;

public class Client {
	private static int port = 13337; /* port to connect to */
	private static String host;
	public static String a;
	
	private static BufferedReader stdIn;
	
	 public static void main (String[] args) throws IOException {
		 Socket server = null;
		  host = "localhost"; // args[0];
	        try {
	            server = new Socket(host, port);
	        } catch (UnknownHostException e) {
	            System.err.println(e);
	            System.exit(1);
	        }
	  
	        stdIn = new BufferedReader(new InputStreamReader(System.in));
	  
	        /* obtain an output stream to the server... */
	        PrintWriter out = new PrintWriter(server.getOutputStream(), true);
	        /* ... and an input stream */
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	                    server.getInputStream()));
	  
	        /* create a thread to asynchronously read messages from the server */
	        ServerConn sc = new ServerConn(server);
	        Thread t = new Thread(sc);
	        t.start();
	  
	        String msg;
	        /* loop reading messages from stdin and sending them to the server */
	        while ((msg = stdIn.readLine()) != null) {
	            out.println(msg);
	        }
		 
	 }
}




class ServerConn implements Runnable {
    private Socket server;
	private BufferedReader in = null;
    private PrintWriter out = null;
  
    public ServerConn(Socket server) throws IOException {

        this.server = server;
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
  
    public class ClientProtocol {
    	private ServerConn conn;
    	
    	//Hier müssen noch die ganzen KeyPairs etc. gespeichert werden.
    	
    	private static final int NOSESSION = 0;
        private static final int KEYEXCHANGEREQUESTED = 1;
        private static final int READYFORSECUREMESSAGING = 2;
        private static final int ANOTHER = 3;
     
        private static final int NUMJOKES = 5;
     
        private int state = NOSESSION;
        private int currentJoke = 0;
     
        private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
        private String[] answers = { "Turnip the heat, it's cold in here!",
                                     "I didn't know you could yodel!",
                                     "Bless you!",
                                     "Is there an owl in here?",
                                     "Is there an echo in here?" };
     
        public ClientProtocol(ServerConn c) {
            conn = c;
        }
        
        public String processInput(String theInput) {
            String theOutput = null;
            
             if (state == KEYEXCHANGEREQUESTED) {
            	 /*speichert Public Key (in Variable).
            	 	- Client generiert symmetrischen Secret Key (AES) und speichert diesen in Variable.
            	 	- Client verschlüsselt symmetrischen Secret Key mittels Public Key.
            	 	- Client sendet verschlüsselten symmetrischen Secret Key an Server.
            	 	- ClientState wechselt auf "ReadyForSecureMessaging"
            	 	*/
            	 
                state = READYFORSECUREMESSAGING;
            } else if (state == READYFORSECUREMESSAGING) {
               //Payload mittels SecretKey entschlüsseln und ausgeben.
            } 
            
            
            return theOutput;
        }
        
    }
    
    public void run() {
        String msg, response;
        ClientProtocol protocol = new ClientProtocol(this);
        /* obtain an output stream to the server... */
        PrintWriter out;
		try {
			out = new PrintWriter(server.getOutputStream(), true);
			
			// Start of KeyExchange
        	out.println("ReqKeyExchange");
        	protocol.state= 2;
            /* loop reading messages from the server and show them 
             * on stdout */
            while ((msg = in.readLine()) != null) {
            	response = protocol.processInput(msg);
                if (response != null) { 
                	out.println(response);
                }
            }
            	//System.out.println(msg);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
       
    }
}