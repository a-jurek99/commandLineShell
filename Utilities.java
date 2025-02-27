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

    /** intakes a string of an attempted file name, parses through PATH and determines closest matches of files that do exist
    * @param userInput treated as an attempt by the user to enter a file name but was not found or does not exist
    * @return didYouMean a string containg the names of 5 existing files in PATH that are determined to be the closest matches to userInput
    */
    public static String findBestMatch(String userInput) {
        String path = System.getenv("PATH"); 
        String[] splitPath = path.split(":"); //string array of all directories in the PATH env
        fileSet = new HashSet<>();
        parseDir(splitPath); //build the string set of each file name
        smallestLevFiles = new String[5]; //will hold the 5 closest existing file name matches based on lev distance
        smallestLevDists = new int[smallestLevFiles.length];
        getLevDists(userInput);
        String didYouMean = buildFileString();
        return didYouMean;
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

    //calculate the levenshtein distance between the user input and every parsed file name from PATH
    private static void getLevDists(String userInput) {
        for(String flnm: fileSet) {
            LevenshteinDistance levD = new LevenshteinDistance();
            int distance = levD.apply(flnm, userInput);
            closest5(flnm, distance);  //compare with current closest matches
        }
    }

    //
    private static void closest5(String flnm, int levDist) {
        int nullIndex = -1; //if the array(s) contain a null element, this will hold the index of where
        for(int i = 0; i < smallestLevDists.length; i++) {
            if(smallestLevDists[i] == 0) {
                nullIndex = i;
            }
        }
        if(nullIndex == -1) { //the array(s) are "full"
            int bigIndx = largestDist(); //indicates which index holds the element with the largest lev distance
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

    //finds the index of the element with the largest levenshtein distance, returns that index to be compared with user input
    private static int largestDist() { 
        int lgstIndex = 0;
        for(int i = 1; i < smallestLevDists.length; i++) {
            if(smallestLevDists[i] < smallestLevDists[lgstIndex]){
                lgstIndex = i;
            }
        }
        return lgstIndex;
    }

    //turns the array of the closest file names into a single string
    private static String buildFileString() {
        StringBuilder builder = new StringBuilder("\nDid you mean: ");
        for(String i: smallestLevFiles) {
            builder.append("\n  " + i);
        }
        return builder.substring(0);
    }
}