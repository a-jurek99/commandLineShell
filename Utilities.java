import java.io.File;
import java.util.HashSet;
import org.apache.commons.text.similarity.LevenshteinDistance;

/*
 * called from executor if given file cannot be found
 * parse all files in current directory, place in a set, compare levenshtein value to given user attempt
 * print out top 5-10 "matches"
 */
public class Utilities {

    private static HashSet<String> fileSet;

    public void unknownFileString(String badFile) {
       // String unfoundFile = badFile;
       String test = "testing";
    }

    public static void main(String[] args) {
        String path = System.getenv("PATH");
        String[] splitPath = path.split(";");
        fileSet = new HashSet<>();
        parseDir(splitPath);
        smallestLevList();
    }

    //takes each directory from the PATH environment and passes it to build fileSet
    private static void parseDir(String[] splitPath) {
        for (String i : splitPath) {
            File dirToParse = new File(i);
            if(dirToParse.isDirectory()) {
                buildFileSet(dirToParse);
            }
        }
    }

    //intakes a directory and gets each file name and adds it to the fileSet
    private static void buildFileSet(File dirToParse) {
        String[] dirFiles = dirToParse.list();
        for (String i: dirFiles) {
            fileSet.add(i);
        }
    }

    //TODO: figure out how to properly impliment the LevenshtienDistance class
    private static void smallestLevList() {
        //LevenshteinDistance distance = new LevenshteinDistance();
    }
}