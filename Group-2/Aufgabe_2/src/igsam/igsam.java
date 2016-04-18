package igsam;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class igsam {

	//todo read debug level via ini file
	public static int debugLevel = 0;
	public static String url = null;
	
	
	public static void main(String[] args) throws Exception{
	
		String serial = "4242";
		url = "https://cdm.ram.m2m.telekom.com/";

		cloudConnection connectCdd = new cloudConnection(url);
	
		// 1337 -> Sensor-Serial | id -> assigned ID by CdD
		String id = connectCdd.getDevice(serial);
				
		if (id == null){
			id = connectCdd.addDevice(serial);
			connectCdd.registerDevice(serial, id);
		}
					
		String strTime = getDateTimeStamp();
		connectCdd.sendData(id,(float) 13.37,strTime);
			
		
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
