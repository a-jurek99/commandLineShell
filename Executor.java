import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class in charge of executing commands, which are now represented by a tree of
 * ProcessNodes. It is also responsible for tracking execution state.
 */
public class Executor {
  private String cwd; // The current working directory of the process
  private String prevCwd; // The previous working directory of the process, used for "cd -"
  private boolean shouldExit; // If the shell should exit after completing the current command
  private File historyFile; // A file object for the history file
  private FileWriter historyWriter; // Used to write each entered command to the history

  /**
   * Construct an executor, initialization is getting the initial cwd and creating
   * the history writer
   */
  public Executor() {
    cwd = prevCwd = System.getProperty("user.dir");
    try {
      historyFile = new File(System.getProperty("user.home"), ".jshhistory");
      if (!historyFile.exists()) {
        historyFile.createNewFile();
      }
      historyWriter = new FileWriter(historyFile, true);
    } catch (IOException ex) {
      // Failed to create historyFile or historyWriter: Just don't record history this
      // sessions
      historyFile = null;
      historyWriter = null;
    }
  }

  /**
   * Execute the command specified by the given root node
   * 
   * @param rootNode The root node of the ProcessNode tree representing the
   *                 command to be run
   * @return True to exit, false to continue running
   */
  public boolean execute(ProcessNode rootNode) {
    Executable root = rootNode.execute(this);
    if (root == null) {
      return false;
    }
    try {
      root.start();
    } catch (Executor.ExecutionException ex) {
      System.out.println("ERROR: " + ex.getMessage());
      return false;
    }
    if (!rootNode.background) {
      try {
        root.waitFor();
      } catch (InterruptedException ex) {
        // Just silently fail
      }
    } else {
      System.out.println(root.threadInfo());
    }
    return shouldExit;
  }

  /**
   * Resolve a command into an executable. Steps, in order:
   * 1) Check if the command is a builtin command. If so, use that.
   * 2) Check if the command starts with "./". If so, search the current directory
   * for the file and execute it if possible.
   * 3) Check if the command is a file in $PATH. If so, use that.
   * 4) Command is not found, use levenshtien distance to find similar commands
   * are suggest the top 5.
   * 
   * @param cmd  The command entered
   * @param args The arguments provided to the command
   * @throws ExecutionException If something goes wrong resolving the command, for
   *                            example it points to a file that isn't executable
   *                            or the command doesn't exist on the $PATH
   * @return An executable representing the command and arguments provided
   */
  public Executable resolveCommand(String cmd, String[] args) throws ExecutionException {
    if (BuiltinExecutable.ALL_BUILTINS.contains(cmd)) {
      return new BuiltinExecutable(cmd, args, this);
    } else if (cmd.startsWith("./")) {
      File exec = new File(cwd, cmd);
      if (exec.canExecute()) {
        return new FileExecutable(exec.getAbsolutePath(), args, this);
      } else {
        throw new ExecutionException("Not executable: " + exec.getAbsolutePath());
      }
    } else {
      String path = System.getenv("PATH");
      String[] splitPath = path.split(":");
      for (int i = 0; i < splitPath.length; i++) {
        File pathDir = new File(splitPath[i]);
        if (pathDir.exists() && pathDir.isDirectory()) {
          File maybeExec = new File(splitPath[i], cmd);
          if (maybeExec.exists() && maybeExec.canExecute()) {
            return new FileExecutable(maybeExec.getAbsolutePath(), args, this);
          }
        }
      }
    }
    throw new ExecutionException("Command not found: " + cmd + Utilities.findBestMatch(cmd));
  }

  /**
   * @return the cwd for this execution.
   */
  public String pwd() {
    return cwd;
  }

  /**
   * Set the cwd for this execution.
   * 
   * @param dir The path of the directory to change the cwd to
   */
  public void cd(String dir) {
    prevCwd = cwd;
    cwd = dir;
  }

  /**
   * Set the cwd for this execution to the previous cwd.
   */
  public void cdPrev() {
    String temp = prevCwd;
    prevCwd = cwd;
    cwd = temp;
  }

  /**
   * Tell the main method to exit
   */
  public void exit() {
    shouldExit = true;
  }

  /**
   * Add an entered command to history
   * 
   * @param cmd The command that was entered
   */
  public void addHistory(String cmd) {
    // If we don't have a writer, the history file is inaccessible
    if (historyWriter == null) {
      return;
    }
    try {
      historyWriter.write(cmd + "\n");
    } catch (IOException ex) {
      // Failed to write some history: Just ignore it
    }
  }

  /**
   * Get the history file and ensure it is prepped for reading by flushing the
   * history writer
   * 
   * @return The history file
   */
  public File readHistory() {
    // If we don't have a writer, the history file is inaccessible
    if (historyFile == null || historyWriter == null) {
      return null;
    }
    try {
      historyWriter.flush();
    } catch (IOException ex) {
      return null;
    }
    return historyFile;
  }

  /**
   * Represents an issue encountered while attempting to execute a command
   */
  static class ExecutionException extends Exception {
    /**
     * Create an exception with the given message
     * 
     * @param message The message describing the cause of the error
     */
    ExecutionException(String message) {
      super(message);
    }

    /**
     * Create an exception with an inner cause
     * 
     * @param cause The inner cause of the exception
     */
    ExecutionException(Throwable cause) {
      super(cause);
    }
  }
}
