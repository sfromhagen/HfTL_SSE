package keyexchange_crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	

	 private static int port = 13337; /* port to listen on */
	  
	    public static void main (String[] args) throws IOException {
	  
	        ServerSocket server = null;
	        try {
	            server = new ServerSocket(port); /* start listening on the port */
	        } catch (IOException e) {
	            System.err.println("Could not listen on port: " + port);
	            System.err.println(e);
	            System.exit(1);
	        }
	  
	        Socket client = null;
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
    
    public class ServerProtocol {
    	private ClientConn conn;
    	
    	//Hier müssen noch die ganzen KeyPairs etc. gespeichert werden.
    	
    	private static final int WAITING = 0;
        private static final int SENTKNOCKKNOCK = 1;
        private static final int SENTCLUE = 2;
        private static final int ANOTHER = 3;
     
        private static final int NUMJOKES = 5;
     
        private int state = WAITING;
        private int currentJoke = 0;
     
        private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
        private String[] answers = { "Turnip the heat, it's cold in here!",
                                     "I didn't know you could yodel!",
                                     "Bless you!",
                                     "Is there an owl in here?",
                                     "Is there an echo in here?" };
     
        public ServerProtocol(ClientConn c) {
            conn = c;
        }
        
        public String processInput(String theInput) {
            String theOutput = null;
            
            if (theInput.matches("Test")){
            	theOutput = "Test geklappt.";
            }
            /** if (state == WAITING) {
                theOutput = "Knock! Knock!";
                state = SENTKNOCKKNOCK;
            } else if (state == SENTKNOCKKNOCK) {
                if (theInput.equalsIgnoreCase("Who's there?")) {
                    theOutput = clues[currentJoke];
                    state = SENTCLUE;
                } else {
                    theOutput = "You're supposed to say \"Who's there?\"! " +
                    "Try again. Knock! Knock!";
                }
            } else if (state == SENTCLUE) {
                if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
                    theOutput = answers[currentJoke] + " Want another? (y/n)";
                    state = ANOTHER;
                } else {
                    theOutput = "You're supposed to say \"" + 
                    clues[currentJoke] + 
                    " who?\"" + 
                    "! Try again. Knock! Knock!";
                    state = SENTKNOCKKNOCK;
                }
            } else if (state == ANOTHER) {
                if (theInput.equalsIgnoreCase("y")) {
                    theOutput = "Knock! Knock!";
                    if (currentJoke == (NUMJOKES - 1))
                        currentJoke = 0;
                    else
                        currentJoke++;
                    state = SENTKNOCKKNOCK;
                } else {
                    theOutput = "Bye.";
                    state = WAITING;
                }
            }
            
            */
            
            return theOutput;
        }
        
    }
  
    public void run() {
        String msg, response;
        ServerProtocol protocol = new ServerProtocol(this);
        try {
            /* loop reading lines from the client which are processed 
             * according to our protocol and the resulting response is 
             * sent back to the client */
            while ((msg = in.readLine()) != null) {
                response = protocol.processInput(msg);
                if (response != null) { 
                	out.println("SERVER: " + response);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
  
    /*public void sendMsg(String msg) {
        out.println(msg);
    }
    */
}