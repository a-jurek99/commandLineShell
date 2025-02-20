/**
 * Represents a group of commands executed together using "&&" or "&".
 * Needs to be a ProcessNode itself to support things like "(command1 &&
 * command2) & command3" Where command1 and command2 are executed sequentially,
 * and command3 is executed in parallel to both.
 */
public class ProcessGroup extends ProcessNode {
  boolean sequential;
  ProcessNode[] members;

  public ProcessGroup(ProcessNode[] members, boolean sequential) {
    this.members = members;
    this.sequential = sequential;
  }

  @Override
  void buildString(StringBuilder builder) {
    builder.append(sequential ? "&&" : "&");
    for (int i = 0; i < members.length; i++) {
      builder.append(' ');
      builder.append(members[i].toString());
    }
  }
}
