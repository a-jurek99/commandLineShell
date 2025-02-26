import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GroupExecutable implements Executable {

  private Executable[] members;
  private ProcessGroup.Type type;
  private File[] tempFiles;

  public GroupExecutable(ProcessGroup.Type type, Executable[] members) {
    this.type = type;
    this.members = members;
  }

  @Override
  public void start() throws Executor.ExecutionException {
    if (type == ProcessGroup.Type.Pipe) {
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

}
