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
    public static String[] smallestLevFiles;
    private static int[] smallestLevDists;

    //intakes a string of an attempted file name, parses through PATH and determines closest matches of files that do exist
    public static String[] findBestMatch(String userInput) {
        String path = System.getenv("PATH");
        String[] splitPath = path.split(":");
        fileSet = new HashSet<>();
        parseDir(splitPath);
        smallestLevFiles = new String[5];
        smallestLevDists = new int[smallestLevFiles.length];
        getLevDists();
        return smallestLevFiles;
    }

    public static void main(String[] args) { //for testing
        String[] test = findBestMatch("bubbbles.scr");
        for(String i: test) {
            System.out.println(i);
        }
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

    private static void getLevDists() {
        smallestLevFiles = new String[5];
        smallestLevDists = new int[smallestLevFiles.length];
        String userInput = "bubbleSort.java";//placeholder
        for(String flnm: fileSet) {
            LevenshteinDistance levD = new LevenshteinDistance();
            int distance = levD.apply(flnm, userInput);
            closest5(flnm, distance);           
        }
    }

    private static void closest5(String flnm, int levDist) {
        int nullIndex = -1; //if the array(s) contain a null element, this will hold the index of where
        for(int i = 0; i < smallestLevDists.length; i++) {
            if(smallestLevDists[i] == 0) {
                nullIndex = i;
            }
        }
        if(nullIndex == -1) { //the array(s) are "full"
            int bigIndx = largestDist();
            if(smallestLevDists[bigIndx] > levDist) {
                smallestLevDists[bigIndx] = levDist;
                smallestLevFiles[bigIndx] = flnm;
            }
        }
        else { //array(s) still contain null elements
            smallestLevDists[nullIndex] = levDist;
            smallestLevFiles[nullIndex] = flnm;
        }
    }

    private static int largestDist() { //returns the index at which the file name with the largest levdist is
        int lgstIndex = 0;
        for(int i = 1; i < smallestLevDists.length; i++) {
            if(smallestLevDists[i] < smallestLevDists[lgstIndex]){
                lgstIndex = i;
            }
        }
        return lgstIndex;
    }
}