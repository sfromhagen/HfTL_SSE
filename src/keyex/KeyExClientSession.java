package keyex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.SecureRandom;

/*
 * @author: Michael Stegemann <m.stegemann_beng-tki2009k@t-online.de>
 * @version 1.0
 */

public class KeyExClientSession implements Runnable{

    private KeyExConnection connectionObject;
    
    public void setConnection(KeyExConnection object){
        this.connectionObject = object;
    } 
    @Override
    public void run() {
    	
    	connectionObject.prepareStreams();
    	
    	System.out.println("\nIm a client doing client stuff");
        receive_handshake(); 
    }
    
    private void receive_handshake() {
    	try{
    		String answer = connectionObject.input.readLine();
    		System.out.println(answer);
    	} catch (Exception e){
    		e=e;
    		
    	}

    }

}