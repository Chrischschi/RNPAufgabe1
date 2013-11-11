package aufgabe1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

import aufgabe1.hosts.MailAccount;

public class Proxy {
	
	/** Hauptklasse des POP3-Proxyservers */
	
	public static final int DEFAULT_PORT = 11000;
	
	public static final boolean DEBUG = true;	
	
	public static final Path MAIL_STORAGE_PATH = Paths.get("mail_storage/"); //Ordner wo die dateien gespeichert werden
	
	private Pop3Client client = new Pop3Client();
	
	//Timer zum Starten des Client Threads
	Timer timer = new Timer();
	
	//Trace zum erstellen eines Logs
	private Trace sTrace = new SystemTrace();
	
	//Liste der Accounts von denen Mails geholt werden sollen
	List<MailAccount> externAccounts = new ArrayList<MailAccount>();
	
	public Proxy(){
		sTrace.setDebug(DEBUG);
		String hostName = "lab30.cpt.haw-hamburg.de";
		InetAddress address;
		try {
			address = InetAddress.getByName(hostName);
			externAccounts.add(new MailAccount("bai4rnpE", "FpHBfTgM", address, 11000));
		} catch (UnknownHostException e) {
			sTrace.error("Unknown Host: " + hostName);
		}
		hostName = "lab31.cpt.haw-hamburg.de";
		try {
			address = InetAddress.getByName(hostName);
			externAccounts.add(new MailAccount("bai4rnpE", "FpHBfTgM", address, 11000));
		} catch (UnknownHostException e) {
			sTrace.error("Unknown Host: " + hostName);
		}
		
		
		
	}
	
	
	
	public void start(){
		//Alle 30 Sekunden mails holen
		sTrace.debug("Start MailTask");
		timer.schedule(new MailTask(), 0, 30*1000);
		(new POP3Server()).start();
		
	}
	
	public static void main(String[] args) {
		Proxy proxy = new Proxy();
		System.out.println("Start");
		proxy.start();
	}

	class MailTask extends TimerTask {
        public void run() {
        	sTrace.debug("Getting Mails");
        	for (MailAccount acc:externAccounts){
        		sTrace.debug("Account " + acc.userName);
        		client.getMails(acc);
        	}
        }
    }
}
