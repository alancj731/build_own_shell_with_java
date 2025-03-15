import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        while(true){
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] parts = parseInput(input);
            String command = parts[0];
            String arg = parts[1];
            if (command.equals("exit")) {
                break;
            }
            System.out.println(input + ": command not found");
        }
    }

    private static String[] parseInput(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts.length > 0 ? parts[0] : "";
        String arg = parts.length > 1 ? parts[1] : "";
        return new String[]{command, arg};
    }
}
