package aufgabe1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import de.huebner.Proxy;
import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

public class POP3Server extends Thread {

	public static final int POP3_TEST_PORT_NUMBER = 11000;

	public ServerSocket welcomeSocket; // TCP-Server-Socketklasse

	public Socket connectionSocket; // TCP-Standard-Socketklasse
	
	Map<String, String> internAccounts = new HashMap<String,String>();

	public POP3Server() {

		Trace sTrace = new SystemTrace();
		sTrace.setDebug(Proxy.DEBUG);

		int counter = 0; // Zählt die erzeugten Bearbeitungs-Threads

		try {
			/* Server-Socket erzeugen */
			welcomeSocket = new ServerSocket(POP3_TEST_PORT_NUMBER);

			while (true) { // Server laufen IMMER
				sTrace.debug("TCP Server: Waiting for connection - listening TCP port "
						+ POP3_TEST_PORT_NUMBER);
				/*
				 * Blockiert auf Verbindungsanfrage warten --> nach
				 * Verbindungsaufbau Standard-Socket erzeugen und
				 * connectionSocket zuweisen
				 */
				connectionSocket = welcomeSocket.accept();

				/* Neuen Arbeits-Thread erzeugen und den Socket übergeben */
				(new POP3ServerThread(++counter, connectionSocket, internAccounts)).start();
			}
		} catch (IOException e) {
			sTrace.error(e.toString());
		}
	}
}

class POP3ServerThread extends Thread {
	/*
	 * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
	 * erhält
	 */
	private int name;
	private Socket socket;
	
	private String userName = null;

	private Trace sTrace = new SystemTrace();

	private BufferedReader inFromClient;
	private DataOutputStream outToClient;

	private POP3State currentState = POP3State.Authorization;
	
	Map<String, String> internAccounts = new HashMap<String,String>();

	public POP3ServerThread(int num, Socket sock, Map<String,String> accounts) {
		/* Konstruktor */
		this.name = num;
		this.socket = sock;
		this.internAccounts = accounts;
	}

	public void run() {
		String capitalizedSentence;

		sTrace.debug("TCP Server Thread " + name
				+ " is running until QUIT is received!");

		try {
			/* Socket-Basisstreams durch spezielle Streams filtern */
			inFromClient = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			outToClient = new DataOutputStream(socket.getOutputStream());

			writeToClient("+OK Hello Client " + socket.getInetAddress().getHostAddress());

			while (currentState!=POP3State.Update) {
				/* String vom Client empfangen und in Großbuchstaben umwandeln */
				capitalizedSentence = readFromClient().toUpperCase();
				//Nachricht vom Client verarbeiten
				computeMessage(capitalizedSentence);
			}

			/* Socket-Streams schließen --> Verbindungsabbau */
			socket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by client!");
		} catch (IllegalStateException e) {
			sTrace.error("");
			e.printStackTrace();
		}

		sTrace.debug("TCP Server Thread " + name + " stopped!");
	}

	private String computeMessage(String message) throws IllegalStateException{
		String response = "-ERR Should never happen";
		if(message.length()>=4){
		String cmd = message.substring(0, 3);
		switch (cmd) {
		case ("USER"):
			//Benutzer
			if(currentState==POP3State.Authorization){
				response = checkUser(message);
			}else{
				response = "-ERR invalid server state";
			}
			break;
		case ("PASS"):
			//Passwort
			if(currentState==POP3State.Authorization){
				response = checkPass(message);
			}else{
				response = "-ERR invalid server state";
			}
			break;
		case ("QUIT"):
			//Verbindung beenden
			response = "+OK";
			currentState = POP3State.Update;
		case ("STAT"):
			//Nachricht-Uebersicht geben
			if(currentState==POP3State.Transaction){
				
			}else{
				response = "-ERR invalid server state";
			}
		case ("LIST"):
			//Nachrichten auflisten
			if(currentState==POP3State.Transaction){
				
			}else{
				response = "-ERR invalid server state";
			}
		case ("RETR"):
			//Nachricht abrufen
			if(currentState==POP3State.Transaction){
				
			}else{
				response = "-ERR invalid server state";
			}
		case ("DELE"):
			//Nachricht loeschen
			if(currentState==POP3State.Transaction){
				
			}else{
				response = "-ERR invalid server state";
			}
		case ("NOOP"):
			//Ping
			if(currentState==POP3State.Transaction){
				response = "-OK NOOP response";
			}else{
				response = "-ERR invalid server state";
			}
		case ("RSET"):
			if(currentState==POP3State.Transaction){
				response = "-OK NOOP response";
			}else{
				response = "-ERR invalid server state";
			}
		case ("UIDL"):
			if(currentState==POP3State.Transaction){
				response = "-OK NOOP response";
			}else{
				response = "-ERR invalid server state";
			}
		default:
			response = "-ERR unknown command";
		}
		}else{
			response = "-ERR unknown command";
		}
		return response;
	}
	
	private String checkUser(String message){
		String response;
		String[] temp = message.split(" ");
		if(temp.length>=2){
			if(internAccounts.containsKey(temp[1])){
				response = "+OK";
				userName = temp[1];
			}else{
				response = "-ERR User" + temp[1] + " not found";
			}
		}else{
			response = "-ERR No Username found in Message";
		}
		return response;
	}
	
	private String checkPass(String message){
		String response;
		String[] temp = message.split(" ");
		if(temp.length>=2){
			if(userName == null){
				response = "-ERR No User was registered";
			}else if(internAccounts.get(userName).equals(temp[1])){
				//Benutzer hat sich erfolgreich eingeloggt. -> Transaction State
				response = "+OK User " + userName + " logged in";
				currentState = POP3State.Transaction;
			}else{
				response = "-ERR User" + temp[1] + " not found";
			}
		}else{
			response = "-ERR No Username found in Message";
		}
		return response;
	}

	private String readFromClient() throws IOException {
		/* Lies die nächste Anfrage-Zeile (request) vom Client */
		String request = inFromClient.readLine();
		sTrace.debug("TCP Server Thread detected job: " + request);
		return request;
	}

	private void writeToClient(String reply) throws IOException {
		/* Sende den String als Antwortzeile (mit newline) zum Client */
		outToClient.writeBytes(reply + '\n');
		sTrace.debug("TCP Server Thread " + name + " has written the message: "
				+ reply);
	}

	public void computeCommand(String msg) {
		
	}
}
