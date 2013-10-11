import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For the "history" command from {@link shell}.
 * @author Jacob Murphy
 */
public class History implements Runnable {
	
	private ArrayList<String> commands;
	protected LinkedBlockingQueue<String> out;
	
	/**
	 * Constructor for the History class. 
	 * @param commands A {@link ArrayList} of Strings representing commands previously entered in {@link shell}.
	 * @param out The {@link LinkedBlockingQueue} to output the previously entered commands. It is usually set later in {@link shell.parser}.
	 */
	public History(ArrayList<String> commands, LinkedBlockingQueue<String> out) {
		this.commands = commands;
		this.setOut(out);
	}
	
	/**
	 * Put all of the commands into the out-bound LinkedBlockingQueue
	 */
	public void run() {
		int count = 0;
		
		for(String str : commands) {
			try {
				out.put(++count +" "+ str);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				System.out.println("History.run: Cannot place message in out-bound message queue.");
			}
		}
		
		// signal end of output
		try {
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("History.run: Could not terminate out-bound message queue.");
			System.exit(0);
		}
	}
	
	public void setOut(LinkedBlockingQueue<String> out) {
		this.out = out;
	}
	
	/**
	 * Gets the previously entered commands. Mostly used by the !n option in {@link shell.parser}
	 * @return Returns the {@link ArrayList} containing all previously entered commands.
	 */
	public ArrayList<String> getCommands(){
		return this.commands;
	}
	
}