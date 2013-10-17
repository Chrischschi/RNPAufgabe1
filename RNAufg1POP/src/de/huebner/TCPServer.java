package de.huebner;

/*
 * TCPServer.java
 *
 * Version 2.0
 * Autor: M. Hübner HAW Hamburg (nach Kurose/Ross)
 * Zweck: TCP-Server Beispielcode:
 *        Bei Dienstanfrage einen Arbeitsthread erzeugen, der eine Anfrage bearbeitet:
 *        einen String empfangen, in Großbuchstaben konvertieren und zurücksenden
 */
import java.io.*;
import java.net.*;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

public class TCPServer {
	/* Server, der Verbindungsanfragen entgegennimmt */
	public static final int SERVER_PORT = 6789;
	
	public static void main(String[] args) {
		ServerSocket welcomeSocket; // TCP-Server-Socketklasse
		Socket connectionSocket; // TCP-Standard-Socketklasse
		
		Trace sTrace = new SystemTrace();
		sTrace.setDebug(Proxy.DEBUG);
		
		int counter = 0; // Zählt die erzeugten Bearbeitungs-Threads

		try {
			/* Server-Socket erzeugen */
			welcomeSocket = new ServerSocket(SERVER_PORT);

			while (true) { // Server laufen IMMER
				sTrace.debug("TCP Server: Waiting for connection - listening TCP port "
								+ SERVER_PORT);
				/*
				 * Blockiert auf Verbindungsanfrage warten --> nach
				 * Verbindungsaufbau Standard-Socket erzeugen und
				 * connectionSocket zuweisen
				 */
				connectionSocket = welcomeSocket.accept();

				/* Neuen Arbeits-Thread erzeugen und den Socket übergeben */
				(new TCPServerThread(++counter, connectionSocket)).start();
			}
		} catch (IOException e) {
			sTrace.error(e.toString());
		}
	}
}

class TCPServerThread extends Thread {
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

	public TCPServerThread(int num, Socket sock) {
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
}