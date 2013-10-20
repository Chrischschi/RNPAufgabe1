package aufgabe1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.huebner.Proxy;
import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;


public class POP3Server extends Thread {
	
	public static final int POP3_TEST_PORT_NUMBER = 11000;  
	
	public ServerSocket welcomeSocket; // TCP-Server-Socketklasse
	
	public Socket connectionSocket; // TCP-Standard-Socketklasse
	
	public POP3Server(){
		
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
				(new POP3ServerThread(++counter, connectionSocket)).start();
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
	
	Trace sTrace = new SystemTrace();

	private BufferedReader inFromClient;
	private DataOutputStream outToClient;

	boolean serviceRequested = true; // Arbeitsthread beenden?

	public POP3ServerThread(int num, Socket sock) {
		/* Konstruktor */
		this.name = num;
		this.socket = sock;
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

			while (serviceRequested) {
				/* String vom Client empfangen und in Großbuchstaben umwandeln */
				capitalizedSentence = readFromClient().toUpperCase();

				/* Modifizierten String an Client senden */
				writeToClient(capitalizedSentence);

				/* Test, ob Arbeitsthread beendet werden soll */
				if (capitalizedSentence.indexOf("QUIT") > -1) {
					serviceRequested = false;
				}
			}

			/* Socket-Streams schließen --> Verbindungsabbau */
			socket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by client!");
		}

		sTrace.debug("TCP Server Thread " + name + " stopped!");
	}

	private String readFromClient() throws IOException {
		/* Lies die nächste Anfrage-Zeile (request) vom Client */
		String request = inFromClient.readLine();
		System.out.println("TCP Server Thread detected job: " + request);
		return request;
	}

	private void writeToClient(String reply) throws IOException {
		/* Sende den String als Antwortzeile (mit newline) zum Client */
		outToClient.writeBytes(reply + '\n');
		System.out.println("TCP Server Thread " + name
				+ " has written the message: " + reply);
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
