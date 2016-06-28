package igsam;

import java.io.Console;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class igsam {

	public static int debugLevel;
	public static String url;
	protected static InetAddress Interface;
	protected static int Port;
	protected static String namePrefix;

	public static void main(String[] args) throws Exception {

		String encodedAuth = null;
		Properties conf = new Properties();

		try {
			// loading Configuration
			FileInputStream config = new FileInputStream("config.properties");
			conf.load(config);
			config.close();
		} catch (Exception e) {
			System.out.println("Couldn't load configuration file.\nUsing default values.");
		}

		String strInterface = (conf.getProperty("Interface") == null) ? "0.0.0.0" : conf.getProperty("Interface");

		Interface = InetAddress.getByName(strInterface);

		Port = (conf.getProperty("Port") == null) ? new Integer(4242) : Integer.parseInt(conf.getProperty("Port"));

		debugLevel = (conf.getProperty("debugLevel") == null) ? new Integer(2)
				: Integer.parseInt(conf.getProperty("debugLevel"));

		url = (conf.getProperty("url") == null) ? "https://cdm.ram.m2m.telekom.com/" : conf.getProperty("url");

		namePrefix = (conf.getProperty("namePrefix") == null) ? "HfTL-Group2" : conf.getProperty("namePrefix");

		Console console = System.console();
		if (console != null) {
			// Eclipse is being retarded again with bugs from 2008
			// this prompt will only show when running the jar as eclipse
			// doesn't attach a proper console
			System.out.print("Enter your username: ");
			String username = console.readLine();

			System.out.print("Enter your password: ");
			char[] password = console.readPassword();

			String authorization = username + ":" + new String(password);
			encodedAuth = Base64.encode(authorization.getBytes());

			if (igsam.debugLevel >= 3) {
				System.out.println("encodedAuth: " + encodedAuth);
			}
		} else {
			// fallback for running in debug mode inside the IDE
			encodedAuth = "SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw";
		}

		// Verify that the provided credentials are OK. Stop execution if not
		cloudConnection testCredentials = new cloudConnection(url);

		try {
			testCredentials.setAuthorization(encodedAuth);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message.equalsIgnoreCase("Invalid Credentials")) {
				System.out.println("Credentials are invalid!");
			}
			return;
		}

		if (igsam.debugLevel >= 1) {
			System.out.println("Interface: " + Interface.toString());
			System.out.println("Port: " + Port);
			System.out.println("debugLevel: " + debugLevel);
			System.out.println("Url: " + url);
			System.out.println("namePrefix: " + namePrefix);
		}

		// configuration read. Starting server.

		ServerSocket serversocket;

		try {
			writeDebug("Trying to bind" + Interface + " on " + Port + ".", 2);
			serversocket = new ServerSocket(Port, 0, Interface);
		} catch (Exception e) {
			System.out.println("I/O Error while binding to socket: " + e.getMessage());
			return;
		}
		writeDebug("\nReady. Wait for request...\n", 2);

		// Dispatch loop
		while (true) {
			try {
				// Waiting for incoming client requests
				Socket connectionSocket = serversocket.accept();

				cloudConnection connectionObject = new cloudConnection(namePrefix);
				connectionObject.setAuthorization(encodedAuth);

				listener runnable = new listener(connectionObject, connectionSocket);

				new Thread(runnable).start();
			} catch (Exception e) {
				System.out.println("Server thread error : " + e.getMessage());
			}
		}
	}

	public static void testCdd(String encodedAuth, String serial, String url) throws Exception {

		cloudConnection connectCdd = new cloudConnection(url);
		connectCdd.setAuthorization(encodedAuth);

		// 1337 -> Sensor-Serial | id -> assigned ID by CdD
		String id = connectCdd.getDevice(serial);

		if (id == null) {
			id = connectCdd.addDevice(serial);
			connectCdd.registerDevice(serial, id);
		}

		String strTime = getDateTimeStamp();
		//connectCdd.sendData(id, (float) 13.37, strTime);
		connectCdd.sendEvent(id, (float) 73.9, (float) 6.151782, (float) 51.211971, strTime, "Location updated",
				"queclink_GV200LocationUpdate");

		// "source": { "id": "10400" },
		// "text": "Tracker lost power",
		// "time": "2013-08-19T21:31:22.740+02:00",
		// "type": "c8y_PowerAlarm",
		// "status": "ACTIVE",
		// "severity": "MAJOR",

		String AlarmID;
		AlarmID = connectCdd.sendAlarms(id, "Tracker lost power", strTime, "c8y_PowerAlarm", "ACTIVE", "MAJOR");

		Thread.sleep(20000);

		connectCdd.updateAlarmStatus(AlarmID, "CLEARED");

		System.out.println(id);

	}

	public static String getDateTimeStamp() {
		// get Timestamp 2014-12-15T13:00:00.123
		LocalDateTime timestamp = LocalDateTime.now();

		// get offset "+02:00"
		TimeZone zone = TimeZone.getDefault();

		long hours = TimeUnit.MILLISECONDS.toHours(zone.getOffset(new Date().getTime()));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(zone.getOffset(new Date().getTime()))
				- TimeUnit.HOURS.toMinutes(hours);

		String timeZoneString = String.format("+%02d:%02d", hours, minutes);

		// get timestamp 2014-12-15T13:00:00.123+02:00
		return timestamp.toString() + timeZoneString;

	}

	public static void writeDebug(String msg, int level) {
		if (level <= igsam.debugLevel) {
			// if used level small eq. configured level
			System.out.println(msg);
		}
	}
}
