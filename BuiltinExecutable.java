import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public class BuiltinExecutable implements Executable, Runnable {
  public static final HashSet<String> ALL_BULTINS = new HashSet<String>(
      Arrays.asList("cd", "echo", "pwd", "history", "exit"));

  private String cmd;
  private String[] args;
  private Thread thread;
  private Optional<Integer> exitValue;
  private Executor executor;
  private String outputFile;
  private boolean appendOutput;

  public BuiltinExecutable(String cmd, String[] args, Executor executor) {
    this.cmd = cmd;
    this.args = args;
    thread = new Thread(this);
    exitValue = Optional.empty();
    this.executor = executor;
  }

  @Override
  public void start() {
    thread.start();
  }

  @Override
  public Optional<Integer> exitValue() {
    return exitValue;
  }

  @Override
  public void waitFor() throws InterruptedException {
    thread.join();
  }

  @Override
  public void redirectOutput(String file, boolean append) {
    outputFile = file;
    appendOutput = append;
  }

  @Override
  public void redirectInput(String file) {
  }

  private void writeOutput(Iterable<String> output) {
    if (outputFile == null) {
      // Empty outputFile means output to the terminal
      for (String str : output) {
        System.out.print(str);
      }
    } else {
      try {
        File file = new File(outputFile);
        if (!file.exists()) {
          // If the file does not exist, create it
          file.createNewFile();
        }
        FileWriter writer = new FileWriter(file, appendOutput);
        for (String str : output) {
          writer.write(str);
        }
        writer.flush();
        writer.close();
      } catch (IOException ex) {
        // For now, just silently fail
        return;
      }
    }
  }

  @Override
  public String threadInfo() {
    return "[" + thread.getId() + "] " + thread.getName() + " (" + thread.getState() + ")";
  }

  @Override
  public void run() {
    Iterable<String> output;
    if (cmd.equals("cd")) {
      output = cd();
    } else if (cmd.equals("echo")) {
      output = echo();
    } else if (cmd.equals("pwd")) {
      output = pwd();
    } else if (cmd.equals("history")) {
      output = history();
    } else if (cmd.equals("exit")) {
      output = exit();
    } else {
      throw new RuntimeException("This should be impossible, the command existing was already checked");
    }

    writeOutput(output);
  }

  private Iterable<String> cd() {
    ArrayList<String> output = new ArrayList<>(1);
    exitValue = Optional.of(0);
    if (args.length == 0) {
      executor.cd(System.getProperty("user.home"));
    } else if (args.length == 1) {
      String dest = args[0];
      if (dest.equals("-")) {
        executor.cdPrev();
      } else {
        File target;
        if (dest.startsWith("/")) {
          target = new File(dest);
        } else {
          target = new File(executor.pwd(), dest);
        }
        if (!target.exists()) {
          exitValue = Optional.of(1);
          output.add("ERROR: Does not exist: " + dest);
        } else if (!target.isDirectory()) {
          exitValue = Optional.of(1);
          output.add("ERROR: Not a directory: " + dest);
        } else {
          try {
            executor.cd(target.getCanonicalPath());
          } catch (IOException ex) {
            exitValue = Optional.of(1);
            output.add("ERROR: " + ex.getMessage());
          }
        }
      }
    } else {
      exitValue = Optional.of(1);
      output.add("ERROR: 'cd' accepts only one argument.");
    }
    if (output.size() > 0) {
      output.add("\n");
    }
    return output;
  }

  private Iterable<String> echo() {
    exitValue = Optional.of(0);
    ArrayList<String> output = new ArrayList<>();
    for (int i = 0; i < args.length; i++) {
      output.add(args[i]);
    }
    output.add("\n");
    return output;
  }

  private Iterable<String> pwd() {
    exitValue = Optional.of(0);
    return Arrays.asList(executor.pwd(), "\n");
  }

  private Iterable<String> history() {
    File historyFile = executor.readHistory();
    if (historyFile == null) {
      exitValue = Optional.of(1);
      return Arrays.asList();
    }
    exitValue = Optional.of(0);
    return new FileIterable(historyFile);
  }

  private Iterable<String> exit() {
    exitValue = Optional.of(0);
    executor.exit();
    return new ArrayList<>();
  }
}
