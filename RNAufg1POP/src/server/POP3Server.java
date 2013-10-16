package server;

public class POP3Server {
	
	
	
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
