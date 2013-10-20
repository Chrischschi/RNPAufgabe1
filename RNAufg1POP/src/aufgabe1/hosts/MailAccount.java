package aufgabe1.hosts;

import java.net.InetAddress;

import aufgabe1.Proxy;


/** Immutable-Repräsentation eines Mail-Accounts auf einem Host , daher auch feldzugriffe statt getter*/
public class MailAccount {
	
	public final String userName; 
	public final String passwd; 
	
	public final InetAddress serverAddress; 
	public final int portNo;
	
	public MailAccount(String userName, String passwd, InetAddress serverAdress){
		this(userName, passwd, serverAdress, Proxy.DEFAULT_PORT);
	}
	
	public MailAccount(String userName, String passwd, InetAddress serverAddress, int portNo) {
		this.userName = userName;
		this.passwd = passwd;
		this.serverAddress = serverAddress;
		this.portNo = portNo;
	}


}
