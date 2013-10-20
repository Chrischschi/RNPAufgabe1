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
	
	public static final Path MAIL_STORAGE_PATH = Paths.get("mail_storage/"); //Ordner wo die dateien gespeichert werden
	/**
	 * Der zeichensatz, der zum lesen und abspeichern verwendet wird,
	 *  laut rfc 5321 (SMTP) http://tools.ietf.org/html/rfc5321 soll 
	 *  dies US-ASCII (7-bit) sein, jedoch macht das große probleme 
	 *  mit umlauten. Daher: US-ASCII oder doch lieber Charset.defaultCharset() ? 
	 */
	public static final Charset USED_CHAR_SET =  Charset.forName("US-ASCII");
	
	
	/** 
	 * Spezifikation im original von Steffen Windrath:
	 * 
	 * */
	/**
	 * ok dann brauch ich ne methode, um ne List<String> mit String x als namen der datei abzuspeichern,
	 * returns: Den Pfad der datei auf dem Dateisystem
	 * */
	public static Path saveMail(List<String> message, String filename) throws IOException {
		if (Files.notExists(MAIL_STORAGE_PATH)) {
			Files.createDirectory(MAIL_STORAGE_PATH); //den ordner erzeugen, wo die mails gespeichert werden
		}
		Path whereItsStored = Paths.get("mail_storage/" + filename + ".txt"); //dateinamen zusammenbauen
		Files.deleteIfExists(whereItsStored); //vorherige mail die den gleichen namen hatte, löschen
		whereItsStored = Files.createFile(whereItsStored); //datei erzeugen
		return Files.write(whereItsStored, message, USED_CHAR_SET); //Nachricht in die datei schreiben
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
