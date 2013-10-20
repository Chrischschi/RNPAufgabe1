package aufgabe1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

import aufgabe1.hosts.MailAccount;

public class Proxy {
	
	/** Hauptklasse des POP3-Proxyservers */

	public static final int DEFAULT_PORT = 11000;
	
	public static final boolean DEBUG = true;	
	
	public static final String MAIL_DIRECTORY = "";
	
	private Pop3Client client = new Pop3Client();
	
	private POP3Server server = new POP3Server();
	
	private Trace sTrace = new SystemTrace();
	
	List<MailAccount> accountList = new ArrayList<MailAccount>();
	
	public Proxy(){
		String hostName = "pop3.test.net";
		InetAddress address;
		try {
			address = InetAddress.getByName(hostName);
			accountList.add(new MailAccount("TestName", "pass", address));
			accountList.add(new MailAccount("abc", "def", address));
		} catch (UnknownHostException e) {
			sTrace.error("Unknown Host: " + hostName);
		}
		hostName = "pop3.web.de";
		try {
			address = InetAddress.getByName(hostName);
			accountList.add(new MailAccount("TestName", "pass", address));
			accountList.add(new MailAccount("abc", "def", address));
		} catch (UnknownHostException e) {
			sTrace.error("Unknown Host: " + hostName);
		}
		
		
		
	}
	
	
	
	public void start(){
		server.start();
	}
	
	public static void main(String[] args) {
		Proxy proxy = new Proxy();
		proxy.start();
	}

}
