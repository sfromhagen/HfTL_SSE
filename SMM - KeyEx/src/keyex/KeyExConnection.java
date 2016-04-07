package keyex;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.xml.bind.DatatypeConverter;

/*
 * @author: Michael Stegemann <m.stegemann_beng-tki2009k@t-online.de>
 * @version 1.0
 */

public class KeyExConnection {
    
    protected int sessionID;
    protected Socket connectionSocket;
    protected BufferedReader input;
    protected PrintWriter output;
    protected Integer contentLength = 0;
    protected String contentType = "text/html";
    protected String accept;
    protected String path;

    KeyExConnection(int sessionID, Socket connectionsocket){
        this.sessionID = sessionID;
        this.connectionSocket = connectionsocket;
    } 
    
    public void prepareStreams(){
        try
        {   
            //Vorbereitung der Streams
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
