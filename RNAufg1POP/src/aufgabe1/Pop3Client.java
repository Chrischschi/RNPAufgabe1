package aufgabe1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import aufgabe1.hosts.MailAccount;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

public class Pop3Client {

	private Socket clientSocket; // TCP-Standard-Socketklasse

	private DataOutputStream outToServer; // Ausgabestream zum Server
	private BufferedReader inFromServer; // Eingabestream vom Server

	private Trace sTrace = new SystemTrace();

	boolean error;

	public Pop3Client() {
		sTrace.setDebug(Proxy.DEBUG);
	}

	public void getMails(MailAccount account) {
		List<String> mailContent;

		int numberOfMails;
		error = false;

		/* Ab Java 7: try-with-resources mit automat. close benutzen! */

		// Socket, inklusive Streams, einrichten
		openSocket(account);

		if (!error) {

			login(account);

			if (!error) {

				numberOfMails = getNumberOfMails();

				for (int i = 1; !error && i <= numberOfMails; i++) {
					mailContent = getMail(i);
					if(!error){
					saveMailContent(mailContent, Proxy.MAIL_DIRECTORY + "\\"
							+ System.currentTimeMillis());
					}
				}
			}
		}
		/* Socket-Streams schließen --> Verbindungsabbau */
		try {
			error = true;
			clientSocket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by server!");
		}

		sTrace.debug("TCP Client stopped!");
	}

	private void openSocket(MailAccount account) {
		try {
			/* Socket erzeugen --> Verbindungsaufbau mit dem Server */
			clientSocket = new Socket(account.serverAddress, account.portNo);
			/* Socket-Basisstreams durch spezielle Streams filtern */
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			if (readFromServer().startsWith("-ERR")) {
				throw new Exception("Server returned Error");
			}

		} catch (IOException e) {
			error = true;
			sTrace.error("Could not recieve Socket Streams");
			e.printStackTrace();
		} catch (Exception e) {
			error = true;
			sTrace.error("Could not connect to Server: "
					+ account.serverAddress.getHostName() + "\n"
					+ e.getMessage());
		}
	}

	private void login(MailAccount account) {
		try {
			// send username
			writeToServer("USER " + account.userName);

			if (readFromServer().startsWith("-ERR")) {
				// Server returned -ERR -> abort
				throw new Exception("Username refused");
			}
			// send password
			writeToServer("PASS " + account.passwd);

			if (readFromServer().startsWith("-ERR")) {
				// Server returned -ERR -> abort
				throw new Exception("Password refused");
			}
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.login()\n" + e.getMessage());
		} catch (Exception e) {
			error = true;
			sTrace.error("Could not login: "
					+ account.serverAddress.getHostName() + " : "
					+ account.userName + "\n" + e.getMessage());
		}
	}

	private int getNumberOfMails() {
		int n = 0;
		String response;
		try {
			writeToServer("STAT");
			response = readFromServer();
			if (response.startsWith("-ERR")) {
				// Server returned -ERR -> abort
				throw new Exception("Server returned -ERR on \"STAT\" command");
			}
			n = Integer.parseInt(response.split(" ")[1]);
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.getNumberofMails()\n" + e.getMessage());
		} catch (Exception e) {
			error = true;
			sTrace.error(e.getMessage());
		}
		return n;
	}

	private List<String> getMail(int n) {
		List<String> mailContent = new ArrayList<String>();
		String response = "";
		try {
			writeToServer("RETR " + n);
			boolean noFullstop = true;
			if (response.startsWith("-ERR")) {
				
			} else {
				do {
					response = readFromServer();
					if (response.equals(".")) {
						noFullstop = false;
					} else {
						if (response.startsWith("..")) {
							response = response.substring(1);
						}
						mailContent.add(response);
					}
				} while (noFullstop);
			}
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.getMail()\n" + e.getMessage());
		}
		return mailContent;
	}

	private void saveMailContent(List<String> mailContent, String file) {
		// TODO Dateien Speichern
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
