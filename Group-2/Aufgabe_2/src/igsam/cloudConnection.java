package igsam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;


public class cloudConnection {

	private String encodedAuth;
	private String namePrefix;
	
	public cloudConnection (String prefix) throws Exception{
		this.namePrefix = prefix;
	}
	
	public void setAuthorization (String encoded){
		this.encodedAuth = encoded;
	}
	
	public String getDevice (String serial) throws Exception{
		
		URL targetUrl  = new URL(igsam.url+"/identity/externalIds/c8y_Serial/"+serial);
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		//built request params
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.externalId+json,application/vnd.com.nsn.cumulocity.error+json; charset=UTF-8;ver=0.9");

		//send request to server
		connection.connect();
		
		BufferedReader inputStream = null;
		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
			//read response from server
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
			igsam.writeDebug("[getDevice] Got 200 OK response.", 2);
		}else{
			//catch errors
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
			igsam.writeDebug("[getDevice] Got error response.", 2);
		}
		
		
		//response is JSON objects, initialise parser
		JSONParser parser = new JSONParser();
		JSONObject jObj = null;

		try {
				
			StringBuilder sb = new StringBuilder();
			String line = null;
            while ((line = inputStream.readLine()) != null) {
            	sb.append(line + "\n");
            }
            inputStream.close();
            String json = sb.toString();
            //now JSON response from server is in string json
            
            jObj = (JSONObject)parser.parse(json);

			}catch (Exception e){
				e.printStackTrace();
			}
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
				igsam.writeDebug("[getDevice] Receive JSON response: " + jObj.toJSONString(), 2);
				return (String) ((JSONObject)jObj.get("managedObject")).get("id");
			} else {
				return null;
			}
	}
			
	
	public String addDevice (String tenantId) throws Exception{
	
		URL targetUrl  = new URL(igsam.url+"inventory/managedObjects");
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8; ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8; ver=0.9");		
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);
	
		JSONObject obj = new JSONObject();
		JSONObject list = new JSONObject();
		obj.put("c8y_IsDevice", list);
		obj.put("name", namePrefix+"_"+tenantId);
		
		OutputStream out = connection.getOutputStream();
		out.write(obj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		igsam.writeDebug("[addDevice] Sending POST with JSON body: " + obj.toJSONString(), 2);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
		
		  JSONParser parser = new JSONParser();
		  JSONObject jObj = null;
		  
			try {
				
				StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = inputStream.readLine()) != null) {
                    sb.append(line + "\n");
                }
                inputStream.close();
                String json = sb.toString();
                
                jObj = (JSONObject)parser.parse(json);

			}catch (Exception e){
				e.printStackTrace();
			}
			
			//check response from server.
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED){
				//read response from server
				igsam.writeDebug("[addDevice] Got 201 CREATED response.",2);
				igsam.writeDebug("[addDevice] Receive JSON response: " + obj.toJSONString(), 2);
				return (String) jObj.get("id");
			}else{
				igsam.writeDebug("[addDevice] Got error response.",2);
				return null;
			}
	}


	public void registerDevice (String tenantId, String id) throws Exception{

		URL targetUrl = new URL(igsam.url+"identity/globalIds/"+id+"/externalIds");
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.externalId+json; charset=UTF-8; ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.externalId+json; charset=UTF-8; ver=0.9");

		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);
		

		JSONObject obj = new JSONObject();
		
		JSONArray list = new JSONArray();
		obj.put("type", "c8y_Serial");
		obj.put("externalId", tenantId);
		
		OutputStream out = connection.getOutputStream();
		out.write(obj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
		
		  JSONParser parser = new JSONParser();
		  JSONObject jObj = null;
		  
			try {
				
				StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = inputStream.readLine()) != null) {
                    sb.append(line + "\n");
                }
                inputStream.close();
                String json = sb.toString();
                
                jObj = (JSONObject)parser.parse(json);

			}catch (Exception e){
				e.printStackTrace();
			}
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED){
				//read response from server
				igsam.writeDebug("[registerDevice] Got 201 CREATED response.",2);
				igsam.writeDebug("[registerDevice] Receive JSON response: " + jObj.toJSONString(), 2);
			}else{
				igsam.writeDebug("[registerDevice] Got error response.",2);
				throw new Exception();
			}	
	}

	public void sendData (String tenantId, float value, String timestamp ) throws Exception{

		URL targetUrl = new URL(igsam.url+"measurement/measurements");
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.measurement+json; charset=UTF-8;ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.measurement+json; charset=UTF-8; ver=0.9");
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);

		// Example JSON-Object to build:
		//	{
		//		"c8y_TemperatureMeasurement":{
		//			"T": {
		//				"value": 21.23,
		//				"unit":"C"
		//				}
		//		},
		//		"time": "2014-12-15T13:00:00.123+02:00",
		//		"source": {
		//			"id": "1231234"
		//			},
		//		"type":"c8y_PTCMeasurement"
		//	}

		JSONObject requestObj = new JSONObject();
		JSONObject mainObj = new JSONObject();
				
		JSONObject tObj = new JSONObject();
		
		tObj.put("value", value);
		tObj.put("unit", "C");
		
		mainObj.put("T", tObj);
		
		requestObj.put("c8y_TemperatureMeasurement", mainObj);
		
		requestObj.put("time", timestamp);
		
		JSONObject sourceObj = new JSONObject();
		
		sourceObj.put("id", tenantId);	
		requestObj.put("source", sourceObj);
		requestObj.put("type", "c8y_PTCMeasurement");
		
		igsam.writeDebug("[sendData] JSON request object: "+ requestObj.toJSONString(),2);
		
		OutputStream out = connection.getOutputStream();
		out.write(requestObj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED){
			//read response from server
			igsam.writeDebug("[sendData]  Got 201 CREATED response.",2);
		}else{
			igsam.writeDebug("[sendData]  Got error response.",2);
			throw new Exception();
		}
	}
	

	public String sendAlarms (String tenantId, String alarmText, String timestamp, String alarmType, String alarmStatus, String alarmSeverity ) throws Exception{	
		
		String serverAlarmID = "";
		
		URL targetUrl = new URL(igsam.url+"/alarm/alarms");
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.alarm+json; charset=UTF-8;ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.alarm+json; charset=UTF-8; ver=0.9");
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);
		
		//Example Alarm JSON: 
		//		{
		//		"source": { "id": "10400" },
		//		"text": "Tracker lost power",
		//		"time": "2013-08-19T21:31:22.740+02:00",
		//		"type": "c8y_PowerAlarm",
		//		"status": "ACTIVE",
		//		"severity": "MAJOR",
		//		}

		JSONObject requestObj = new JSONObject();
		JSONObject mainObj = new JSONObject();	
		
		mainObj.put("id", tenantId);
		requestObj.put("source", mainObj);
		
		requestObj.put("text", alarmText);
		requestObj.put("time", timestamp);		
		requestObj.put("type", alarmType);
		requestObj.put("status", alarmStatus);
		requestObj.put("severity", alarmSeverity);
		
		igsam.writeDebug("[sendAlarms] JSON request object: "+ requestObj.toJSONString(),2);
		
		OutputStream out = connection.getOutputStream();
		
		out.write(requestObj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		BufferedReader inputStream;
		
		//		HTTP/1.1 201 Created
		//		Content-Type: application/vnd.com.nsn.cumulocity.alarm+json
		//		...
		//		{
		//		"id": "214600",
		//		"self": "https://.../alarm/alarms/214600",
		//		...
		//		}
		
		if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED){
			
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
			
			  JSONParser parser = new JSONParser();
			  JSONObject jObj = null;
			  
				try {
					
					StringBuilder sb = new StringBuilder();
	                String line = null;
	                while ((line = inputStream.readLine()) != null) {
	                    sb.append(line + "\n");
	                }
	                inputStream.close();
	                String json = sb.toString();
	                
	                jObj = (JSONObject)parser.parse(json);

				}catch (Exception e){
					e.printStackTrace();
				}
				
				serverAlarmID = (String) jObj.get("id");
			
				igsam.writeDebug("[sendAlarms]  Got 201 CREATED response.",2);
				igsam.writeDebug("[sendAlarms]  ALARM_CREATED, ID: " + serverAlarmID,2);
			
		}else{
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
			igsam.writeDebug("[sendAlarms]  Got error response.",2);
		}
		return serverAlarmID;
	}

	public void updateAlarmStatus (String serverAlarmID, String alarmStatus ) throws Exception{	
		
		URL targetUrl = new URL(igsam.url+"/alarm/alarms/"+serverAlarmID);
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.alarm+json; charset=UTF-8;ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.alarm+json; charset=UTF-8; ver=0.9");
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);
		
		//Example:
		//		PUT /alarm/alarms/ID HTTP/1.1
		//		Content-Type: application/vnd.com.nsn.cumulocity.alarm+json
		//		Accept: application/vnd.com.nsn.cumulocity.alarm+json
		//		...
		//		{
		
		//		"status": "CLEARED",
		//		}

		JSONObject requestObj = new JSONObject();
		requestObj.put("status", alarmStatus);
		
		igsam.writeDebug("[updateAlarmStatus] JSON request object: "+ requestObj.toJSONString(),2);

		OutputStream out = connection.getOutputStream();
		
		out.write(requestObj.toJSONString().getBytes("UTF-8"));
		out.close();

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
			//read response from server
			igsam.writeDebug("[updateAlarmStatus]  Got 200 OK response.",2);
		}else{
			igsam.writeDebug("[updateAlarmStatus]  Got error response.",2);
			throw new Exception();
		}
	}

	
	public void sendEvent (String tenantId, float altValue,float lngValue,float latValue, String timestamp, String msg, String type) throws Exception{

		URL targetUrl = new URL(igsam.url+"event/events");
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.event+json; charset=UTF-8;ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.event+json; charset=UTF-8; ver=0.9");
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);
			
//		 POST /event/events HTTP/1.1
//		 Content-Type: application/vnd.com.nsn.cumulocity.event+json
//		 ...	
//		 {
//		 	"source": { "id": "1197500" },
//		 	"text": "Location updated",
//		 	"time": "2013-07-19T09:07:22.598+02:00",
//			"type": "queclink_GV200LocationUpdate",
//		 	"c8y_Position": {
//		 		"alt": 73.9,
//		 		"lng": 6.151782,
//		 		"lat": 51.211971
//		 	}
//		 }	
		
		JSONObject requestObj = new JSONObject();
		JSONObject sourceObj = new JSONObject();
		JSONObject posObj = new JSONObject();
		
		sourceObj.put("id", tenantId);
		requestObj.put("source", sourceObj);
		
		requestObj.put("text", msg);	
		requestObj.put("time", timestamp);
		requestObj.put("type", type);
		
		posObj.put("alt", altValue);
		posObj.put("lng", lngValue);
		posObj.put("lat", latValue);
		requestObj.put("c8y_Position", posObj);

		igsam.writeDebug("[sendEvent] JSON request object: "+ requestObj.toJSONString(),2);
		
		OutputStream out = connection.getOutputStream();
		
		out.write(requestObj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED){
			//read response from server
			igsam.writeDebug("[sendEvent]  Got 201 CREATED response.",2);
		}else{
			igsam.writeDebug("[sendEvent]  Got error response.",2);
			throw new Exception();
		}	 
	}	
}