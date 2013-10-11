import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For the "!n" command from {@link shell}.
 * @author Jacob Murphy
 */
public class Exclam implements Runnable {
	protected LinkedBlockingQueue<String> in;
	protected LinkedBlockingQueue<String> out;
	
	/**
	 * Constructor for the Exclam class.
	 * @param in         The {@link LinkedBlockingQueue} returned from a call to {@link shell.parser).
	 * @param out        The {@link LinkedBlockingQueue} to output messages from the in-bound LinkedBlockingQueue to.
	 */
	public Exclam (LinkedBlockingQueue<String> in, LinkedBlockingQueue<String> out) {
		this.in = in;
		this.out = out;
	}
	
	/**
	 * Writes each message from a {@link LinkedBlockingQueue} returned from a call to {@link shell.parser}
	 * to the out-bound LinkedBlockingQueue.
	 */
	public void run(){
		if(this.in != null) {
			String inStr = "";
	
			while(!inStr.equals("JCMexitString")) {
				try {
					inStr = in.take();
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
					System.out.println("Exclam.run: Could not take message from in-bound queue.");
				}
				
				try {
					out.put(inStr);
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
					System.out.println("Exclam.run: Could not send message to out-bound queue.");
				}
			}
		}

		// send out the termination string
		try {
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("Exclam.run: Could not terminate out-bound message queue.");
			System.exit(0);
		}
	}
}