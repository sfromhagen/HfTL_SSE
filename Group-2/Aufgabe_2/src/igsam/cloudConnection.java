package igsam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;

import java.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;


public class cloudConnection {

	//todo: read url from config file
	private String url ="";
	private String user;
	private String password;
	
	public cloudConnection (String url) throws Exception{
		this.url = url;
	}
	
	
	public void login (String user, String password){
		this.user = user;
		this.password = password;		
	}
	
	public String getDevice (String serial) throws Exception{
		
		URL targetUrl  = new URL(igsam.url+"/identity/externalIds/c8y_Serial/"+serial);
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
				
		connection.setRequestMethod("GET");
				
		//todo: Base64 encoding 
		//String encodedPassword = user + ":" + password;
        //String encoded = Base64.
        //connection.setRequestProperty("Authorization", "Basic "+encoded);
		
		connection.setRequestProperty("Authorization", "Basic SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.externalId+json,application/vnd.com.nsn.cumulocity.error+json; charset=UTF-8;ver=0.9");

		connection.connect();
		
		BufferedReader inputStream = null;
		if (connection.getResponseCode() == connection.HTTP_OK){
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
		}else{
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
		}
		
		
		
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
		
		connection.setRequestProperty("Authorization", "Basic SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw");
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

		connection.setRequestProperty("Authorization", "Basic SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw");
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
	
	
	public String sendData (String tenantId, float value, String timestamp ) throws Exception{

		URL targetUrl = new URL(igsam.url+"measurement/measurements");
		HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/vnd.com.nsn.cumulocity.measurement+json; charset=UTF-8;ver=0.9");
		connection.setRequestProperty("Accept", "application/vnd.com.nsn.cumulocity.measurement+json; charset=UTF-8; ver=0.9");

		connection.setRequestProperty("Authorization", "Basic SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw");
		connection.setDoOutput(true);
		
		
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
		//out.write(body.getBytes("UTF-8"));
		out.write(requestObj.toJSONString().getBytes("UTF-8"));
		out.close();
		
		BufferedReader inputStream;
		
		if (connection.getResponseCode() == connection.HTTP_CREATED){
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
			System.out.println("ACCEPTED");
		}else{
			inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getErrorStream()));
			System.out.println("DENY");
		}
		
		
		
	 
		
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
	
	
	
	
	
}