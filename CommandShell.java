import java.util.Scanner;

public class CommandShell {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while(true) {
            System.out.print("this is our command line: ");
            String userInput = scan.nextLine();
            //pass to parser
            //get return value(s) from parser
            //pass to executor
            //get return from executor
            System.out.println(userInput);
            break;
        }
        scan.close();
    }
}