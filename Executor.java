import java.io.File;

/**
 * Class in charge of executing a command, which is now represented by a tree of
 * ProcessNodes
 */
public class Executor {
  private String cwd;
  private String prevCwd;
  private boolean shouldExit;

  public Executor() {
    cwd = prevCwd = System.getProperty("user.dir");
  }

  /**
   * Execute the command specified by the given root node
   * 
   * @return True to exit, false to continue running
   */
  public boolean execute(ProcessNode rootNode) {
    Executable root = rootNode.execute(this);
    try {
      root.start();
    } catch (Executor.ExecutionException ex) {
      System.out.println("ERROR: " + ex.toString());
      return false;
    }
    try {
      root.waitFor();
    } catch (InterruptedException ex) {
      // Just silently fail
    }
    return shouldExit;
  }

  public Executable resolveCommand(String cmd, String[] args) throws ExecutionException {
    if (BuiltinExecutable.ALL_BULTINS.contains(cmd)) {
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
    throw new ExecutionException("Command not found: " + cmd);
  }

  /**
   * Return the cwd for this execution.
   */
  public String pwd() {
    return cwd;
  }

  /**
   * Set the cwd for this execution.
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

  static class ExecutionException extends Exception {
    ExecutionException(String message) {
      super(message);
    }

    ExecutionException(Throwable cause) {
      super(cause);
    }
  }
}
