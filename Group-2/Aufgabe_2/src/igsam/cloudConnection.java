package igsam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;


public class cloudConnection {

	private String encodedAuth;
	
	public cloudConnection (String url) throws Exception{
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
		if (connection.getResponseCode() == connection.HTTP_OK){
			//read response from server
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
		}else{
			//catch errors
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
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
			
			if (connection.getResponseCode() == connection.HTTP_OK){
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
		
		//todo: Abfrage ob schon eingeloggt.
		
		//todo: Base64 encoding 
		//String encodedPassword = user + ":" + password;
        //String encoded = Base64.
        //connection.setRequestProperty("Authorization", "Basic "+encoded);
		
		connection.setRequestProperty("Authorization", "Basic "+encodedAuth);
		connection.setDoOutput(true);
		
		
		//String body = "{\r\n \"c8y_IsDevice\" : {},\r\n\"name\" : \"TestDeviceGr2\"\r\n}";

		JSONObject obj = new JSONObject();
		
		JSONArray list = new JSONArray();
		obj.put("c8y_IsDevice", list);
		obj.put("name", "TestDeviceGr2_JSON");
		
		OutputStream out = connection.getOutputStream();
		//out.write(body.getBytes("UTF-8"));
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
			
			return (String) jObj.get("id");
	}
	

	public String registerDevice (String tenantId, String id) throws Exception{

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
		//out.write(body.getBytes("UTF-8"));
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
			
			return (String) jObj.get("id");
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
		//ToDo Celsius in config file?!?!

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
		
		System.out.println(requestObj.toJSONString());

		OutputStream out = connection.getOutputStream();
		
		out.write(requestObj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		BufferedReader inputStream;
		
		if (connection.getResponseCode() == connection.HTTP_CREATED){
			//inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
			System.out.println("ACCEPTED");
		}else{
			//inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
			System.out.println("DENY");
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
		
//		POST /alarm/alarms HTTP/1.1
//		Content-Type: application/vnd.com.nsn.cumulocity.alarm+json
//		Accept: application/vnd.com.nsn.cumulocity.alarm+json
//		...
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
		
		System.out.println(requestObj.toJSONString());

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
		
		
		if (connection.getResponseCode() == connection.HTTP_CREATED){
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
			
			System.out.println("ALARM_CREATED, ID: " + serverAlarmID);
		}else{
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
			System.out.println("DENY");
		}
		 
		return serverAlarmID;
		
		
		
	}

	
	
	
	
}