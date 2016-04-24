package igsam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class listener implements Runnable {

	private Socket clientSocket;
	private BufferedReader input;
	
	public listener(cloudConnection cloudCon, Socket clientSocket){
	 
		
		try
        {   
			System.out.println("try input");
            //Preparing streams
            input =
                new BufferedReader(new InputStreamReader(clientSocket.
                getInputStream()));
    
           // output =
           //     new PrintWriter(clientSocket.getOutputStream(), true);
            
        }
        catch(Exception e){
            System.out.println("Fehler beim generieren der Streams");
        }
		
		
	}

	
	public static String fixDateTimeStamp(String orgTimeStamp) throws ParseException{
		// get Timestamp 2014-12-15T13:00:00.123
		// 2016-04-24_15:47:56
		
		Date orgDate;
		orgDate = DateFormat.getDateInstance().parse(orgTimeStamp);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		  sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		  String fixTimeStamp = sdf.format(orgDate);
		
		  System.out.println(fixTimeStamp);
		  
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("ready to listen");
        try {
			//System.out.println(input.readLine());
			
			String inputData = input.readLine();
			String[] data;
			
			String timestamp;
					
			//Splitt data  
			data = inputData.split(";");
			
		
			//Fix Timestamp 
			fixDateTimeStamp(data[0]);
			
			
			// Tendent ID -> Check ob dev angelegt
			
			// Check ob serial registiert
			
			// send
			
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
		
	}
	
	
	
	
	/*Kommunikation zum Versender*/
	
}
