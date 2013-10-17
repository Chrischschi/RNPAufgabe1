package de.wendholt.utility;

public class FileTrace implements Trace {
	private java.io.PrintWriter pw;
	private boolean debug;

	public FileTrace(String filename) throws java.io.IOException {
		pw = new java.io.PrintWriter(new java.io.FileWriter(filename));
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void debug(String message) {
		if (debug) {
			pw.println("DEBUG: " + message);
			pw.flush();
		}
	}

	public void error(String message) {
		pw.println("ERROR: " + message);
		pw.flush();
	}
}
