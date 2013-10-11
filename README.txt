Read Me file for shell and related classes
-------------------------------------------
Table of Contents:
  - Credits
  - How To Use
  - Technical Details of Files


Credits
-------------------------------------------
** Created by Jacob Murphy
** jcmurphy@brandeis.edu
** For Brandeis COSI 31a, Fall 2013
I, Jacob Murphy, promise that the work presented in these 
files are my own.



How To Use
-------------------------------------------
Command Name: cat
Arguments: one or more file names separated by spaces
Description: Outputs the contents of the given files
	     cat should be the first command in a string of commands.
Example input: cat file1.txt file2.txt

Command Name: grep
Arguments: one search string
Description: Follows a piped input and outputs the lines 
             of the input containing the search string
Example input: cat file.txt | grep string

Command Name: lc
Arguments: none
Description: Follows a piped input and outputs 
             the number of lines in the input
Example input: cat file1.txt file2.txt | lc

Command Name: history
Arguments: none
Description: Outputs the list of commands executed so far.
	     history should be the first command in a string of commands.
Example input: history

Command: !n
Arguments: n must be an integer
Description: Execute the nth command in the history of commands
Example input: !4
**NOTE: !n is the only command that doesn't show up in history

Command: pwd
Arguments: none
Description: Outputs the current working directory
	     cat should be the first command in a string of commands.
Example input: pwd

Command: ls
Arguments: none
Description: Outputs the contents of the current working directory
Example input: ls | grep txt

Command: cd
Arguments: Name of a subfolder, .. to move up a directory, 
           . for current directory, or nothing to go to the root directory
Description: Changes the current working directory
Example inputs: cd
                cd .
                cd ..
                cd subfolder/subsubfolder

Command: exit
Arguments: none
Description: leaves the shell and returns to the regular terminal/command prompt
Example inputs: exit



Technical Details of Files
-------------------------------------------
shell.java:
- main file for the shell program
- uses a REPL loop for user input
- uses threads for piped input

Cat.java
- for the "cat" command
- implements runnable
- accepts a LinkedList of input files and
  a LinkedBlockingQueue for output

DirectoryPrinter.java
- for the "pwd" command
- implements runnable
- accepts a String of the current working directory from the invoking class
  and a LinkedBlockingQueue for output
- the output LinkedBlockingQueue can be set later with setOut method
  which accepts a LinkedBlockingQueue
- function getDir() is used to return the directory without sending it
  through a LinkedBlockingQueue, mostly used for File creation and input prompts

History.java
- for the "history" command
- implements runnable
- accepts an ArrayList of commands that is filled from the invoking class
  and a LinkedBlockingQueue for output
- the output LinkedBlockingQueue can be set later with setOut method
  which accepts a LinkedBlockingQueue
- has method getCommands() which returns the ArrayList of commands,
  only used by !n command

LineCount.java
- for the "lc" command
- implements runnable
- accepts input and output LinkedBlockingQueues

DirectoryList.java
- for the "ls" command
- implements runnable
- accepts a String representing the current working directory
  and a LinkedBlockingQueue for output

Grep.java
- for the "grep" command
- implements runnable
- accepts a String to search for and an input LinkedBlockingQueue to search through
  and an output LinkedBlockingQueue to send lines with the search string in them to

Exclam.java
- for the "!n" command
- implements runnable
- accepts a LinkedBlockingQueue that comes from running parser with a given command
  and Exclam accepts a LinkedBlockingQueue to send out the messages in the in queue
 
