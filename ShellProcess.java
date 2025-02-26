import java.util.Arrays;

/**
 * Represents a process that is run on the command line. The main part of the
 * process tree, these are connected either by groups or by pipe relationships.
 */
public class ShellProcess extends ProcessNode {
  String command;
  String[] arguments;

  /**
   * Construct a ShellProcess
   * 
   * @param args All of the arguments provided on the command line, including the
   *             command itself
   */
  public ShellProcess(String[] args) {
    command = args[0];
    arguments = Arrays.copyOfRange(args, 1, args.length);
  }

  @Override
  Executable resolve(Executor executor) {
    Executable executable;
    try {
      executable = executor.resolveCommand(command, arguments);
    } catch (Executor.ExecutionException ex) {
      System.out.println("ERROR: " + ex.toString());
      return null;
    }
    return executable;
  }

  @Override
  void buildString(StringBuilder builder) {
    builder.append(command);
    for (int i = 0; i < arguments.length; i++) {
      builder.append(' ');
      builder.append(arguments[i]);
    }
  }
}
