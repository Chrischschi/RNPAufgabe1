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

	//public static final int SERVER_PORT =  6789; //der client verbindet nicht zu diesem PORT, sondern zu den konfigurierten
	
	private Socket clientSocket; // TCP-Standard-Socketklasse

	private DataOutputStream outToServer; // Ausgabestream zum Server
	private BufferedReader inFromServer; // Eingabestream vom Server
	
	Trace sTrace = new SystemTrace();


	public Pop3Client(){
		sTrace.setDebug(Proxy.DEBUG);
	}
	
	public void getMails(MailAccount account) {
		/* Client starten. Ende, wenn quit eingegeben wurde */
		List<String> mailContent;
		
		int numberOfMails;
		

		/* Ab Java 7: try-with-resources mit automat. close benutzen! */
		try {
			//Socket, inklusive Streams, einrichten
			openSocket(account);
			
			login(account);
			
			numberOfMails = getNumberOfMails();
			
			for (int i=1; i<=numberOfMails; i++){
				mailContent = getMail(i);
				saveMailContent(mailContent, Proxy.MAIL_DIRECTORY+"\\"+System.currentTimeMillis());
			}
			
//			while (serviceRequested) {
//				sTrace.debug("ENTER TCP-DATA: ");
//				/* String vom Benutzer (Konsoleneingabe) holen */
//				command = inFromUser.nextLine();
//
//				/* String an den Server senden */
//				writeToServer(command);
//
//				/* Modifizierten String vom Server empfangen */
//				singleLineResponse = readFromServer();
//
//				/* Test, ob Client beendet werden soll */
//				if (singleLineResponse.indexOf("QUIT") > -1) {
//					serviceRequested = false;
//				}
//			}

			/* Socket-Streams schließen --> Verbindungsabbau */
			clientSocket.close();
		} catch (IOException e) {
			sTrace.error("Connection aborted by server!");
		}

		sTrace.debug("TCP Client stopped!");
	}
	
	private void openSocket(MailAccount account){
		try {
			/* Socket erzeugen --> Verbindungsaufbau mit dem Server */
			clientSocket = new Socket(account.serverAddress, account.portNo);
			/* Socket-Basisstreams durch spezielle Streams filtern */
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			
			// "+OK [Server begrüßung]" abfangen
			readFromServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void login(MailAccount account){
		//TODO -ERR Response & exception behandeln 
		try {
			writeToServer("USER " + account.userName);
			readFromServer();
			writeToServer("PASS " + account.passwd);
			readFromServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int getNumberOfMails(){
		int n = 0;
		String response;
		try {
			writeToServer("STAT");
			response = readFromServer();
			n = Integer.parseInt(response.split(" ")[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}
	
	private List<String> getMail(int n){
		List<String> mailContent = new ArrayList<String>();
		String response = "";
		try {
			writeToServer("RETR " + n);
			boolean noFullstop = true;
			do{
				response = readFromServer();
				if(response.equals(".")){
					noFullstop = false;
				}else{
					mailContent.add(response);
				}
			}while(noFullstop);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mailContent;
	}
	
	private void saveMailContent(List<String> mailContent, String file){
		//TODO Dateien Speichern
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
