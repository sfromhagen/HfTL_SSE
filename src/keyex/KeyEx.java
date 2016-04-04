package keyex;

import java.io.File;
import java.io.FileInputStream;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.InetAddressConverter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.ArrayList;

public class KeyEx{

    protected static HashMap<String, String> types = new HashMap<String, String>();
    protected static int globalSessionID;
    protected static int debugLevel;
    protected static String docPath;
    protected static int delay;
    protected static InetAddress Interface;
    protected static int Port;
    public enum runModes {
        SERVER, CLIENT
    }
    protected static runModes runMode;


    public static void main(String[] args) throws IOException{
             
        System.out.println("Key Exchange System v.1\n Programmed by SMM\n");

        // simple way of parsing cmd args in a typesafe(!) manner
        OptionParser parser = new OptionParser();
        
        OptionSpec oServer =
        		parser.accepts( "server" ).withOptionalArg();
        OptionSpec<Integer> oPort =
                parser.accepts( "p" ).withOptionalArg().ofType( Integer.class )
                .describedAs( "port" ).defaultsTo( 1337 );
        OptionSpec<InetAddress> oInterface =
                parser.accepts( "i" ).withOptionalArg().withValuesConvertedBy( new InetAddressConverter() );
        OptionSpec<File> oFile =
                parser.accepts( "output-file" ).withOptionalArg().ofType( File.class ).describedAs( "file" );
        OptionSpec<Integer> oDebugLevel =
                parser.accepts( "d" ).withOptionalArg().ofType( Integer.class )
                .describedAs( "debug level" ).defaultsTo( 2 );
        OptionSpec oHelp = parser.accepts( "h" ).forHelp();
        OptionSet options = parser.parse( args );
 
        // Options are now parsed from command line. Some translation needed.        
        if (options.has( oHelp )) {
        	// User asked for help. 
        	//Dump parameter text and stop execution (as he needs to fix the arguments)
        	parser.printHelpOn( System.out );
        	return;
        }
          
    	if (options.has( oServer )) {
    		runMode = runModes.SERVER;
    	} else {
    		runMode = runModes.CLIENT;
    	}
// Blödsinn
    	Port = oPort.value(options);
    	
    	// jopt does not support default values for InetAddress for some reason. Workaround:
    	if (options.has( oInterface )) {
    		Interface = oInterface.value(options);
    	} else {
    		Interface = InetAddress.getLocalHost();    		
    	}
    	
    	if (options.has( oDebugLevel )) {
    		debugLevel = oDebugLevel.value(options);
    	} else {
    		debugLevel=0;
    	}

    	if(debugLevel >=1){
            System.out.println("Configuration");
            System.out.println("Interface: "+Interface);
            System.out.println("Port: "+Port);
            System.out.println("Debug Level: "+debugLevel);
        }
    	
    	if (runMode == runModes.SERVER){
    		ServerSocket serversocket;
    		
    		try {
                System.out.println("Versuche " +Interface+ " an Port " +Port+ " zu binden.");
                
                serversocket = new ServerSocket(Port, 0, Interface);
               
            }
            catch (Exception e) { 
                System.out.println("I/O Error while binding to socket: "+e.getMessage());
                return;
            }
            
            System.out.println("\nBereit. Warten auf Anfragen...\n");
            
            // Dispatch loop
            while (true){
                // i changed something
                try {
                    // Waiting for incoming client requests
                    Socket connectionsocket = serversocket.accept();

                    // 
                    KeyExServerSession runnable = new KeyExServerSession();
                    
                    // hold all metadata of the connection
                    KeyExConnection connectionObject = new KeyExConnection(globalSessionID,connectionsocket);
                    
                    // handoff the socket data to the worker thread and start it
                    runnable.setConnection(connectionObject);
                    new Thread(runnable).start();
                }
                catch (Exception e) { 
                    System.out.println("Fehler im Serverthread: " + e.getMessage());
                }
            }
    	}else if(runMode == runModes.CLIENT){
    		
    		Socket connectionSocket;
    		
    		try {
                System.out.println("Connecting to " +Interface+ ":" +Port);
                connectionSocket = new Socket(Interface, Port);
               
            }
    		catch (ConnectException e) { 
    			System.out.println("Connection refused by target/timed out. Is the server running?\n");
                return;
            }
            catch (Exception e) { 
                System.out.println("Unknown error while connecting to target: "+Interface+"\n"+e.getMessage());
                return;
            }
    		
    		KeyExConnection connectionObject = new KeyExConnection(globalSessionID, connectionSocket);
	
    		KeyExClientSession runnable = new KeyExClientSession();
    		runnable.setConnection(connectionObject);
            new Thread(runnable).start();
    	}
    	
        
       
        
        
        
    }
} 

