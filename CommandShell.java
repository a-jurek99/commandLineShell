import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Primary entry point of the program, just contains the main method
 */
public class CommandShell {
    /**
     * Entry point of the program, simply loops until told to exit.
     * Each loop involves getting the inputted command, adding it to history,
     * parsing it, and finally executing it.
     * When provided with command line arguments, interprets them as files, and
     * attempts to execute each line of each file as if it was a command.
     * 
     * @param args The command line arguments passed to the shell, if any
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Scanner scan = new Scanner(System.in);
            Executor executor = new Executor();
            while (true) {
                System.out.print("> ");
                String userInput = scan.nextLine();
                executor.addHistory(userInput);
                Parser parser = new Parser(userInput);
                ProcessNode root;
                try {
                    root = parser.parse();
                } catch (Parser.SyntaxException ex) {
                    System.out.println(ex.toString());
                    continue;
                }
                if (root == null) {
                    continue;
                }
                // System.out.println(root.toString());
                boolean shouldExit = executor.execute(root);
                if (shouldExit) {
                    break;
                }
            }
            scan.close();
            try {
                executor.close();
            } catch (IOException ex) {
                // Just ignore it
            }
        } else {
            Executor executor = new Executor();
            for (int i = 0; i < args.length; i++) {
                File file = new File(args[i]);
                if (!file.exists() || !file.canRead()) {
                    System.out.println("ERROR: File not readable: " + args[i]);
                    continue;
                }
                Scanner scan;
                try {
                    scan = new Scanner(file);
                } catch (FileNotFoundException ex) {
                    System.out.println("ERROR: File not found: " + args[i]);
                    continue;
                }
                int line = 1;
                while (scan.hasNextLine()) {
                    String input = scan.nextLine();
                    Parser parser = new Parser(input);
                    ProcessNode root;
                    try {
                        root = parser.parse();
                    } catch (Parser.SyntaxException ex) {
                        System.out.println("Syntax error in " + args[i] + " on line " + line + ":");
                        System.out.println(ex.toString());
                        continue;
                    }
                    if (root == null) {
                        continue;
                    }
                    // System.out.println(root.toString());
                    boolean shouldExit = executor.execute(root);
                    if (shouldExit) {
                        break;
                    }
                    line += 1;
                }
                scan.close();
            }
            try {
                executor.close();
            } catch (IOException ex) {
                // Just ignore it
            }
        }
    }
}
