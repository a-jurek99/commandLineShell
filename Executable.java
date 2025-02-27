import java.util.Optional;

/**
 * Represents an executable
 */
public interface Executable {
  /**
   * Start execution
   * 
   * @throws Executor.ExecutionException If an issue is encountered when trying to
   *                                     start execution
   */
  public void start() throws Executor.ExecutionException;

  /**
   * Get the exit value of this execution if execution has completed
   * 
   * @return The exit code of the executable, or null if it hasn't finished
   */
  public Optional<Integer> exitValue();

  /**
   * Wait for the execution to be complete
   * 
   * @throws InterruptedException If the executable thread is interrupted while
   *                              waiting
   */
  public void waitFor() throws InterruptedException;

  /**
   * Set input redirection, so input comes from the given file
   * 
   * @param file The file to redirect to
   */
  public void redirectInput(String file);

  /**
   * Set output redirection, so output goes to a given file, and set whether to
   * overwrite or append to an existing file
   * 
   * @param file   The file to redirect to
   * @param append True to append to an existing file, false to overwrite
   */
  public void redirectOutput(String file, boolean append);

  /**
   * Get information about the thread running this Executable
   * 
   * @return A string representation of the Executable process
   */
  public String threadInfo();
}
