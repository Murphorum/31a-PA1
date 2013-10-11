import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For the "ls" command from {@link shell}.
 * @author Jacob Murphy
 *
 */
public class DirectoryList implements Runnable {
	
	private String dir;
	protected LinkedBlockingQueue<String> out;
	
	/**
	 * Constructor for the DirectoryList class.
	 * @param dir String representing the current working directory.
	 * @param out The {@link LinkedBlockingQueue} to output subfolders and files in the current working directory.
	 */
	public DirectoryList(String dir, LinkedBlockingQueue<String> out) {
		this.dir = dir;
		this.out = out;
	}
	
	/**
	 * Gets the list of subfolders and files in the current working directory and outputs them.
	 */
	public void run(){
		File f = new File(dir);
		String[] subFiles = f.list();
		for(String s : subFiles) {
			try {
				out.put(s);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				System.out.println("DirectoryList.run: Cannot place path in out-bound message queue.");
			}
		}
		
		// signal end of output
		try {
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("DirectoryList.run: Could not terminate out-bound message queue.");
			System.exit(0);
		}
	}
}