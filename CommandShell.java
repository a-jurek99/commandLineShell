import java.util.Scanner;

/**
 * Primary entry point of the program, just contains the main method
 */
public class CommandShell {
    /**
     * Entry point of the program, simply loops until told to exit.
     * Each loop involves getting the inputted command, adding it to history,
     * parsing it, and finally executing it
     * 
     * @param args The command line arguments passed to the shell, currently ignores
     */
    public static void main(String[] args) {
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
    }
}
