package aufgabe1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

public class Pop3Client {

	public static final int SERVER_PORT = 6789;

	private Socket clientSocket; // TCP-Standard-Socketklasse

	private DataOutputStream outToServer; // Ausgabestream zum Server
	private BufferedReader inFromServer; // Eingabestream vom Server
	
	Trace sTrace = new SystemTrace();

	private boolean serviceRequested = true; // Client beenden?

	public Pop3Client(){
		sTrace.setDebug(Proxy.DEBUG);
	}
	
	public void startJob(String host) {
		/* Client starten. Ende, wenn quit eingegeben wurde */
		Scanner inFromUser;
		String command; // vom Client übergebener Befehls-String
		 // vom Server übertragene antwort (2 Alternativen)
		String singleLineResponse;
		List<String> multiLineResponse;
		

		/* Ab Java 7: try-with-resources mit automat. close benutzen! */
		try {
			/* Socket erzeugen --> Verbindungsaufbau mit dem Server */
			clientSocket = new Socket(host, SERVER_PORT);

			/* Socket-Basisstreams durch spezielle Streams filtern */
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			/* Konsolenstream (Standardeingabe) initialisieren */
			inFromUser = new Scanner(System.in);

			while (serviceRequested) {
				sTrace.debug("ENTER TCP-DATA: ");
				/* String vom Benutzer (Konsoleneingabe) holen */
				command = inFromUser.nextLine();

				/* String an den Server senden */
				writeToServer(command);

				/* Modifizierten String vom Server empfangen */
				singleLineResponse = readFromServer();

				/* Test, ob Client beendet werden soll */
				if (singleLineResponse.indexOf("QUIT") > -1) {
					serviceRequested = false;
				}
			}

			/* Socket-Streams schließen --> Verbindungsabbau */
			clientSocket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by server!");
		}

		sTrace.debug("TCP Client stopped!");
	}

	private void writeToServer(String request) throws IOException {
		/* Sende eine Zeile zum Server */
		outToServer.writeBytes(request + '\n');
		sTrace.debug("TCP Client has sent the message: " + request);
	}

	private String readFromServer() throws IOException {
		/* Lies die Antwort (reply) vom Server */
		String reply = inFromServer.readLine();
		sTrace.debug("TCP Client got from Server: " + reply);
		return reply;
	}
}
