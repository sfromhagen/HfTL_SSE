package keyexchange_crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private static int port = 13337; /* port to connect to */
	private static String host;
	
	private static BufferedReader stdIn;
	
	 public static void main (String[] args) throws IOException {
		 Socket server = null;
		  host = args[0];
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
    private BufferedReader in = null;
  
    public ServerConn(Socket server) throws IOException {
        /* obtain an input stream from the server */
        in = new BufferedReader(new InputStreamReader(
                    server.getInputStream()));
    }
  
    public void run() {
        String msg;
        try {
            /* loop reading messages from the server and show them 
             * on stdout */
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}