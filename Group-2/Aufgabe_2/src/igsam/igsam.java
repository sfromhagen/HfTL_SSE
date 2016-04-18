package igsam;

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
		
		System.out.println(id);
		
		
		
		
	}
}
