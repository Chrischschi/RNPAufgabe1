package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class POP3Server {
	
	public static final int POP3_TEST_PORT_NUMBER = 11000;  
	
	public ServerSocket welcomeSocket;
	
	public Socket connectionSocket;
	
	public POP3Server(){
		
		try {
			welcomeSocket = new ServerSocket(POP3_TEST_PORT_NUMBER);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

	}
	
	
	public void listenForRequests() {
		
		
		
	}
	
	public static void main(String[] args) {
		POP3Server server = new POP3Server(); 
		try {
			server.connectionSocket = server.welcomeSocket.accept(); //dem server auf seinen "gesprächspartner" warten lassen
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		
		
		
		
		
	}
	
	
	
	public void computeCommand(String msg){
		String cmd = msg.toUpperCase().substring(0, 3);
		switch(cmd){
		case("USER"):
		case("PASS"):
		case("QUIT"):
		case("STAT"):
		case("LIST"):
		case("RETR"):
		case("DELE"):
		case("NOOP"):
		case("RSET"):
		case("UIDL"):
		default: // Crash and Burn ;)
		}
	}
}
