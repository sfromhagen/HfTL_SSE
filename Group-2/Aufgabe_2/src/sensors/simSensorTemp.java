package sensors;

import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.InetAddressConverter;

public class simSensorTemp {
	
    protected static InetAddress serverInterface;
    protected static int port;
    protected static String serial;
    protected static int debugLevel;
	
	public static void main(String[] args) throws Exception{
	
		Socket connectionSocket;
	        
		OptionParser parser = new OptionParser();
        
        OptionSpec<Integer> oPort =
                parser.accepts( "p" ).withOptionalArg().ofType( Integer.class )
                .describedAs( "port" ).defaultsTo( 4242 );
        OptionSpec<InetAddress> oInterface =
                parser.accepts( "i" ).withOptionalArg().withValuesConvertedBy( new InetAddressConverter() );
        OptionSpec<String> oSerial =
                parser.accepts( "s" ).withOptionalArg().ofType( String.class )
                .describedAs( "serial" ).defaultsTo( "1234567890" );
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
          
    	port = oPort.value(options);
    	serial = oSerial.value(options);
    	
    	// jopt does not support default values for InetAddress for some reason. Workaround:
    	if (options.has( oInterface )) {
    		serverInterface = oInterface.value(options);
    	} else {
    		serverInterface = InetAddress.getLocalHost();    		
    	}
    	
    	if (options.has( oDebugLevel )) {
    		debugLevel = oDebugLevel.value(options);
    	} else {
    		debugLevel=0;
    	}

    	if(debugLevel >=1){
            System.out.println("Configuration");
            System.out.println("Interface: "+serverInterface);
            System.out.println("Port: "+port);
            System.out.println("Serial: "+serial);
            System.out.println("Debug Level: "+debugLevel);
        }
		
		try {
            System.out.println("Connecting to " +serverInterface+ ":" +port);
            connectionSocket = new Socket(serverInterface, port);
           
        }
		catch (ConnectException e) { 
			System.out.println("Connection refused by target/timed out. Is the server running?\n");
            return;
        }
        catch (Exception e) { 
            System.out.println("Unknown error while connecting to target: "+serverInterface+"\n"+e.getMessage());
            return;
        }
		
		PrintWriter output =
                new PrintWriter(connectionSocket.getOutputStream(), true);

		while (true){
			
			
			Random random = new Random();
			
			if (random.nextFloat()<0.9){
			
				float minX = 20.0f;
				float maxX = 25.0f;
	
				float finalX = random.nextFloat() * (maxX - minX) + minX;
				
				String line = getDateTimeStamp()+";"+serial+";measuredTemp;"+Float.toString(finalX); 
				System.out.println(line);
				output.println(line);
			} else{
				// Triggered an alarm
				String line = getDateTimeStamp()+";"+serial+";newAlarm;"+"Sensor battery low"; 
				System.out.println(line);
				output.println(line);		
			}
			Thread.sleep(30000);
				
		}
			
	}//end main
		

	public static String getDateTimeStamp(){
		// get Timestamp 2014-12-15T13:00:00.123
		LocalDateTime timestamp = LocalDateTime.now();
		
		//get offset "+02:00"
		TimeZone zone = TimeZone.getDefault();
	
		long hours = TimeUnit.MILLISECONDS.toHours(zone.getOffset(new Date().getTime()));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(zone.getOffset(new Date().getTime()))
				- TimeUnit.HOURS.toMinutes(hours);
	
		String timeZoneString = String.format("+%02d:%02d", hours, minutes);
		
		// get timestamp 2014-12-15T13:00:00.123+02:00
		return timestamp.toString()+timeZoneString;
		
	}
}//end class
