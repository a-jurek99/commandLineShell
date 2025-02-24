import java.io.File;
import java.util.*;
import org.apache.commons.text.similarity.LevenshteinDistance;

/*
 * called from executor if given file cannot be found
 * parse all files in current directory, place in a set, compare levenshtein value to given user attempt
 * print out top 5-10 "matches"
 */
public class Utilities {

    private static HashSet<String> fileSet;
    private static String[] smallestLevFiles;
    private static int[] smallestLevDists;


    public static void main(String[] args) {
        String path = System.getenv("PATH");
        String[] splitPath = path.split(";");
        fileSet = new HashSet<>();
        parseDir(splitPath);
        getLevDists();
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

    //TODO: figure out how to impliment levenshtienDistance methods
    private static void getLevDists() {
        //I cannot figure out how to impliment the levenshtienDistance class :(
        smallestLevFiles = new String[5];
        smallestLevDists = new int[smallestLevFiles.length];
        for(String flnm: fileSet) {
            //int distance = levenshtienDistance.apply(flnm, userInput);
            int distance = (int)(Math.random() * 100); //placeholder for lev distance
            closest5(flnm, distance);           
        }
        for(String i: smallestLevFiles) {
            System.out.println(i);
        }
    }

    private static void closest5(String flnm, int levDist) {
       for(int i = 0; i < smallestLevFiles.length; i++) {
            if(smallestLevFiles[i] == null || smallestLevDists[i] > levDist) { //if the given element is null or the lev distance is bigger than the given value
                smallestLevDists[i] = levDist; //replace
                smallestLevFiles[i] = flnm;
                break;
            }
       }
    }
}