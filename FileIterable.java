import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * This class iterates over the contents of a text file one line at a time
 */
public class FileIterable implements Iterable<String> {
  private File file; // The file that this object iterates over

  /**
   * Create an iterable for a given file
   * 
   * @param file The file to iterate over
   */
  public FileIterable(File file) {
    this.file = file;
  }

  @Override
  public Iterator<String> iterator() {
    return new FileIterator(file);
  }

  /**
   * Iterates over a file using a BufferedReader
   */
  private static class FileIterator implements Iterator<String> {
    private BufferedReader reader; // The reader used to iterate over the file

    /**
     * Create the iterator
     * 
     * @param file The file to iterate over
     */
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
