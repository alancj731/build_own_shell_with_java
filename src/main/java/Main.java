import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static String[] VALID_TYPES = { "echo", "type", "exit", "pwd", "cd" };
    static char[] ESCAPE_CHARS = { '\"', '\'', '\\', '$', 'n' };

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

                        String[] result = formatArg(arg, true);

                        List<String> argsList = Arrays.asList(result);

                        // Remove quotes
                        // List<String> argsList = Arrays.stream(result)
                        //         .map(s -> s.replace("'", ""))
                        //         .collect(Collectors.toList());

                        if (!input.startsWith("cat '")) {
                            System.out.print("argList:" + argsList);
                        }

                        commandArgs.addAll(argsList);

                        Process process = new ProcessBuilder(commandArgs).start();
                        String output = new String(process.getInputStream().readAllBytes());
                        System.out.print(output);
                        break;
                    }
                }
                System.out.print(input + ": command not found");
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

    private static String needEscape(String text, int curIndex) {
        if (curIndex >= text.length() - 1) {
            return "";
        }
        char nextChar = text.charAt(curIndex + 1);
        for (char c : ESCAPE_CHARS) {
            if (c == nextChar) {
                return String.valueOf(c);
            }
        }
        return "";
    }

    private static String[] formatArg(String arg, boolean command) {
        arg = arg.trim();
        String toReturn = "";
        List<String> toReturnList = new ArrayList<>();
        String toProcess = "";
        // int numOfSingle = 0;
        // int numOfDouble = 0;
        String mode = "";
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);

            switch (mode) {
                case "":
                    switch (c) {
                        case '\'':
                            mode = "single";
                            break;
                        case '\"':
                            mode = "double";
                            break;
                        case '\\':
                            i += 1;
                            c = arg.charAt(i);
                            toProcess = addCurrentToProcess(toProcess, c, mode);
                            break;
                        default:
                            toProcess = addCurrentToProcess(toProcess, c, mode);
                            break;
                    }
                    if (c != ' ' && toProcess.length() > 0 && toProcess.charAt(toProcess.length() - 1) == ' ') {
                        toReturn += toProcess;
                        toReturnList.add(toProcess);
                        toProcess = "";
                    }
                    break;
                case "single":
                    switch (c) {
                        case '\'':
                            // end of single quote
                            if (toProcess != "") {
                                toReturn += toProcess;
                                toReturnList.add(toProcess);
                                toProcess = "";
                            }
                            mode = "";
                            break;
                        default:
                            toProcess = addCurrentToProcess(toProcess, c, mode);
                            break;
                    }
                    break;
                case "double":
                    switch (c) {
                        case ('\"'): {
                            // end of double quote
                            if (toProcess != "") {
                                toReturn += toProcess;
                                toReturnList.add(toProcess);
                                toProcess = "";
                            }
                            mode = "";
                            break;
                        }
                        case '\\': {
                            String toEscape = needEscape(arg, i);
                            if ( toEscape != "") {
                                i += 1;
                                c = arg.charAt(i);
                                toProcess = addCurrentToProcess(toProcess, c, mode);
                            } else {
                                toProcess = addCurrentToProcess(toProcess, c, mode);
                            }
                            break;
                        }
                        default: {
                            toProcess = addCurrentToProcess(toProcess, c, mode);
                            break;
                        }
                    }
                    break;
            }

            // if (c == '\\' && mode != "single") {
            // i += 1;
            // toProcess = addCurrentToProcess(toProcess, arg.charAt(i), mode);
            // continue;
            // }
            // if (c == '\'') {
            // numOfSingle++;
            // if( mode == ""){
            // mode = "single";
            // continue;
            // }
            // else if (mode == "single") {
            // mode = numOfDouble > 0 ? "double" : "";
            // continue;
            // }
            // else{
            // // double quote before this single quote
            // toProcess += c;

            // }
            // if (mode != "") {
            // if (numOfDouble > 0) {
            // // there is a double quote before this single quote
            // toProcess += c;
            // }

            // if (numOfSingle == 1) {
            // mode = "single";
            // private static String handleBackslash(String text) {
            // return text.replaceAll("\\\\([^\\\\])", "$1") // Remove \ before any non-\
            // character
            // .replaceAll("\\\\\\\\", "\\\\"); // Replace \\ with \
            // }
            // mode = "";
            // }
            // }
            // continue;
            // }
            // // mode = "";
            // if (numOfSingle == 1) {
            // mode = "single";
            // // if (toProcess != "") {
            // // String toAdd = handleBackslash(toProcess.replaceAll("\\s+", " "));
            // // toReturn += toProcess;
            // // toReturnList.add(toProcess);
            // // toProcess = "";
            // // }
            // continue;
            // } else { // numOfSingle == 2
            // mode = "";
            // numOfSingle = 0;
            // // toReturn += handleBackslash(toProcess);
            // // toReturnList.add(handleBackslash(toProcess));
            // // toReturn += toProcess;
            // // toReturnList.add(toProcess);
            // // toProcess = "";
            // continue;
            // }
            // }
            // if (c == '\"') {
            // if (mode == "single" && numOfDouble == 0) {
            // toProcess += c;
            // continue;
            // }
            // numOfDouble++;
            // if (numOfDouble == 1) {
            // mode = "double";
            // if (toProcess != "") {
            // // String toAdd = handleBackslash(toProcess.replaceAll("\\s+", " "));
            // toReturn += toProcess;
            // toReturnList.add(toProcess);
            // toProcess = "";
            // }
            // continue;
            // } else { // numOfDouble == 2
            // mode = "";
            // numOfDouble = 0;
            // // toReturn += handleBackslash(toProcess);
            // // toReturnList.add(handleBackslash(toProcess));
            // toReturn += toProcess;
            // toReturnList.add(toProcess);
            // toProcess = "";
            // continue;
            // }
            // }

            // toProcess = addCurrentToProcess(toProcess, c, mode);
        }
        if (toProcess != "") {
            // toReturn += handleBackslash(toProcess.trim().replaceAll("\\s+", " "));
            // toReturnList.add(handleBackslash(toProcess));
            toReturn += toProcess;
            toReturnList.add(toProcess);
        }
        if (!command) {
            return new String[] { toReturn };
        }

        toReturnList.replaceAll(String::trim);
        toReturnList.removeIf(String::isEmpty);
        return toReturnList.toArray(new String[0]);
    }

    private static String addCurrentToProcess(String text, char c, String mode) {
        if (mode == "") {
            if (c == ' ' && text.length() > 0 && text.charAt(text.length() - 1) == c) {
                return text;
            } else {
                return text + c;
            }
        } else {
            return text + c;
        }
    }
}
