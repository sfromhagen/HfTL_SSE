package sensors;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class simSensorTemp {
	
    protected static InetAddress serverInterface;
    protected static int port;
    protected static String serial;
	
	public static void main(String[] args) throws Exception{
		
	/*Parameter auslesen für Modus: Push(=Intervall)/Pull(=get)- Mode*/
	
	/*Simulierte Rückgabe von zeitstempel + Temperatur in Grad C*/

		
		serverInterface = InetAddress.getLocalHost(); 
		port = 4242;
		serial = "12345678";
		Socket connectionSocket;
		
		
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
			
			float minX = 00.0f;
			float maxX = 35.0f;

			Random rand = new Random();

			float finalX = rand.nextFloat() * (maxX - minX) + minX;
			
			
			String line = getDateTimeStamp()+","+serial+",measuredTemp,"+Float.toString(finalX); 
			System.out.println(line);
			output.println(line);
			Thread.sleep(5000);
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
