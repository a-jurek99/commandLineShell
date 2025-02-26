/**
 * Represents a node in the process tree. This can be given files for input or
 * output, along with being told to run as a background process. Note that only
 * the root node of the tree should have the background flag set.
 */
public abstract class ProcessNode {
  String inputFile;
  String outputFile;
  boolean appendOutput;
  boolean background;

  abstract Executable resolve(Executor executor);

  public Executable execute(Executor executor) {
    Executable executable = this.resolve(executor);
    if (executable == null)
      return null;
    if (inputFile != null) {
      executable.redirectInput(inputFile);
    }
    if (outputFile != null) {
      executable.redirectOutput(outputFile, appendOutput);
    }
    return executable;
  }

  /**
   * Set an input file that this node is using
   * 
   * @param file The file to take input from
   */
  public void setInput(String file) {
    inputFile = file;
  }

  /**
   * Set an output file that this node is using
   * 
   * @param file   The file to put output in
   * @param append If true, append output to the file. If false, replace the
   *               file's contents with the output
   */
  public void setOutput(String file, boolean append) {
    outputFile = file;
    appendOutput = append;
  }

  /**
   * Set if this process should run in the background or not
   * 
   * @param background True if this process should run in the background, false if
   *                   not
   */
  public void setBackground(boolean background) {
    this.background = background;
  }

  abstract void buildString(StringBuilder builder);

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    this.buildString(builder);
    if (this.inputFile != null) {
      builder.append(" < ");
      builder.append(this.inputFile);
    }
    if (this.outputFile != null) {
      builder.append(' ');
      builder.append(this.appendOutput ? ">>" : ">");
      builder.append(' ');
      builder.append(this.outputFile);
    }
    builder.append(')');
    return builder.toString();
  }
}
