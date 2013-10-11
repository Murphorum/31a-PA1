import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For the "grep" command from {@link shell}.
 * @author Jacob Murphy
 *
 */
public class Grep implements Runnable {
	protected String searchStr;
	protected LinkedBlockingQueue<String> in;
	protected LinkedBlockingQueue<String> out;
	
	/**
	 * Constructor for the Grep class.
	 * @param searchStr  The String to search for in messages from in.
	 * @param in         The {@link LinkedBlockingQueue} to search through for the search string.
	 * @param out        The {@link LinkedBlockingQueue} to output messages that contain the search stringfrom the in-bound LinkedBlockingQueue to.
	 */
	public Grep (String searchStr, LinkedBlockingQueue<String> in, LinkedBlockingQueue<String> out) {
		this.searchStr = searchStr;
		this.in = in;
		this.out = out;
	}
	
	/**
	 * Takes a line from the in-bound {@link LinkedBlockingQueue} and checks if it contains the search String.
	 * If it does, the line is sent to the out-bound {@link LinkedBlockingQueue}.
	 */
	public void run() {
		String inStr = "";

		while(!inStr.equals("JCMexitString")) {
			try {
				inStr = in.take();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				System.out.println("Grep.run: Could not take message from in-bound queue.");
			}
			
			if(inStr.contains(searchStr)) {
				try {
					out.put(inStr);
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
					System.out.println("Grep.run: Could not send message to out-bound queue.");
				}
			}
		}
		
		try {
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("Grep.run: Could not terminate out-bound message queue.");
			System.exit(0);
		}
	}
}
