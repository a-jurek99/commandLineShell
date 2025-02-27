import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Represents a group of executables, either in parallel or not
 */
public class GroupExecutable implements Executable {
  private Executable[] members; // The executables that make up this group
  private ProcessGroup.Type type; // The type of group
  private File[] tempFiles; // Temporary files to handle pipes

  /**
   * Create a group executable
   * 
   * @param type    The type of the group
   * @param members The members of the group
   */
  public GroupExecutable(ProcessGroup.Type type, Executable[] members) {
    this.type = type;
    this.members = members;
  }

  @Override
  public void start() throws Executor.ExecutionException {
    if (type == ProcessGroup.Type.Pipe) {
      // We handle pipes by creating temporary files between each executable and
      // directing the output of the previous process into the file and the input of
      // the next process from the file
      tempFiles = new File[members.length - 1];
      for (int i = 1; i < members.length; i++) {
        try {
          tempFiles[i - 1] = File.createTempFile("pipe", ".data");
        } catch (IOException ex) {
          throw new Executor.ExecutionException(ex);
        }
        members[i - 1].redirectOutput(tempFiles[i - 1].getAbsolutePath(), false);
        members[i].redirectInput(tempFiles[i - 1].getAbsolutePath());
      }
    }
    if (type == ProcessGroup.Type.Parallel) {
      for (int i = 0; i < members.length; i++) {
        members[i].start();
      }
    } else {
      members[0].start();
      for (int i = 1; i < members.length; i++) {
        try {
          members[i - 1].waitFor();
        } catch (InterruptedException ex) {
          // Skip this one
        }
        if (members[i - 1].exitValue().orElse(1) != 0) {
          // Fail on the first failure
          break;
        }
        members[i].start();
      }
    }
  }

  @Override
  public Optional<Integer> exitValue() {
    for (int i = 0; i < members.length; i++) {
      Optional<Integer> exitValue = members[i].exitValue();
      if (exitValue.orElse(1) != 0) {
        return exitValue;
      }
    }
    return Optional.of(0);
  }

  @Override
  public void waitFor() throws InterruptedException {
    if (type == ProcessGroup.Type.Parallel) {
      for (int i = 0; i < members.length; i++) {
        members[i].waitFor();
      }
    } else {
      members[members.length - 1].waitFor();
    }
  }

  @Override
  public void redirectInput(String file) {
    if (type == ProcessGroup.Type.Parallel) {
      for (int i = 0; i < members.length; i++) {
        members[i].redirectInput(file);
      }
    } else {
      members[0].redirectInput(file);
    }
  }

  @Override
  public void redirectOutput(String file, boolean append) {
    if (type == ProcessGroup.Type.Parallel) {
      for (int i = 0; i < members.length; i++) {
        members[i].redirectOutput(file, append);
      }
    } else {
      members[members.length - 1].redirectOutput(file, append);
    }
  }

  @Override
  public String threadInfo() {
    StringBuilder builder = new StringBuilder();
    if (members.length == 0) {
      // This shouldn't ever happen, but just in case
      builder.append("<NONE>");
    } else if (members.length == 1) {
      // This also shouldn't ever happen, but again, just in case
      builder.append(members[0].threadInfo());
    } else if (members.length == 2) {
      builder.append(members[0].threadInfo());
      builder.append(" and ");
      builder.append(members[1].threadInfo());
    } else {
      for (int i = 0; i < members.length - 2; i++) {
        builder.append(members[i].threadInfo());
        builder.append(", ");
      }
      builder.append(", and ");
      builder.append(members[members.length - 1].threadInfo());
    }
    return builder.toString();
  }

}
