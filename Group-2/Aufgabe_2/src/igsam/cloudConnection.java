package igsam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


public class cloudConnection {

	//todo: read url from config file
	private String url ="";
	private HttpsURLConnection connection;
	
	public cloudConnection (String url) throws Exception{
		this.url = url;
		
		//connect to url
		
		// http://www.programcreek.com/java-api-examples/javax.net.ssl.HttpsURLConnection
		
		  URL uploadUrl  = new URL(url);
		  HttpsURLConnection connection = (HttpsURLConnection) uploadUrl.openConnection();
		  
		  connection.setRequestMethod("GET");
		  connection.connect();
		  
		  BufferedReader inputStream = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
		  
		  this.connection = connection;
		  
		  //test print response 
		  if (igsam.debugLevel == 3){
			  
			  String line = inputStream.readLine();
			  
			  while (line != null)
			  {  
				 System.out.println(line);
				 line = inputStream.readLine();
			  }
		  }// end if 
		
	}
	
	
	public void login (String user, String password){
	
		
	}
	
	public void addDevice (int id, String password, String tenantId){
	
		connection.setRequestMethod(POST);
		
		
		
		
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