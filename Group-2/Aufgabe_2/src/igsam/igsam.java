package igsam;

import java.io.Console;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class igsam {

	//todo read debug level via ini file
	public static int debugLevel = 0;
	public static String url;
    protected static InetAddress Interface;
    protected static int Port;
    protected static Hashtable<String, String> types = new Hashtable<String, String>();
    
	public static void main(String[] args) throws Exception{
	
		String encodedAuth = null; 
		Properties conf = new Properties();
		
		try {
            // loading Configuration
            FileInputStream config = new FileInputStream("config.properties");
            conf.load(config);
            config.close();
        }catch (Exception e) { 
            System.out.println("Konfiguration konnte nicht geladen werden.\nVerwendet Standardwerte");
        }
		
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
		Port = (conf.getProperty("Port")==null) ? 
	            new Integer(4242) : Integer.parseInt(conf.getProperty("Port"));	
	            
	    String strInterface = (conf.getProperty("Interface")==null) ? 
	    		"0.0.0.0" : conf.getProperty("Interface");
	    
	    url = (conf.getProperty("url")==null) ? 
	    		"https://cdm.ram.m2m.telekom.com/" : conf.getProperty("url");
	    
	    
	    Interface = InetAddress.getByName(strInterface);
		
		//START SERVER!!!!!!!!!!!!!
		
    		ServerSocket serversocket;
    		
    		try {
    			
    			Interface = InetAddress.getLocalHost();
    			
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
                    Socket connectionSocket = serversocket.accept();
                    
                    cloudConnection connectionObject = new cloudConnection("HfTL-Group2");
                    connectionObject.setAuthorization(encodedAuth);

                    listener runnable = new listener(connectionObject, connectionSocket);

                    new Thread(runnable).start();
                }
                catch (Exception e) { 
                    System.out.println("Fehler im Serverthread: " + e.getMessage());
                }
            }
            // ENDE SERVER
		
		
	}
	
	
	public static void testCdd(String encodedAuth, String serial, String url) throws Exception{
		
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
		connectCdd.sendEvent(id, (float) 73.9, (float) 6.151782, (float) 51.211971, strTime, "Location updated", "queclink_GV200LocationUpdate");
			
//		"source": { "id": "10400" },
//		"text": "Tracker lost power",
//		"time": "2013-08-19T21:31:22.740+02:00",
//		"type": "c8y_PowerAlarm",
//		"status": "ACTIVE",
//		"severity": "MAJOR",
		
		String AlarmID;
		AlarmID = connectCdd.sendAlarms(id,"Tracker lost power",strTime,"c8y_PowerAlarm","ACTIVE","MAJOR");
		
		//ToDO: GetAlarms -> Macht keine Sinn?! Last occurence ist doch auch wichtig?
		
		Thread.sleep(20000);
			
		connectCdd.updateAlarmStatus(AlarmID,"CLEARED");
		
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
