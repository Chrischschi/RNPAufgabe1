package aufgabe1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.wendholt.utility.SystemTrace;
import de.wendholt.utility.Trace;

/**
 * Eine Klasse die die Serverseitige funktionalitaet des rfc 1939 Dokuments
 * implementiert.
 * 
 * @author aaw236
 * 
 */
public class POP3Server extends Thread {

	public static final int POP3_TEST_PORT_NUMBER = 11000;

	public ServerSocket welcomeSocket; // TCP-Server-Socketklasse

	public Socket connectionSocket; // TCP-Standard-Socketklasse
	
	Trace sTrace;

	Map<String, String> internAccounts = new HashMap<String, String>();
	public POP3Server(){
		sTrace = new SystemTrace();
		sTrace.setDebug(Proxy.DEBUG);
		internAccounts.put("collector", "123");
		
		int counter = 0; // Z�hlt die erzeugten Bearbeitungs-Threads
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

				/* Neuen Arbeits-Thread erzeugen und den Socket �bergeben */
				(new POP3ServerThread(++counter, connectionSocket,
						internAccounts)).start();
			}
		} catch (IOException e) {
			sTrace.error(e.toString());
		}
	}
}

class POP3ServerThread extends Thread {
	/*
	 * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
	 * erh�lt
	 */
	public static final Charset USED_CHAR_SET = Charset.forName("US-ASCII");

	private int name;
	private Socket socket;

	private String userName = null;

	private Trace sTrace = new SystemTrace();

	private BufferedReader inFromClient;
	private DataOutputStream outToClient;

	private POP3State currentState = POP3State.Authorization;

	private Map<String, String> internAccounts = new HashMap<String, String>();

	private Map<Integer, String> mails = new HashMap<Integer, String>();
	private Integer mailCounter = 0;

	private Set<Integer> deletedMails = new HashSet<Integer>();

	public POP3ServerThread(int num, Socket sock, Map<String, String> accounts) {
		/* Konstruktor */
		this.name = num;
		this.socket = sock;
		this.internAccounts = accounts;
		sTrace.setDebug(Proxy.DEBUG);
	}

	public void run() {
		String response;

		sTrace.debug("TCP Server Thread " + name
				+ " is running until QUIT is received!");

		try {
			/* Socket-Basisstreams durch spezielle Streams filtern */
			inFromClient = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			outToClient = new DataOutputStream(socket.getOutputStream());

			writeToClient("+OK Hello Client "
					+ socket.getInetAddress().getHostAddress());

			while (currentState != POP3State.Update) {
				/* String vom Client empfangen und in Gro�buchstaben umwandeln */
				response = readFromClient();
				// Nachricht vom Client verarbeiten
				sTrace.debug("Got Message " + response);
				response = computeMessage(response);
				if(response != ""){
					writeToClient(response);
				}
			}
			// L�schen der Mails nach erreichen des Update Status
			for (Integer n : deletedMails) {
				// Hole den Namen der Datei
				String fileName = mails.get(n);
				// Namens String in File Objekt, das auf die Datei im Mail
				// Ordner
				// referenziert, umwandeln
				File mailFile = Paths.get(
						Proxy.MAIL_STORAGE_PATH + fileName + ".txt").toFile();
				// Versucht die Mail zu l�schen
				sTrace.debug("loesche Datei " + fileName);
				if (!mailFile.delete()) {
					mailFile.deleteOnExit();
				}
			}

			/* Socket-Streams schlie�en --> Verbindungsabbau */
			socket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by client!");
		} catch (IllegalStateException e) {
			sTrace.error("");
			e.printStackTrace();
		}

		sTrace.debug("TCP Server Thread " + name + " stopped!");
	}

	private String computeMessage(String message) throws IllegalStateException {
		String response = "";
		if (message.length() >= 4) {
			String cmd = message.substring(0, 4).toUpperCase();
			sTrace.debug("Compute Command " + cmd);
			switch (cmd) {
			case ("USER"):
				// Benutzer
				if (currentState == POP3State.Authorization) {
					sTrace.debug("execute Method checkUser for " + message);
					response = checkUser(message);
				} else {
					response = "-ERR invalid server state";
				}
				break;
			case ("PASS"):
				// Passwort
				if (currentState == POP3State.Authorization) {
					sTrace.debug("execute Method checkPass for " + message);
					response = checkPass(message);
				} else {
					response = "-ERR invalid server state";
				}
				break;
			case ("QUIT"):
				// Verbindung beenden
				response = "+OK";
				currentState = POP3State.Update;
				break;
			case ("STAT"):
				// Nachricht-Uebersicht geben
				if (currentState == POP3State.Transaction) {
					computeStatCommand();
				} else {
					response = "-ERR invalid server state";
				}
			break;
			case ("LIST"):
				// Nachrichten auflisten
				if (currentState == POP3State.Transaction) {
					computeListCommand(message);
				} else {
					response = "-ERR invalid server state";
				}
			break;
			case ("RETR"):
				// Nachricht abrufen
				if (currentState == POP3State.Transaction) {
					computeRetrCommand(message);
				} else {
					response = "-ERR invalid server state";
				}
			break;
			case ("DELE"):
				// Nachricht loeschen
				if (currentState == POP3State.Transaction) {
					computeDeleCommand(message);
				} else {
					response = "-ERR invalid server state";
				}
			break;
			case ("NOOP"):
				// Ping
				if (currentState == POP3State.Transaction) {
					response = "+OK NOOP response";
				} else {
					response = "-ERR invalid server state";
				}
			break;
			case ("RSET"):
				// "Nachricht geloescht"-Markierungen loeschen
				if (currentState == POP3State.Transaction) {
					computeRsetCommand();
				} else {
					response = "-ERR invalid server state";
				}
			break;
			case ("UIDL"):
				if (currentState == POP3State.Transaction) {
					computeUidlCommand(message);
				} else {
					response = "-ERR invalid server state";
				}
			break;
			default:
				response = "-ERR unknown command";
			}
		} else {
			response = "-ERR unknown command";
		}
		return response;
	}

	private void computeUidlCommand(String message) {
		String[] cmd = message.split(" ");
		// Auf Argument ueberpruefen
		if (cmd.length > 1) {
			// Es wurde ein Argument gefunden
			int n = Integer.parseInt(cmd[1]);
			// -ERR Antwort, wenn Mail geloescht wurde
			if (mailIsDeleted(n)) {
				try {
					writeToClient("-ERR no such message");
				} catch (IOException e) {
					sTrace.error("Connection aborted by client! Thread" + name);
				}
			} else {
				// Sende Antwort der Form "+OK n s", fuer n = Nummer der
				// Nachricht, s = UIDL (und Name) der Nachricht, zuruck
				try {
					writeToClient("+OK " + n + " " + mails.get(n));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			// Es wurde kein Argument gefunden.
			try {
				// Positive Antwort schicken
				writeToClient("+OK");
				// Liste der UIDLs schicken
				for (int i = 1; i <= mailCounter; i++) {
					if (!mailIsDeleted(i)) {
						String response = "" + i + " " + mails.get(i);
						writeToClient(response);
					}

				}
				// Ende der Mehrzeiligen Antwort senden
				writeToClient(".");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void computeRsetCommand() {
		// Flags werden geloescht, indem die Tabelle der geloeschten Mails
		// zurueckgesetzt wird.
		deletedMails = new HashSet<Integer>();
		try {
			writeToClient("+OK Delete Flags removed");
		} catch (IOException e) {
			sTrace.error("Connection aborted by client! Thread" + name);
		}

	}

	private void computeDeleCommand(String message) {
		String[] cmd = message.split(" ");
		if (cmd.length > 1) {
			int n = Integer.parseInt(cmd[1]);
			if (mails.containsKey(n)) {
				// Mail ist noch nicht geloescht
				if (!mailIsDeleted(n)) {
					// Mail wird geloescht
					deletedMails.add(n);
					try {
						writeToClient("+OK message " + n + " deleted");
					} catch (IOException e) {
						sTrace.error("Connection aborted by client! Thread"
								+ name);
					}
				} else {
					// Mail wurde bereits geloescht
					try {
						writeToClient("-ERR message " + n + " already deleted");
					} catch (IOException e) {
						sTrace.error("Connection aborted by client! Thread"
								+ name);
					}
				}
			} else {
				// Keine Mail mit der Nummer n gefunden
				try {
					writeToClient("-ERR no such message");
				} catch (IOException e) {
					sTrace.error("Connection aborted by client! Thread" + name);
				}
			}
		} else {
			// Nachricht enthielt kein Argument
			try {
				writeToClient("-ERR no argument found");
			} catch (IOException e) {
				sTrace.error("Connection aborted by client! Thread" + name);
			}
		}

	}

	private void computeRetrCommand(String message) {
		String[] cmd = message.split(" ");
		// Auf Argument Pruefen
		if (cmd.length > 1) {
			int n = Integer.parseInt(cmd[1]);
			if (Files.notExists(Proxy.MAIL_STORAGE_PATH)) {
				// Mail Ordner existiert nicht -> noch keine Mails gespeichert
				try {
					writeToClient("-ERR no messages");
				} catch (IOException e) {
					sTrace.error("Connection aborted by client! Thread" + name);
				}
			} else {
				// Mail ueberpruefen
				if (mails.containsKey(n)) {
					if (!mailIsDeleted(n)) {
						// Mail wird gesendet
						List<String> mailContent = loadMail(n);
						try {
							writeToClient("+OK message follows");
							for (String currentLine : mailContent) {
								// Es wird auf . am anfang der Zeile gepr�ft.
								// Sollte dies der fall sein, wird ein weiterer
								// . am Anfang der Zeile hinzugef�gt, um vor dem
								// Ende der Mail das CRLF.CRLF problem zu
								// vermeiden.
								if (currentLine.startsWith(".")) {
									currentLine = "." + currentLine;
								}
								writeToClient(currentLine);
							}
							writeToClient(".");
						} catch (IOException e) {
							sTrace.error("Connection aborted by client! Thread"
									+ name);
						}
					} else {
						try {
							writeToClient("-ERR message " + n + " deleted");
						} catch (IOException e) {
							sTrace.error("Connection aborted by client! Thread"
									+ name);
						}
					}
				} else {
					// Keine Mail mit dieser Nummer gefunden
					try {
						writeToClient("-ERR no such message");
					} catch (IOException e) {
						sTrace.error("Connection aborted by client! Thread"
								+ name);
					}
				}
			}
		} else {
			// Kein Argument wurde gefunden
			try {
				writeToClient("-ERR no argument");
			} catch (IOException e) {
				sTrace.error("Connection aborted by client! Thread" + name);
			}
		}

	}

	private List<String> loadMail(int n) {
		List<String> mail = new ArrayList<String>();
		String fileName = mails.get(n);
		Path whereItsStored = Paths.get("mail_storage/" + fileName);
		try {
			mail = Files.readAllLines(whereItsStored, USED_CHAR_SET);
			sTrace.debug("Mail " + mail.toString());
		} catch (IOException e) {
			sTrace.error("Could not load Mail " + fileName);
		}
		return mail;
	}

	private void computeStatCommand() {
		// Aktualisiere liste der Mails
		checkMailFiles();
		try {
			writeToClient("+OK " + mailCounter + " " + getAllMailSize());
		} catch (IOException e) {
			sTrace.error("Connection aborted by client! Thread" + name);
		}

	}

	private void computeListCommand(String message) {
		// Aktualisiere liste der Mails
		checkMailFiles();
		// Auf Argumente ueberpruefen
		String[] cmd = message.split(" ");
		if (cmd.length > 1) {
			int n = Integer.parseInt(cmd[1]);
			long size = getMailSize(n);
			String response = "+OK " + n + " " + size;
			try {
				writeToClient(response);
			} catch (IOException e) {
				sTrace.error("Connection aborted by client! Thread" + name);
			}
		} else {
			try {
				// Signalisieren, dass der Befehl akzeptiert wurde
				writeToClient("+OK List follows");
				for (int i = 1; i <= mailCounter; i++) {
					// Antwort der Form "n m", f�r n = Nummer der Nachricht und
					// m = groesse der Nachricht n, wenn die Mail nicht
					// geloescht ist.
					if (!mailIsDeleted(i)) {
						String response = "" + i + " " + getMailSize(i);
						writeToClient(response);
					}

				}
				// Ende der Mehrzeilennachricht �bertragen.
				writeToClient(".");
			} catch (IOException e) {
				sTrace.error("Connection aborted by client! Thread" + name);
			}
		}
	}

	private String checkUser(String message) {
		String response;
		String[] temp = message.split(" ");
		if (temp.length >= 2) {
			if (internAccounts.containsKey(temp[1])) {
				response = "+OK";
				userName = temp[1];
			} else {
				response = "-ERR User" + temp[1] + " not found";
			}
		} else {
			response = "-ERR No Username found in Message";
		}
		return response;
	}

	private String checkPass(String message) {
		String response;
		String[] temp = message.split(" ");
		if (temp.length >= 2) {
			if (userName == null) {
				response = "-ERR No User was registered";
			} else if (internAccounts.get(userName).equals(temp[1])) {
				// Benutzer hat sich erfolgreich eingeloggt. -> Transaction
				// State
				response = "+OK User " + userName + " logged in";
				currentState = POP3State.Transaction;
			} else {
				response = "-ERR User" + temp[1] + " not found";
			}
		} else {
			response = "-ERR No Username found in Message";
		}
		return response;
	}

	private String readFromClient() throws IOException {
		/* Lies die n�chste Anfrage-Zeile (request) vom Client */
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

	public void checkMailFiles() {
		// Order, in dem die Mails liegen
		File mailDirectory = Proxy.MAIL_STORAGE_PATH.toFile();
		// Ordner und Datein im Mailordner
		File[] mailFiles = mailDirectory.listFiles();
		// Auswerten, ob die Dateien, die direkt im Mailordner liegen, bereits
		// bekannt sind, sonst hinzufuegen.
		for (int i = 0; i < mailFiles.length; i++) {
			if (!mails.containsValue(mailFiles[i].getName())) {
				mails.put(++mailCounter, mailFiles[i].getName());
			}
		}
	}

	public long getMailSize(int n) {
		long size = 0;
		// Hole den Namen der Datei
		String fileName = mails.get(n);
		// Namens String in File Objekt, das auf die Datei im Mail Ordner
		// referenziert, umwandeln
		File mailFile = Paths.get(Proxy.MAIL_STORAGE_PATH.toString() + "/" + fileName)
				.toFile();
		sTrace.debug("mailFile " + mailFile);
		// Gib die groesse der Datei in byte als Long-Wert zurueck
		if (mailIsDeleted(n)) {
			size = -1;
		} else {
			size = mailFile.length();
			sTrace.debug("FileSize " + mailFile.length());
		}
		return size;
	}

	public long getAllMailSize() {
		long size = 0;
		for (int i = 1; i <= mailCounter; i++) {
			if (!mailIsDeleted(i)) {
				size += getMailSize(i);
			}
		}
		return size;
	}

	public boolean mailIsDeleted(int n) {
		// Hole den Namen der Datei
		String fileName = mails.get(n);
		// Namens String in File Objekt, das auf die Datei im Mail Ordner
		// referenziert, umwandeln
		File mailFile = Paths.get(Proxy.MAIL_STORAGE_PATH + fileName + ".txt")
				.toFile();
		// �berpr�fen, ob die Datei existiert und nicht als gel�scht geflaggt
		// wurde. (Datei kann in der Map enthalten sein, jedoch nicht mehr
		// Existieren, wenn diese durch einen anderen Thread entg�ltig gel�scht
		// wurde)
		return !deletedMails.contains(n) && mailFile.exists();
	}

	public static int getKeyByValue(Map<Integer, String> map, String value) {
		for (Entry<Integer, String> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return -1;
	}
}
