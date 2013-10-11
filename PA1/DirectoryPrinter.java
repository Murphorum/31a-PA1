import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * For the "pwd" command from {@link shell}.
 * @author Jacob Murphy
 *
 */
public class DirectoryPrinter implements Runnable {
	
	private String dir;
	protected LinkedBlockingQueue<String> out;
	
	/**
	 * Constructor for the DirectoryPrinter class
	 * @param dir String representing the current working directory.
	 * @param out The {@link LinkedBlockingQueue} to output the current working directory to. 
	 */
	public DirectoryPrinter (String dir, LinkedBlockingQueue<String> out) {
		this.dir = dir;
		this.out = out;
	}
	
	public void run() {
		try {
			out.put(dir);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("DirectoryPrinter.run: Cannot send directory to out-bound queue.");
		}
		
		// signal end of output
		try {
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("DirectoryPrinter.run: Cannot place message in out-bound message queue.");
			System.exit(0);
		}
	}
	
	/**
	 * Allows updating the current working directory stored.
	 * @param dir A String representing the current working directory.
	 */
	public void updateDir(String dir) {
		this.dir = dir;
	}
	
	/**
	 * Allows the out-bound {@link LinkedBlockingQueue} to be updated.
	 * @param out The LinkedBlockingQueue to output to.
	 */
	public void setOut(LinkedBlockingQueue<String> out) {
		this.out = out;
	}

	/**
	 *  Mainly used by {@link shell.printPrompt}
	 * @return Returns the current working directory.
	 */
	public String getDir(){
		return this.dir;
	}
}
