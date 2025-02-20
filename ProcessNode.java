/**
 * Represents a node in the process tree. This can be given files for input or
 * output, along with told to pipe to another node.
 */
public abstract class ProcessNode {
  String inputFile;
  String outputFile;
  boolean appendOutput;
  ProcessNode pipeTo;

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
   * Set a pipe from this node to another
   * 
   * @param pipeTo The process node to pipe this node's output to
   */
  public void setPipe(ProcessNode pipeTo) {
    this.pipeTo = pipeTo;
  }

  abstract void buildString(StringBuilder builder);

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    this.buildString(builder);
    if (pipeTo != null) {
      builder.append(" | ");
      builder.append(pipeTo.toString());
    }
    if (this.inputFile != null) {
      builder.append(" < ");
      builder.append(this.inputFile);
    }
    if (this.outputFile != null) {
      builder.append(' ');
      builder.append(this.appendOutput ? "<<" : "<");
      builder.append(' ');
      builder.append(this.outputFile);
    }
    builder.append(')');
    return builder.toString();
  }
}
