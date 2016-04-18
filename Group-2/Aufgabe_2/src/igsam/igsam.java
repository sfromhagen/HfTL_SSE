package igsam;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class igsam {

	//todo read debug level via ini file
	public static int debugLevel = 0;
	
	
	public static void main(String[] args) throws Exception{
		
		
		String serial = "1337";
		
		//todo read url via config file
		String url2 = "https://cdm.ram.m2m.telekom.com/apps/devicemanagement/index.html";
		String url = "https://cdm.ram.m2m.telekom.com/inventory/managedObjects";
		
		
		String url3 = "https://cdm.ram.m2m.telekom.com/identity/externalIds/c8y_Serial/"+serial;
		
		
		
		cloudConnection connectCdd = new cloudConnection(url);
		cloudConnection connectCddget = new cloudConnection(url3);
		
		
	//	connectCdd.login("HfTL-Group-2", "GehHeim1310");
		
		// 1337 -> Sensor-Serial | id -> assigned ID by CdD
		String id = connectCddget.getDevice(serial);
				
		if (id == null){
			id = connectCdd.addDevice(serial);
			String url4 = "https://cdm.ram.m2m.telekom.com/identity/globalIds/"+id+"/externalIds";
			cloudConnection connectCddreg = new cloudConnection(url4);
			connectCddreg.registerDevice(serial);
		}
		
		//transmit first data
		String url5 = "https://cdm.ram.m2m.telekom.com//measurement/measurements";
		cloudConnection connectCddSendData = new cloudConnection(url5);
		
		// get Timestamp 2014-12-15T13:00:00.123
		LocalDateTime timestamp = LocalDateTime.now();
		
		//get offset "+02:00"
		TimeZone zone = TimeZone.getDefault();
	
		long hours = TimeUnit.MILLISECONDS.toHours(zone.getOffset(new Date().getTime()));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(zone.getOffset(new Date().getTime()))
				- TimeUnit.HOURS.toMinutes(hours);

		String timeZoneString = String.format("+%02d:%02d", hours, minutes);
		
		// get timestamp 2014-12-15T13:00:00.123+02:00
		String strTime = timestamp.toString()+timeZoneString;
		
				
		connectCddSendData.sendData(id,(float) 13.37,strTime);
			
		
		System.out.println(id);
		
		
		
		
	}
}
