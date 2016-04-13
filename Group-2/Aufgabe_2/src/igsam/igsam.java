package igsam;

public class igsam {

	//todo read debug level via ini file
	public static int debugLevel = 0;
	
	
	public static void main(String[] args) throws Exception{
		
		

		
		//todo read url via config file
		String url2 = "https://cdm.ram.m2m.telekom.com/apps/devicemanagement/index.html";
		String url = "https://cdm.ram.m2m.telekom.com/inventory/managedObjects";
		
		
		cloudConnection connectCdd = new cloudConnection(url);
		
		
		
		//connectCdd.login();
		
		
	}
}
