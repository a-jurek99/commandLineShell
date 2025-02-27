import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FileExecutable implements Executable {
  private ProcessBuilder builder; // Used to build the process
  private Process process; // The process that this executable started

  /**
   * Create a file executable
   * 
   * @param file The path to the file to execute
   */
  public FileExecutable(String file, String[] args, Executor executor) {
    String[] command = new String[args.length + 1];
    command[0] = file;
    System.arraycopy(args, 0, command, 1, args.length);
    builder = new ProcessBuilder(command);
    builder.directory(new File(executor.pwd()));
    builder.redirectErrorStream(true);
    builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
  }

  @Override
  public void start() throws Executor.ExecutionException {
    try {
      process = builder.start();
    } catch (IOException ex) {
      throw new Executor.ExecutionException(ex);
    }
  }

  @Override
  public Optional<Integer> exitValue() {
    if (process == null || process.isAlive()) {
      return Optional.empty();
    }
    return Optional.of(process.exitValue());
  }

  @Override
  public void waitFor() throws InterruptedException {
    process.waitFor();
  }

  @Override
  public void redirectOutput(String file, boolean append) {
    builder.redirectOutput(
        append ? ProcessBuilder.Redirect.appendTo(new File(file)) : ProcessBuilder.Redirect.to(new File(file)));

  }

  @Override
  public void redirectInput(String file) {
    builder.redirectInput(new File(file));
  }

  @Override
  public String threadInfo() {
    return process.toString();
  }
}
