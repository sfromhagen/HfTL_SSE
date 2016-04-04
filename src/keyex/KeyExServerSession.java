package keyex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.DHParameterSpec;

import java.security.KeyPairGenerator;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.SecureRandom;

public class KeyExServerSession implements Runnable{

    private KeyExConnection connectionObject;
    private DHParameterSpec dhSpec;
    
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();
    	
    	System.out.println("\nIm a server doing server stuff");
    	
    	// We need to come up with fresh Diffie Hellman (DH) Parameters
    	// 
    	initialize_DH();
        
    	// 
    	transmit_keys(); 
        
        // Close all sockets and make sure connectionObject is ready to close
        clean_up(); 
    }
    
    private void initialize_DH() {
	
	    int mode = 0;
	    try {
		    if (mode == 0) {
			    // Some central authority creates new DH parameters
			    System.out.println("Creating Diffie-Hellman parameters ...");
			    AlgorithmParameterGenerator paramGen =
			    AlgorithmParameterGenerator.getInstance("DH");
			    paramGen.init(512);
			    AlgorithmParameters params = paramGen.generateParameters();
			    dhSpec =
			    (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
			    System.out.println("" + dhSpec.getP() + "," + dhSpec.getG() + "," + dhSpec.getL());
		    }
	    }
	    catch (Exception e) {
	    	System.out.println("Error while generating DH Parameters");
	    	e.printStackTrace();
	    }
	    finally{
	    	System.out.println("DH parameters successfully generated.");
	    }
	    
    }
    
    private void transmit_keys() {

        try {
        	PrintWriter out =
                    new PrintWriter(connectionObject.connectionSocket.getOutputStream(), true);
            out.println(new Date().toString());
        }
        catch (Exception e) {
            System.out.println("error!" + e.getMessage());
        }  
    }
    private void clean_up() {

        connectionObject.prepareStreams();
    }
}