package JonasPhilMalte;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class GenerateKeypair {
	
	public KeyPair neuesKeypair() throws Exception{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		keyGen.initialize(1024, random);
		return (keyGen.generateKeyPair());
	}	
	
}
