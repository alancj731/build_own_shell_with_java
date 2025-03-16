import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static String[] VALID_TYPES = { "echo", "type", "exit", "pwd", "cd" };
    static char[] ESCAPE_CHARS = { '\"', '\\' };

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            String[] parts = parseInput(input);
            String command = parts[0].trim();
            String arg = parts[1].trim();
            String redirect = parts[2].trim();
            handleCommand(command, arg, redirect, input, scanner);
        }
    }

    private static void handleRedirect(String content, String redirect) {
        if (redirect.equals("")) {
            System.out.println(content);
            return;
        } else {
            try (FileWriter writer = new FileWriter(redirect)) {
                writer.write(content);
            } catch (IOException e) {
                System.err.println("Error saving content to file: " + e.getMessage());
            }
            return;
        }
    }

    public static void handleCommand(String command, String arg, String redirect, String input, Scanner scanner)
            throws Exception {
        switch (command) {
            case "exit":
                System.exit(0);
                break;
            case "echo":
                String formatedArg = formatArg(arg)[0];
                handleRedirect(formatedArg, redirect);
                break;
            case "type":
                if (Arrays.asList(VALID_TYPES).contains(arg)) {
                    // System.out.println(arg + " is a shell builtin");
                    handleRedirect(arg + " is a shell builtin", redirect);
                    break;
                }
                String foundPath = checkInPATH(arg);
                if (foundPath != "") {
                    // System.out.println(arg + " is " + foundPath.split(":")[0] + arg);
                    handleRedirect(arg + " is " + foundPath.split(":")[0] + arg, redirect);
                    break;
                }
                // System.out.println(arg + ": not found");
                handleRedirect(arg + ": not found", redirect);
                break;
            case "pwd":
                System.out.println(System.getProperty("user.dir"));
                handleRedirect(System.getProperty("user.dir"), redirect);
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
                // System.out.println("cd: " + arg + ": No such file or directory");
                handleRedirect("cd: " + arg + ": No such file or directory", redirect);
                break;
            default:
                String execPath = checkInPATH(command);
                // System.out.println("execPath 71: " + execPath);
                if (execPath != "") {
                    String[] parts = execPath.split(":");
                    if (parts[1].equals("executable")) {
                        List<String> commandArgs = new ArrayList<>();
                        commandArgs.add(command);

                        String[] result = formatArg(arg, true);

                        List<String> argsList = Arrays.asList(result);

                        commandArgs.addAll(argsList);
                        

                        Process process = new ProcessBuilder(commandArgs).start();
                        String output = new String(process.getInputStream().readAllBytes());
                        // System.out.print(output);
                        handleRedirect(output, redirect);
                        break;
                    }
                }
                System.out.println(command + ": command not found");
        }
    }

    private static String checkInPATH(String arg) {
        String[] paths = System.getenv("PATH").split(":");
        for (String path : paths) {

            if (!path.endsWith("/")) {
                path += "/";
            }

            File file = new File(path + arg);

            if (file.exists() && file.isFile()) {
                // System.out.println("Found " + arg + " in " + path);
                if (file.canExecute()) {
                    return path + ":executable";
                } else {
                    return path + ":executable";
                }
            }
        }
        return "";
    }

    private static String[] parseInput(String input) {
        String start = input.substring(0, 1);

        String command = "";
        String rest = "";
        if (start.equals("'") || start.equals("\"")) {
            for (int i = 1; i < input.length(); i++) {
                if (input.charAt(i) == start.charAt(0)) {
                    command = formatArg(input.substring(0, i))[0];
                    rest = input.substring(i + 1);
                    break;
                }
            }
            // System.out.println("Command: " + command);
            // System.out.println("Arg: " + arg);
        } else {
            String[] parts = input.split(" ", 2);
            command = parts.length > 0 ? parts[0] : "";
            rest = parts.length > 1 ? parts[1] : "";
        }

        String[] parts = new String[] { rest, "" };
        ;
        if (input.contains("1>")) {
            parts = rest.split("1>", 2);
        } else if (input.contains(">")) {
            parts = rest.split(">", 2);
        }

        return new String[] { command, parts[0], parts[1] };
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
                            toProcess = addCurrentToProcess(toProcess, c, mode, true);
                            break;
                        default:
                            toProcess = addCurrentToProcess(toProcess, c, mode);
                            break;
                    }

                    if (c == ' ' && (toProcess.trim().length() > 0)) {
                        toReturn += toProcess.trim();
                        toReturnList.add(toProcess.trim());
                        // System.out.println(c);
                        // System.out.println(toProcess);
                        // System.out.println(toReturnList);
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
                            if (toEscape != "") {
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

    private static String addCurrentToProcess(String text, char c, String mode, boolean escape) {
        if (escape) {
            return text + c;
        }

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

    private static String addCurrentToProcess(String text, char c, String mode) {
        return addCurrentToProcess(text, c, mode, false);
    }
}
