package igsam;

import java.io.Console;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class igsam {

	//todo read debug level via ini file
	public static int debugLevel = 0;
	public static String url = null;
	
	
	public static void main(String[] args) throws Exception{
	
		String serial = "4242";
		url = "https://cdm.ram.m2m.telekom.com/";
		String encodedAuth = null; 
		
		Console console = System.console();
		if (console != null) {
			// Eclipse is being retarded again with bugs from 2008
			// this prompt will only show when running the jar as eclipse doesn't attach a proper console
			System.out.print("Enter your username: ");
			String username = console.readLine();
			 
			System.out.print("Enter your password: ");
			char[] password = console.readPassword();
			
			String authorization = username +":"+ new String(password);
			encodedAuth = Base64.encode(authorization.getBytes());
	        System.out.println(encodedAuth);
		}else{
			// fallback for running in debug mode inside the IDE 
			encodedAuth = "SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw";
		}
			
		cloudConnection connectCdd = new cloudConnection(url);
		connectCdd.setAuthorization(encodedAuth);
	
		// 1337 -> Sensor-Serial | id -> assigned ID by CdD
		String id = connectCdd.getDevice(serial);
				
		if (id == null){
			id = connectCdd.addDevice(serial);
			connectCdd.registerDevice(serial, id);
		}
					
		String strTime = getDateTimeStamp();
		connectCdd.sendData(id,(float) 13.37,strTime);
			
//		"source": { "id": "10400" },
//		"text": "Tracker lost power",
//		"time": "2013-08-19T21:31:22.740+02:00",
//		"type": "c8y_PowerAlarm",
//		"status": "ACTIVE",
//		"severity": "MAJOR",
		
		connectCdd.sendAlarms(id,"Tracker lost power",strTime,"c8y_PowerAlarm","ACTIVE","MAJOR");
		
		
		System.out.println(id);
	}
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
}
