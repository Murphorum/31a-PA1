import java.io.*;
import java.lang.StringBuilder;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main code of the simple shell implementation
 * Code for Brandeis University COSI 31a Project 1
 * jcmurphy@brandeis.edu
 * @author Jacob Murphy
 */
public class shell {	
	public static void main (String[] args) {
		Scanner console = new Scanner(System.in);
		System.out.println();
		
		// dir stores the working directory, can be accessed by dir.getDir()
		// dir also has a run method that pipes out the working directory
		DirectoryPrinter dir = new DirectoryPrinter(System.getProperty("user.dir"), null);
		
		// History stores each command entered
		ArrayList<String> commands = new ArrayList<String>();
		
		// out queue of hist is to be set in the parser
		History hist = new History(commands, null);
		
		// program ends once done is changed set to true in the while loop
		boolean done = false;
		while(!done){
			
			// get user to input a command
			printPrompt(dir.getDir());
			String in = console.nextLine();
			
			// name of output file, default is System.out
			String fileName = "System.out";
			
			// handle user input
			if(in.isEmpty()) {         						// reject empty commands
			} else if(in.equalsIgnoreCase("exit")) {      	// quit if the "exit" command is given
				done = true;
				System.out.println();
			} else { 										// go to the command parser otherwise
				
				// ">" is an optional special character to indicate output file
				if(in.contains(">")){
					int position = in.lastIndexOf(">");
					if(in.length() > position){
						fileName = dir.getDir()+System.getProperty("file.separator")+in.substring(position+1).replaceAll("\\s","");
						
						// strip away the output file from command because !<int> command
						// could be done from different directory and the output file 
						// could already exist in that directory and be overwritten
						in = in.substring(0, position);
					}
				}
				
				// add the input to the history
				// !<int> commands do not get added to history
				if(in.charAt(0) != '!'){
					commands.add(in);
				}
				
				// establish objects needed for parser
				LinkedBlockingQueue<String> inQ  = new LinkedBlockingQueue<String>();
				LinkedBlockingQueue<String> outQ = new LinkedBlockingQueue<String>();
				LinkedList<Thread> threadList = new LinkedList<Thread>();
				
				// parserGateKeeper calls parser after checking that the command does not start with grep or lc
				LinkedBlockingQueue<String> q = parserGateKeeper(in, inQ, outQ, dir, hist, threadList);
				
				
				// close open threads
				for(Thread t : threadList) {
					try {
						t.join();
					} catch (InterruptedException e){
						System.err.println(e.getMessage());
						System.out.println("Shell.main: Could not join thread.");
					}
				}
				
				// ensure the LinkedBlockingQueue returned from parser has the termination string
				// if it already has the termination string, adding a second one will not be harmful
				if( q != null){
					try {
						q.put("JCMexitString");
					} catch (InterruptedException e) {
						System.err.println(e.getMessage());
					}
		
					// print results from parser to proper file stream					
					printQueue(q, fileName);
				}
			}
		}
	}
	
	
	/**
	 * Reads through a {@link LinkedBlockingQueue} and prints the contents. 
	 * The LinkedBlockingQueue must end with the {@link String} "JCMexitString".
	 * @param q        The LinkedBlockingQueue to print out.
	 * @param fileName The name of the file to print out to. If given, "System.out" it prints to standard out. 
	 */
	public static void printQueue(LinkedBlockingQueue<String> q, String fileName){
		
		PrintWriter writer;
		
		if(fileName.equals("System.out")){
			writer = new PrintWriter(System.out, true);
		} else {
			File f = new File(fileName);
			try{
				writer = new PrintWriter(f);
			} catch (FileNotFoundException e) {
				writer = new PrintWriter(System.out, true);
			}
		}
		
		String str = "";
		boolean done = false;
		while(!done){
			try {
				str = q.take();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				System.exit(0);
			}	
			
			if(str != "JCMexitString") {
				writer.println(str);
			} else {
				done = true;
			}
		}
		if(!fileName.equals("System.out")) {
			writer.close();
		}
	}
	
	/**
	 * Returns a {@link LinkedBlockingQueue}. This is used in conjunction with {@link parser}
	 * to check for invalid first commands.
	 * @param in         A non-null String of command(s) to parse.
	 * @param inQ        The {@link LinkedBlockingQueue} for piped input. 
	 * @param outQ       The {@link LinkedBlockingQueue} for piped output. 
	 * @param dir        The {@link DirectoryPrinter} that contains the current working directory.
	 * @param hist       The {@link History} that has the list of commands entered so far.
	 * @param threadList The {@link LinkedList} of threads that need to be closed in the calling method.
	 * @return           Returns a {@link LinkedBlockingQueue}; if the parameter in starts with the invalid commands of "lc" or "grep" it returns null. 
	 */
	public static LinkedBlockingQueue<String> parserGateKeeper (String in, LinkedBlockingQueue<String> inQ, LinkedBlockingQueue<String> outQ, DirectoryPrinter dir, History hist, LinkedList<Thread> threadList) {
		String[] input = in.split("\\|");
		
		// lc command - cannot start
		if(input[0].replaceAll("\\s","").equals("lc")) {
			printOut(dir.getDir(), "Command \"lc\" cannot start a string of commands.");
			return null;
		}
		
		// grep command - cannot start
		else if((input[0].length()==4 && input[0].substring(0, 4).equals("grep")) || (input[0].length()>5 && input[0].substring(0, 5).replaceAll("\\s","").equals("grep"))) {
			printOut(dir.getDir(), "Command \"grep\" cannot start a string of commands.");
			return null;
		}
		
		// otherwise continue on to the parser
		else {
			return parser (in, inQ, outQ, dir, hist, threadList);
		}
	}
	
	/**
	 * Returns a {@link LinkedBlockingQueue}. This is used in conjunction with {@link parser}
	 * to set up commands with pipes in them.
	 * @param input      A String array with each String being a separate command for {@link parser}.
	 * @param inQ        The {@link LinkedBlockingQueue} for piped input. 
	 * @param dir        The {@link DirectoryPrinter} that contains the current working directory.
	 * @param hist       The {@link History} that has the list of commands entered so far.
	 * @param threadList The {@link LinkedList} of threads that need to be closed in the method that called parser.
	 * @return           Returns a {@link LinkedBlockingQueue}.
	 */
	// pipe handler, used to support the parser method
	// determines if parser needs to be called again for piped input or if it should return the last LinkedBlockingQueue sent from parser
	public static LinkedBlockingQueue<String> pipeHandler (String[] input, LinkedBlockingQueue<String> inQ, DirectoryPrinter dir, History hist, LinkedList<Thread> threadList) {	
		if(input.length == 1) {
			return inQ;
		} else {
			LinkedBlockingQueue<String> outQ = new LinkedBlockingQueue<String>();
			return parser (buildString(input, "|", 1), inQ, outQ, dir, hist, threadList); 
		}
	}
	
	
	/**
	 * Parses the String of user input and executes the commands. Return a {@link LinkedBlockingQueue} 
	 * to be printed out by the calling method 
	 * @param in         A non-null String of command(s) to parse.
	 * @param inQ        The {@link LinkedBlockingQueue} for piped input. 
	 * @param outQ       The {@link LinkedBlockingQueue} for piped output. 
	 * @param dir        The {@link DirectoryPrinter} that contains the current working directory.
	 * @param hist       The {@link History} that has the list of commands entered so far.
	 * @param threadList The {@link LinkedList} of threads that need to be closed in the calling method.
	 * @return           Returns a {@link LinkedBlockingQueue}. 
	 */
	public static LinkedBlockingQueue<String> parser (String in, LinkedBlockingQueue<String> inQ, LinkedBlockingQueue<String> outQ, DirectoryPrinter dir, History hist, LinkedList<Thread> threadList) {	
		String[] input = in.split("\\|");
		
		// pwd command - pipes out
		if(input[0].replaceAll("\\s","").equals("pwd")){
			dir.setOut(outQ);
			Thread t = new Thread(dir);
			try{
				t.start();
				threadList.add(t);
			} catch(RuntimeException e) {
				System.out.println("Shell.parser: pwd thread failed to start.");
			}
		}
		
		// ls command - pipes out
		else if(input[0].replaceAll("\\s","").equals("ls")) {
			DirectoryList ls = new DirectoryList(dir.getDir(), outQ);
			Thread t = new Thread(ls);
			try{
				t.start();
				threadList.add(t);
			} catch(RuntimeException e) {
				System.out.println("Shell.parser: ls thread failed to start.");
			}
		}
		
		// history command - pipes out
		else if(input[0].replaceAll("\\s","").equals("history")) {
			hist.setOut(outQ);
			Thread t = new Thread(hist);
			try{
				t.start();
				threadList.add(t);
			} catch(RuntimeException e) {
				System.out.println("Shell.parser: history thread failed to start.");
			}
		}

		// "cd" command - no piping
		else if(input[0].length()> 1 && input[0].substring(0, 2).equals("cd")) {
			
			if(input.length>1){
				printOut(dir.getDir(), "Command \"cd\" does use pipes.");
			}
			
			// handles the special of of just "cd"
			else if(input[0].length() == 2 || (input[0].length() == 3 && input[0].charAt(2) == ' ')) {
				dir.updateDir(System.getProperty("user.home")); 
			} else {					
				// send everything after "cd "
				String newDir = changeDir(input[0].substring(3), dir.getDir()); 
				
				// check if cd goes to a valid directory, 
				// if not valid reset directory to original one
				File f = new File(newDir);
				if(f.exists()){
					dir.updateDir(newDir);
				} else {
					printOut(dir.getDir(), "File \"" + input[0].substring(3) + "\" does not exist.");
				}
			}
			return null;
		}

		// handles the "!n" command - calls parser with correct command
		else if(input[0].charAt(0) == ('!')) {
			if(isInt(input[0].substring(1).replaceAll("\\s",""))){
				int commNum = Integer.parseInt(input[0].substring(1).replaceAll("\\s",""));
				
				if(commNum > hist.getCommands().size()){
					printOut(dir.getDir(), "The history does not currently have " + commNum + " commands.");
					return null;
				} else {
					String command = hist.getCommands().get(commNum-1);
					
					// initialize the Exclam thread
					// the input queue for Exclam is what is returned from a call to parser
					Exclam ex = new Exclam(parserGateKeeper (command, inQ, outQ, dir, hist, threadList), outQ);
					Thread t = new Thread(ex);
					try{
						t.start();
						threadList.add(t); 
					} catch(RuntimeException e) {
						System.out.println("Shell.parser: !n thread failed to start.");
					}
					
				}
			} else {
				printOut(dir.getDir(), "Command \"!\" requires the form !<int> ");
			}
		}
		
		// cat command - pipes out
		// if given valid file(s) and invalid file, only pipes out valid file(s)
		else if(input[0].length()>2 && input[0].substring(0,3).equals("cat")) {			
			// if no Files are given, print hint
			if(input[0].length() == 3 || (input[0].length() == 4 && input[0].charAt(3) == ' ')){
				printOut(dir.getDir(), "Command \"cat\" needs at least one file name as input.");
			}
			
			// if the input is somthing like "cata", print hint
			else if (input[0].charAt(3) != ' ') {
				printOut(dir.getDir(), "Command \"cat\" needs a space before the first file name.");
			}
			
			// regular cat situation
			else {
				// cat accepts a list of file names, store them in fileList
				LinkedList<File> fileList = new LinkedList<File>();
				
				// used to read the commands following "cat"
				Scanner scan = new Scanner(input[0].substring(3));
				
				// check if each file name is valid, if it is, add it to fileList 
				while (scan.hasNext()){		
					String fileName = scan.next();
					File f = new File(dir.getDir()+"/"+fileName);
					if(f.exists()) {
						fileList.add(f);
					} else {
						// if an invalid file name is found, tell the user	
						printOut(dir.getDir(), "File \"" + fileName + "\" does not exist.");
					}
				}
				
				// initialize the cat thread
				Cat cat = new Cat(fileList, outQ);
				Thread t = new Thread(cat);
				try{
					t.start();
					threadList.add(t); 
				} catch(RuntimeException e) {
					System.out.println("Shell.parser: cat thread failed to start.");
				}
			}
		}					
		// lc command - needs pipe in and pipe out
		else if(input[0].replaceAll("\\s","").equals("lc")) {
			LineCount lc = new LineCount(inQ, outQ);			
			Thread t = new Thread(lc);
			try{
				t.start();
				threadList.add(t); 
			} catch(RuntimeException e) {
				System.out.println("Shell.parser: lc thread failed to start.");
			}
		}
		
		// grep command - piped into and pipes out
		else if((input[0].length()==4 && input[0].substring(0, 4).equals("grep")) || (input[0].length()>5 && input[0].substring(0, 5).replaceAll("\\s","").equals("grep"))) {
			
			// check if there is a space after "grep"
			int afterGrep = input[0].indexOf('p') + 1;
			if(input[0].length() == 4){
				printOut(dir.getDir(), "grep needs a search string");
			} else if(input[0].charAt(afterGrep) != ' ') {
				printOut(dir.getDir(), "grep needs a space before the search string");
			} 
			
			// setup Grep thread
			else {
				String searchStr = input[0].substring(afterGrep+1);
				
				// remove space between end of search term and the pipe
				if(searchStr.charAt(searchStr.length()-1) == ' '){
					searchStr = searchStr.substring(0, searchStr.length()-1);
				}
				
				
				Grep g = new Grep(searchStr, inQ, outQ);
				Thread t = new Thread(g);
				try{
					t.start();
					threadList.add(t); 
				} catch(RuntimeException e) {
					System.out.println("Shell.parser: grep thread failed to start.");
				}
			}
		}
		
		// catch invalid commands and tell the user
		else {
			printOut(dir.getDir(), "\"" + in + "\" is not a recognized command.");
		}
		
		// call pipHandler to see if data can be returned to main
		// or if parser needs to be used for a piped command
		return pipeHandler(input, outQ, dir, hist, threadList);
	}
	
	
	/**
	 * Prints the working directory as a prompt for user input into the shell.
	 * @param dir The current working directory as a String.
	 */
	public static void printPrompt(String dir){
		System.out.print("JMshell: "+dir + "> ");
	}
	
	// for outputing messages to user
	public static void printOut(String dir, String text){
		printPrompt(dir);
		System.out.println(text);
	}
	
	/**
	 * Returns a String for the changed path. The calling method needs to check if 
	 * the returned path exists. The directory changes work recursively.
	 * @param cd   A String of the desired changes to the current working directory.
	 * @param dir  A String of the currect working directory.
	 * @return     Returns a modified working directory that the calling method needs to check for correctness.
	 */
	public static String changeDir(String cd, String dir){		
		
		// find the seperator used by the system
		char seperator = System.getProperty("file.separator").charAt(0);
		int lastSeperator = dir.lastIndexOf(seperator);

		String seperatorStr = Character.toString(seperator);
		
		// split up the path based on the separator
		String[] cdPath = cd.split(seperatorStr);
		
		// return the directory if in the lowest directory already
		if (lastSeperator < 0) {
			return dir;
		}
		// if there is no more changes in the cd command, return the directory string
		else if (cd.equals("")) {
			return dir;
		} 
		// special case of "cd ."
		else if(cdPath[0].equals(".")) {
			return changeDir(buildString(cdPath, seperatorStr, 1), dir);
		}
		// special case of "cd .." 
		else if (cdPath[0].equals("..")) {
			return changeDir(buildString(cdPath, seperatorStr, 1), dir.substring(0,lastSeperator));
		} 
		// typical cd to subdirectory
		else {
			return changeDir(buildString(cdPath, seperatorStr, 1), dir+seperator+cdPath[0]);
		}
		
	}
	
	/**
	 * It uses {@link StringBuilder} to create a one String from a String array.
	 * @param arr          The array of Strings to join together.
	 * @param separator    The String to add between each element in arr.
	 * @param start        The index of arr to start building from.
	 * @return             Returns a String built from a String array. 
	 */
	public static String buildString(String[] arr, String separator, int start) {
		StringBuilder sb = new StringBuilder();
		for(int i = start; i<arr.length; i++){
			sb.append(arr[i]);
			if(i<arr.length-1){
				sb.append(separator);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Checks if the given String represents a valid integer.
	 * @param s The String to check if it's an integer.
	 * @return Returns true if Integer.parseInt(s) is an integer, returns false otherwise
	 */
	// used by the !n command to check for valid input
	public static boolean isInt(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    
	    return true;
	}
}