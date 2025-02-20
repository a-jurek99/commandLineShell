import java.util.Scanner;

public class CommandShell {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.print("this is our command line: ");
            String userInput = scan.nextLine();
            Parser parser = new Parser(userInput);
            try {
                ProcessNode root = parser.parse();
                System.out.println(root.toString());
            } catch (Parser.SyntaxException ex) {
                System.err.println(ex.toString());
            }
            // get return value(s) from parser
            // pass to executor
            // get return from executor
            break;
        }
        scan.close();
    }
}
