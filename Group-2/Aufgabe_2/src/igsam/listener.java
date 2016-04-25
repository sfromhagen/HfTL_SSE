package igsam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	private cloudConnection cloudCon;
	private volatile boolean done = false;
	
	public listener(cloudConnection cloudConnect, Socket cSocket){

		this.cloudCon = cloudConnect;
		this.clientSocket = cSocket;
		
		try{   
            //Preparing streams
            input =
                new BufferedReader(new InputStreamReader(clientSocket.
                getInputStream()));           
        }
        catch(Exception e){
            System.out.println("Fehler beim generieren der Streams");
        }
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("ready to listen");
        try {
			//System.out.println(input.readLine());
			
        	while (!done){
				String inputData = input.readLine();
				String[] data;
				
				String timestamp;
						
				//Split the data  
				data = inputData.split(";");
				
				
				if (data.length != 4){
					//we didnt get 4 elements, lets skip this one
					//[0] - Timestamp
					//[1] - Serial
					//[2] - Type
					//[3] - Measurement
					continue;
				}
				
				String id = cloudCon.getDevice(data[1]);
				
				System.out.println(id);
				if (id == null){
					id = cloudCon.addDevice(data[1]);
					System.out.println(id);
					cloudCon.registerDevice(data[1], id);
				}
				
				if (data[2].equalsIgnoreCase("measuredTemp")){
					cloudCon.sendData(id,Float.parseFloat(data[3]),data[0]);
				} else if(data[2].equalsIgnoreCase("newAlarm")){
					cloudCon.sendAlarms(id,data[3],data[0],"c8y_PowerAlarm","ACTIVE","MAJOR");
				} 
				
				
        	}
	
		} catch (Exception e) {
			// Exception while processing data, lets close this thread
			done = true;
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

}

