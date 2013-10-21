package aufgabe1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import aufgabe1.hosts.MailAccount;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

public class Pop3Client {

	public static final Charset USED_CHAR_SET = Charset.forName("US-ASCII");

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
		sTrace.debug("getting Mails");
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
					if (!error) {
						try {
							String fileName = Long.toString(System
									.currentTimeMillis());
							// Die letzten 20 Ziffern der aktuellen Zeit in
							// milisekunden.
							// fileName =
							// fileName.substring(fileName.length()-21);
							saveMail(mailContent, fileName);
						} catch (IOException e) {
							sTrace.debug("Mail konnte nicht gespeichert werden");
						}
					}
					deleteMail(i);
				}
			}
		}
		/* Socket-Streams schlie�en --> Verbindungsabbau */
		try {
			error = true;
			clientSocket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by server!");
		}

		sTrace.debug("TCP Client stopped!");
	}

	private void openSocket(MailAccount account) {
		sTrace.debug("open Socket for Account " + account.userName);
		try {
			/* Socket erzeugen --> Verbindungsaufbau mit dem Server */
			clientSocket = new Socket(account.serverAddress, account.portNo);
			sTrace.debug("socket opened for " + account.userName + " on Port "
					+ account.portNo + " !");
			/* Socket-Basisstreams durch spezielle Streams filtern */
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			sTrace.debug("outToServer Stream opened");
			inFromServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			sTrace.debug("inFromServer Stream opened");
			String blub = readFromServer();
			sTrace.debug("Server Welcome message: " + blub);
			if (blub.startsWith("-ERR")) {
				sTrace.error("Server returned -ERR as Welcome");
				throw new Exception("Server returned Error");
			} else {
				sTrace.debug("Server greeting ok");
			}
			sTrace.debug("test");

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
		sTrace.debug("login for Account " + account.userName);
		try {
			// send username
			writeToServer("USER " + account.userName);

			if (readFromServer().startsWith("-ERR")) {
				// Server returned -ERR -> abort
				throw new Exception("Username refused");
			} else {
				sTrace.debug("Login successful for Account " + account.userName);
			}
			// send password
			writeToServer("PASS " + account.passwd);

			if (readFromServer().startsWith("-ERR")) {
				// Server returned -ERR -> abort
				throw new Exception("Password refused");
			}
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.login()\n"
					+ e.getMessage());
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
			sTrace.debug("Try Stat Command");
			writeToServer("STAT");
			response = readFromServer();
			if (response.startsWith("-ERR")) {
				// Server returned -ERR -> abort
				throw new Exception("Server returned -ERR on \"STAT\" command");
			}
			n = Integer.parseInt(response.split(" ")[1]);
			sTrace.debug("Number of Mails: " + n);
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.getNumberofMails()\n"
					+ e.getMessage());
		} catch (Exception e) {
			error = true;
			sTrace.error(e.getMessage());
		}
		return n;
	}

	private void deleteMail(int n) {
		String response = "";
		try {
			sTrace.debug("Try DELE " + n + " Command");
			writeToServer("DELE " + n);
			response = readFromServer();
			if (response.startsWith("-ERR")) {
				sTrace.error("Could not delete Mail " + n);
			} else if(response.startsWith("+OK")){
				sTrace.debug("Deleted Mail " + n);
			}else{
				sTrace.error("Unknown response on DELE Command: " + response);
			}
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.getMail()\n"
					+ e.getMessage());
		}
	}

	private List<String> getMail(int n) {
		List<String> mailContent = new ArrayList<String>();
		String response = "";
		try {
			sTrace.debug("Try RETR " + n + " Command");
			writeToServer("RETR " + n);
			boolean noFullstop = true;
			if (response.startsWith("-ERR")) {
				sTrace.error("Could not Retrive Mail " + n);
			} else {
				do {
					response = readFromServer();
					if (response.equals(".")) {
						noFullstop = false;
					} else {
						if (response.startsWith("..")) {
							response = response.substring(1);
						}
						sTrace.debug("Save: " + response);
						mailContent.add(response);
					}
				} while (noFullstop);
			}
		} catch (IOException e) {
			error = true;
			sTrace.error("Socket Error during Pop3Client.getMail()\n"
					+ e.getMessage());
		}
		return mailContent;
	}

	private Path saveMail(List<String> message, String fileName)
			throws IOException {
		if (Files.notExists(Proxy.MAIL_STORAGE_PATH)) {
			Files.createDirectory(Proxy.MAIL_STORAGE_PATH); // den ordner
															// erzeugen, wo die
															// mails gespeichert
															// werden
		}
		Path whereItsStored = Paths.get("mail_storage/" + fileName + ".txt"); // dateinamen
																				// zusammenbauen
		Files.deleteIfExists(whereItsStored); // vorherige mail die den gleichen
												// namen hatte, l�schen
		whereItsStored = Files.createFile(whereItsStored); // datei erzeugen
		return Files.write(whereItsStored, message, USED_CHAR_SET); // Nachricht
																	// in die
																	// datei
																	// schreiben
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
