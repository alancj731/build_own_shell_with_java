import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static String[] VALID_TYPES = { "echo", "type", "exit", "pwd", "cd" };

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] parts = parseInput(input);
            String command = parts[0].trim();
            String arg = parts[1].trim();
            handleCommand(command, arg, input, scanner);
        }
    }

    public static void handleCommand(String command, String arg, String input, Scanner scanner) throws Exception {
        switch (command) {
            case "exit":
                System.exit(0);
                break;
            case "echo":
                String formatedArg = formatArg(arg)[0];
                System.out.println(formatedArg);
                break;
            case "type":
                if (Arrays.asList(VALID_TYPES).contains(arg)) {
                    System.out.println(arg + " is a shell builtin");
                    break;
                }
                String foundPath = checkInPATH(arg);
                if (foundPath != "") {
                    System.out.println(arg + " is " + foundPath.split(":")[0] + "/" + arg);
                    break;
                }
                System.out.println(arg + ": not found");
                break;
            case "pwd":
                System.out.println(System.getProperty("user.dir"));
                break;
            case "cd":
                if (arg.startsWith("~")) {
                    arg = System.getenv("HOME") + arg.substring(1);
                } else if (arg.startsWith("..")) {
                    File file = new File(System.getProperty("user.dir"));
                    File parent = file.getParentFile();
                    arg = parent.getAbsolutePath() + arg.substring(2);
                } else if (arg.startsWith(".")) {
                    arg = System.getProperty("user.dir") + arg.substring(1);
                }

                File file = new File(arg);
                File absoluteFile = file.getCanonicalFile(); // Resolves ".." and other relative parts

                if (absoluteFile.exists() && absoluteFile.isDirectory()) {
                    System.setProperty("user.dir", absoluteFile.getPath());
                    break;
                }
                System.out.println("cd: " + arg + ": No such file or directory");
                break;
            default:
                String execPath = checkInPATH(command);
                if (execPath != "") {
                    String[] parts = execPath.split(":");
                    if (parts[1].equals("executable")) {
                        List<String> commandArgs = new ArrayList<>();
                        commandArgs.add(command);
                        commandArgs.addAll(Arrays.asList(formatArg(arg, true)));
                        // if(command.startsWith("custom_exe")){
                        //     System.out.println("command:");
                        //     System.out.println(commandArgs.get(0));
                        //     System.out.println("args:");
                        //     System.out.println(commandArgs.get(1));
                        // }

                        Process process = new ProcessBuilder(commandArgs).start();
                        String output = new String(process.getInputStream().readAllBytes());                        
                        System.out.print(output);

                    }
                }
                System.out.println(input + ": command not found");
        }
    }

    private static String checkInPATH(String arg) {
        String[] paths = System.getenv("PATH").split(":");
        for (String path : paths) {
            File file = new File(path + "/" + arg);
            if (file.exists() && file.isFile()) {
                if (file.canExecute()) {
                    return path + ":executable";
                } else {
                    return path + ":non_executable";
                }
            }
        }
        return "";
    }

    private static String[] parseInput(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts.length > 0 ? parts[0] : "";
        String arg = parts.length > 1 ? parts[1] : "";
        return new String[] { command, arg };
    }

    private static String[] formatArg(String arg) {
        return formatArg(arg, false);
    }
    
    private static String[] formatArg(String arg, boolean command) {
        // String [] argArr = arg.split("\\s+");
        // if (argArr.length > 1) {
        // for (int i = 0; i < argArr.length; i++) {
        // argArr[i] = argArr[i].trim().replaceAll("\"", "").replaceAll("'", "");
        // }
        // String toReturn = String.join(" ", argArr);
        // return toReturn;
        // }
        // if (arg.contains("\"") || arg.contains("'")) {
        // return arg.replaceAll("\"", "").replaceAll("'", "");
        // }
        // else{
        // return String.join(" ", arg.split("\\s+"));
        // }
        arg = arg.trim();
        String toReturn = "";
        List<String> toReturnList = new ArrayList<>();
        String toProcess = "";
        int numOfSingle = 0;
        int numOfDouble = 0;
        for (int i = 0; i < arg.length(); i++) {
            boolean quotaion = false;
            char c = arg.charAt(i);
            if (arg.charAt(i) == '\"') {
                numOfDouble++;
                quotaion = true;
            }
            if (arg.charAt(i) == '\'') {
                numOfSingle++;
                quotaion = true;
            }
            if (quotaion && ((numOfDouble == 2) || (numOfSingle == 2))) {
                toReturn += toProcess;
                // toReturnList.add(c + "" + toProcess + c); 
                toReturnList.add(toProcess);
                toProcess = "";
                numOfDouble = numOfDouble % 2;
                numOfSingle = numOfSingle % 2;
                continue;
            }

            if (quotaion && (numOfDouble == 1 || numOfSingle == 1)) {
                if (toProcess != "") {
                    toReturn += toProcess.replaceAll("\\s+", " ");
                    toReturnList.add(toProcess);
                    toProcess = "";
                }
                continue;
            }

            toProcess += arg.charAt(i);
        }
        if (toProcess != "") {
            toReturn += toProcess.trim().replaceAll("\\s+", " ");
            toReturnList.add(toProcess);
        }
        if (!command) {
            return new String[] {toReturn};
        }
        return toReturnList.toArray(new String[0]);
    }
}
