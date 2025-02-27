import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;

public class BuiltinExecutable implements Executable, Runnable {
  /**
   * A set of all the builtin commands available
   */
  public static final HashSet<String> ALL_BUILTINS = new HashSet<String>(
      Arrays.asList("cd", "echo", "pwd", "history", "source", "exit"));

  private String cmd; // The command to run
  private String[] args; // All the arguments of the command
  private Thread thread; // The thread that the command is executed in
  private Optional<Integer> exitValue; // The return value of the command
  private Executor executor; // The executor that ran this command
  private String outputFile; // A file to direct output to
  private boolean appendOutput; // True to append output to an existing file, false to overwrite

  /**
   * Construct a BuiltinExecutable
   * 
   * @param cmd      The command to run. Must be a member of ALL_BUILTINs
   * @param args     The arguments to the command
   * @param executor The executor that is running this command
   */
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

  /**
   * Write output to the appropriate place
   * 
   * @param output The output that needs to be written
   */
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
    } else if (cmd.equals("source")) {
      output = source();
    } else if (cmd.equals("exit")) {
      output = exit();
    } else {
      throw new RuntimeException("This should be impossible, the command existing was already checked");
    }

    writeOutput(output);
  }

  /**
   * Run the cd command, which changes the cwd
   * 
   * @return The output of the command
   */
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

  /**
   * Run the echo command, which prints it's input
   * 
   * @return The output of the command
   */
  private Iterable<String> echo() {
    exitValue = Optional.of(0);
    ArrayList<String> output = new ArrayList<>();
    for (int i = 0; i < args.length; i++) {
      output.add(args[i]);
    }
    output.add("\n");
    return output;
  }

  /**
   * Run the pwd command, which prints the cwd
   * 
   * @return The output of the command
   */
  private Iterable<String> pwd() {
    exitValue = Optional.of(0);
    return Arrays.asList(executor.pwd(), "\n");
  }

  /**
   * Run the history command, which prints out the previous commands entered
   * 
   * @return The output of the command
   */
  private Iterable<String> history() {
    File historyFile = executor.readHistory();
    if (historyFile == null) {
      exitValue = Optional.of(1);
      return Arrays.asList();
    }
    exitValue = Optional.of(0);
    return new FileIterable(historyFile);
  }

  /**
   * Run the source command, which reads a file and executes each line of it as a
   * command
   * 
   * @return The output of the command
   */
  private Iterable<String> source() {
    ArrayList<String> output = new ArrayList<>();
    exitValue = Optional.of(0);
    Executor executor = new Executor();
    for (int i = 0; i < args.length; i++) {
      File file = new File(args[i]);
      if (!file.exists() || !file.canRead()) {
        output.add("ERROR: File not readable: " + args[i] + "\n");
        continue;
      }
      Scanner scan;
      try {
        scan = new Scanner(file);
      } catch (FileNotFoundException ex) {
        output.add("ERROR: File not found: " + args[i] + "\n");
        continue;
      }
      int line = 1;
      while (scan.hasNextLine()) {
        String input = scan.nextLine();
        executor.addHistory(input);
        Parser parser = new Parser(input);
        ProcessNode root;
        try {
          root = parser.parse();
        } catch (Parser.SyntaxException ex) {
          System.out.println("Syntax error in " + args[i] + " on line " + line + ":\n");
          System.out.println(ex.toString() + "\n");
          continue;
        }
        if (root == null) {
          continue;
        }
        // System.out.println(root.toString());
        boolean shouldExit = executor.execute(root);
        if (shouldExit) {
          break;
        }
        line += 1;
      }
      scan.close();
    }
    return output;
  }

  /**
   * Run the exit command, which exits the shell
   * 
   * @return The output of the command
   */
  private Iterable<String> exit() {
    exitValue = Optional.of(0);
    executor.exit();
    return new ArrayList<>();
  }
}
