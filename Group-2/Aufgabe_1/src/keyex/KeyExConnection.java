package keyex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This helper-class encapsulates the socket connection to the remote party
 * @author  Michael Stegemann, Soeren Fromhagen, Manfred Kops
 * @version 1.0, April 2016
 */
public class KeyExConnection {
    
    protected int sessionID;
    protected Socket connectionSocket;
    protected BufferedReader input;
    protected PrintWriter output;

    /**
     * Constructs a connection object from this socket
     * @param   connectionsocket   the socket to derive Input/Outputstreams from 
     */
    KeyExConnection(Socket connectionsocket){
        this.connectionSocket = connectionsocket;
    } 
    
    /**
     * Derives convenient write/read handles to transfer data to the remote party
     */
    public void prepareStreams(){
        try
        {   
            //Preparing streams
            input =
                new BufferedReader(new InputStreamReader(connectionSocket.
                getInputStream()));
    
            output =
                new PrintWriter(connectionSocket.getOutputStream(), true);
            
        }
        catch(Exception e){
            System.out.println("Fehler beim generieren der Streams");
        }
    } 
    
    /**
     * Closes the socket associated with this connection.
     */
    public void closeStreams(){
        try
        {   
            connectionSocket.close();
        }
        catch(Exception e){
            System.out.println("Error while closing the connection socket");
        }
    } 
    

    
    
}
