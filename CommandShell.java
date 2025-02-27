import java.util.Scanner;

public class CommandShell {
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
