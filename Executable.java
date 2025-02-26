import java.util.Optional;

/**
 * Represents an executable
 */
public interface Executable {
  /**
   * Start execution
   */
  public void start() throws Executor.ExecutionException;

  /**
   * Get the exit value of this execution, or empty if execution has not completed
   */
  public Optional<Integer> exitValue();

  /**
   * Wait for the execution to be complete
   */
  public void waitFor() throws InterruptedException;

  /**
   * Set input redirection, so input comes from the given file
   */
  public void redirectInput(String file);

  /**
   * Set output redirection, so output goes to a given file, and set whether to
   * overwrite or append to an existing file
   */
  public void redirectOutput(String file, boolean append);
}
