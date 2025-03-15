import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        while(true){
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] parts = parseInput(input);
            String command = parts[0].trim();
            String arg = parts[1].trim();
            handleCommand(command, arg, input);
        }
    }

    public static void handleCommand(String command, String arg, String input) throws Exception {
        switch (command) {
            case "exit":
                System.exit(0);
                break;
            case "echo":
                System.out.println(arg);
                break;
            default:
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
