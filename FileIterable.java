import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class FileIterable implements Iterable<String> {
  private File file;

  public FileIterable(File file) {
    this.file = file;
  }

  @Override
  public Iterator<String> iterator() {
    return new FileIterator(file);
  }

  private static class FileIterator implements Iterator<String> {
    private BufferedReader reader;

    FileIterator(File file) {
      try {
        reader = new BufferedReader(new FileReader(file));
      } catch (IOException ex) {
        reader = null;
      }
    }

    @Override
    public boolean hasNext() {
      try {
        return reader != null && reader.ready();
      } catch (IOException ex) {
        return false;
      }
    }

    @Override
    public String next() {
      try {
        return reader.readLine() + "\n";
      } catch (IOException ex) {
        return null;
      }
    }

  }

}
