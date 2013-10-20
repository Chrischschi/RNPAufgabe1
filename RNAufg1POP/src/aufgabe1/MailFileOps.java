package aufgabe1;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Mail File Operations
 * 
 * Stellt operationen auf dateien eines Maildrops bereit, notwendig, um dateien abzuspeichern, 
 * client-requests zu bearbeiten(z.B. für STAT und LIST), und aus maildrops wieder nachrichten 
 * zu erzeugen. Die Operationen werden mithilfe der in Java 7 eingeführten NIO2-FileSystem-API 
 * realisiert.
 * @author Christian
 *
 */
public class MailFileOps {
	/** 
	 * Spezifikation im original von Steffen Windrath:
	 * 
	 * */
	/**
	 * ok dann brauch ich ne methode, um ne List<String> mit String x als namen der datei abzuspeichern,
	 * returns: Den Pfad der datei auf dem Dateisystem
	 * */
	public static Path saveMail(List<String> message,String filename) throws IOException {
		Path whereItsStored = Paths.get("mail_storage", filename).toAbsolutePath();
		Path whereItGotStored = Files.createFile(whereItsStored);
		return Files.write(whereItGotStored, message,Charset.defaultCharset() );
	}
	
	/**
	 * ne methode, um den namen einer datei nr n zu bekommen,
	 */
	
	public static Path findMailByIndex(int fileNumber) {
		throw new UnsupportedOperationException("Not implemented Yet.");
	}
	/**
 	ne methode um die datei nr n zu laden,
 	returns: Eine Liste von strings mit dem inhalt der mail, zeilenweise.
 	*/
	
	public static List<String> loadMail(int fileNumber) {
		throw new UnsupportedOperationException("Not implemented Yet.");
	}
	

	/**
 	eine methode, die mir größe der datei nr n sagt,
 	returns: Anzahl der bytes der datei, die die nummer "filenumber" hat.
	*/
	public static int getFileSize(int fileNumber) {
		throw new UnsupportedOperationException("Not implemented Yet.");
	}
	
	/**
 	ne methode, die mir die anzahl der dateien sagt und zuletzt
	 */
	public static int getAmountOfMailFiles()  {
		throw new UnsupportedOperationException("Not implemented Yet.");
	}
	
	/**
	* eine methode um datei n zu löschen
	*/
	public static void deleteFile(int fileNumber) {
		throw new UnsupportedOperationException("Not implemented Yet.");
	}
}
