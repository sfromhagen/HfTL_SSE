package igsam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;


public class cloudConnection {

	//todo: read url from config file
	private String url ="";
	private HttpsURLConnection connection;
	private String user;
	private String password;
	
	public cloudConnection (String url) throws Exception{
		this.url = url;
		
		//connect to url
		
		// http://www.programcreek.com/java-api-examples/javax.net.ssl.HttpsURLConnection
		
		  URL uploadUrl  = new URL(url);
		  HttpsURLConnection connection = (HttpsURLConnection) uploadUrl.openConnection();
		  
//		 connection.setRequestMethod("GET");
//		  connection.connect();
//		  
//		  BufferedReader inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
//		  
		  this.connection = connection;
//		  
//		  //test print response 
//		  if (igsam.debugLevel == 3){
//			  
//			  String line = inputStream.readLine();
//			  
//			  while (line != null)
//			  {  
//				 System.out.println(line);
//				 line = inputStream.readLine();
//			  }
//	
//		  }// end if 
//		  inputStream.close();
	}
	
	
	public void login (String user, String password){
	
	this.user = user;
	this.password = password;
		
	}
	
	public void addDevice (String tenantId) throws Exception{
	
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
		String body = "{\r\n \"c8y_IsDevice\" : {},\r\n\"name\" : \"TestDeviceGr2\"\r\n}";

		OutputStream out = connection.getOutputStream();
		out.write(body.getBytes("UTF-8"));
		out.close();
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
		  
		 // this.connection = connection;
		  
		  //test print response 
		  //if (igsam.debugLevel == 3){
			  
			  String line = inputStream.readLine();
			  
			  while (line != null)
			  {  
				 System.out.println(line);
				 line = inputStream.readLine();
			  }
		//  }// end if 
		
		
		
		
		
		
		
		
		
		
		//    cdn/HfTL-Group-2:GehHeim1310
		// Base 64 encoder  ->> https://www.base64encode.org/
		//    Y2RuL0hmVEwtR3JvdXAtMjpHZWhIZWltMTMxMA==
			  //
			  
			  //HfTL-Group-2:GehHeim1310
			  // SGZUTC1Hcm91cC0yOkdlaEhlaW0xMzEw
			  
			  
		
//		POST /inventory/managedObjects HTTP/1.1
//		Content-Type: application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-
//		8; ver=0.9
//		Accept: application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-8;
//		ver=0.9
//		Authorization: Basic <<Base64 encoded credentials <tenant
//		ID>/<username>:<password> >>
//		...
//		{
//		"c8y_IsDevice" : {},
//		"name" : "HelloWorldDevice"
//		}
//		
//		
//		Response 
//		You will receive a response like that:
//			HTTP/1.1 201 Created
//			Content-Type: application/vnd.com.nsn.cumulocity.managedObject+json; charset=UTF-
//			8; ver=0.9
//			Authorization: Basic <<Base64 encoded credentials <tenant
//			ID>/<username>:<password> >>
//			...
//			{
//			"id": "1231234"
//			"lastUpdated": "2014-12-15T14:58:26.279+01:00",
//			"name": "HelloWorldDevice",
//			"owner": "<username>",
//			"self": "https://<tenant-
//			ID>.cumulocity.com/inventory/managedObjects/1231234",
//			"c8y_IsDevice": {},
//			...
//			}
		
		
	}
	
	
}