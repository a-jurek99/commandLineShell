/**
 * Represents a group of commands executed together using "&&" or "&".
 * Needs to be a ProcessNode itself to support things like "(command1 &&
 * command2) & command3" Where command1 and command2 are executed sequentially,
 * and command3 is executed in parallel to both.
 */
public class ProcessGroup extends ProcessNode {
  Type type;
  ProcessNode[] members;

  public ProcessGroup(ProcessNode[] members, Type type) {
    this.members = members;
    this.type = type;
  }

  @Override
  void buildString(StringBuilder builder) {
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
