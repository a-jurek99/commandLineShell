/**
 * Represents a group of commands executed together using "&&" or "&".
 * Needs to be a ProcessNode itself to support things like "(command1 &&
 * command2) & command3" Where command1 and command2 are executed sequentially,
 * and command3 is executed in parallel to both.
 */
public class ProcessGroup extends ProcessNode {
  public Type type; // The type of group this is
  public ProcessNode[] members; // All the members of this group

  /**
   * Create a process group
   * 
   * @param members The members of the group
   * @param type    The type of the group
   */
  public ProcessGroup(ProcessNode[] members, Type type) {
    this.members = members;
    this.type = type;
  }

  @Override
  protected Executable resolve(Executor executor) {
    Executable[] executables = new Executable[members.length];
    for (int i = 0; i < members.length; i++) {
      executables[i] = members[i].execute(executor);
    }
    Executable executable = new GroupExecutable(type, executables);
    return executable;
  }

  @Override
  protected void buildString(StringBuilder builder) {
    switch (type) {
      case Parallel:
        builder.append('&');
        break;
      case Sequential:
        builder.append("&&");
        break;
      case Pipe:
        builder.append('|');
        break;
    }
    for (int i = 0; i < members.length; i++) {
      builder.append(' ');
      builder.append(members[i].toString());
    }
  }

  static enum Type {
    Parallel, // Built using '&', runs all processes in parellel
    Sequential, // Built using '&&', runs each process in order
    Pipe, // Built using '|', runs each process with the output from the previous
  }
}
