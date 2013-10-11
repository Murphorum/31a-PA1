import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For the "lc" command from {@link shell}.
 * @author Jacob Murphy
 *
 */
public class LineCount implements Runnable {
	protected LinkedBlockingQueue<String> in;
	protected LinkedBlockingQueue<String> out;
	
	/**
	 * Constructor for the LineCount class.
	 * @param in  The {@link LinkedBlockingQueue} to count the number of lines of.
	 * @param out The {@link LinkedBlockingQueue} to output line count of in. 
	 */
	public LineCount(LinkedBlockingQueue<String> in, LinkedBlockingQueue<String> out) {
		this.in = in;
		this.out = out;
	}
	
	/**
	 * Increments a count variable for each message in the in-bound {@link LinkedBlockingQueue} until the termination string is found.
	 */
	public void run() {
		int count = 0;
		String inStr = "";

		while(!inStr.equals("JCMexitString")) {
			try {
				inStr = in.take();
				count++;
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				System.out.println("LineCount.run: Cannot read from in-bound message queue.");
			}
		}
				
		// signal end of output
		try {
			// --count to counter the increment from reading the termination string
			out.put(Integer.toString(--count));
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("LineCount.run: Could not terminate out-bound message queue.");
			System.exit(0);
		}
	}
	
}