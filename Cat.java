import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For the "cat" command from {@link shell}.
 * @author Jacob Murphy
 *
 */
public class Cat implements Runnable {
	private LinkedList<File> files;
	protected LinkedBlockingQueue<String> out;

	/**
	 * Constructor for the Cat class.
	 * @param files A {@link LinkedList} of Files to output line by line.
	 * @param out  A {@link LinkBlockingQueue} to output the lines to.
	 */
	public Cat(LinkedList<File> files, LinkedBlockingQueue<String> out) {
        this.out = out;
        this.files = files;
    }
	
	/**
	 * Reads in each {@link File} from the given array of Files
	 * line by line and outputs to the given LinkedBlockingQueue
	 */
	public void run(){
		for (File f : files){
			
			boolean safe = true;
			
			Scanner scan = new Scanner(System.in);
			try{ 
				scan = new Scanner(f);
			} catch (FileNotFoundException e) {
				System.out.println("Cat.run: "+f.getName()+" not found.");
				safe = false;
			}
			
			while (safe && scan.hasNextLine()) {
				try {
					out.put(scan.nextLine());
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
					System.out.println("Cat.run: Cannot place message in out-bound message queue.");
				}
			}
		}
		
		// signal end of output
		try {
			out.put("JCMexitString");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("Cat.run: Could not terminate out bound message queue.");
			System.exit(0);
		}
	}
}