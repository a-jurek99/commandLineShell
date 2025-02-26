import java.util.Scanner;

public class CommandShell {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String userInput = scan.nextLine();
            Parser parser = new Parser(userInput);
            ProcessNode root;
            try {
                root = parser.parse();
            } catch (Parser.SyntaxException ex) {
                System.out.println(ex.toString());
                continue;
            }
            System.out.println(root.toString());
            Executor executor = new Executor(root);
            boolean shouldExit = executor.execute();
            if (shouldExit) {
                break;
            }
        }
        scan.close();
    }
}
