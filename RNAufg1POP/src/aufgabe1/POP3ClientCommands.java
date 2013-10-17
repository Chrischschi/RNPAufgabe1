package aufgabe1;

import java.util.List;

public final class POP3ClientCommands {
	/** Utility-klasse, welche die unterschiedlichen POP3-Befehle ausführt und antworten zurückbekommt */
	
	private POP3ClientCommands() {} // BITTE keine instanzen davon, macht keinen sinn, hat eh nur statische methoden.
	
	final static class Auth{ 
		/*Kommandos, welche während des AUTHORIZATION-zustandes der POP3-Session verwendet werden. **/
		
		/** Sendet den POP3-Befehl USER an den Server, bekommt eine antwort. 
		 * Die anderen befehle sind analog für pass und quit
		 * @param name 
		 * @return antwort vom server
		 */
		 static String user(String name) {
			 
		 }
		 
		 static String pass(String password) {
			 
		 }
		 
		 static String quit() {
			 
		 }
		
	}
	
	final static class Transact {
		static String stat() {
			
		}
		/** Methoden, die eine Liste von Strings zurückgeben, hatten eine multi-line Response als antwort vom server.
		 * 
		 * @return das scan listing vom server, welches die nummern der mails und deren byte-größe enthält.
		 */
		static List<String> list() {
			
		}
		
		static String list(String msg) {
			
		}
		
		static List<String> retr(String msg) {
			
		}
		
		static String dele(String msg) {
			
		}
		
		static String noop() {
			
		}
		
		static String rset() {
			
		}
		
		static String quit() {
			
		}
		
		
		/** optionale methoden aus dem rfc 1939 */
		static final class Opt {
		static List<String> uidl() {
			
		}

		
		static String uidl(String msg){
			
		}
		
		}
		
				
		
		
		
		
		
	}
	
	

}
