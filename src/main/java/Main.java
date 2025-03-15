import java.util.Scanner;
import java.io.File;
import java.util.Arrays;

public class Main {

    static String[] VALID_TYPES = {"echo", "type", "exit"};
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
            case "type":
                if(Arrays.asList(VALID_TYPES).contains(arg)){
                    System.out.println(arg + " is a shell builtin");
                    break;
                }
                String foundPath = checkInPATH(arg);
                if (foundPath != "") {
                    System.out.println(arg + " is " + foundPath + "/" + arg);
                    break;
                }
                System.out.println(arg + ": not found");
                break;
            default:
                System.out.println(input + ": command not found");
        }
    }

    private static String checkInPATH(String arg) {
        String[] paths = System.getenv("PATH").split(":");
        for (String path : paths) {
            File file = new File(path + "/" + arg);
            if (file.exists() && file.isFile() && file.canExecute()) {
                return path;
            }
        }
        return "";
    }

    private static String[] parseInput(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts.length > 0 ? parts[0] : "";
        String arg = parts.length > 1 ? parts[1] : "";
        return new String[]{command, arg};
    }
}
