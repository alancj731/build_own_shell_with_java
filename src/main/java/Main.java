import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLEditorKit.Parser;

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
        InputParser parser = new InputParser();
        Commander commander = new Commander();
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            parser.parseInput(input);
            commander.set(parser.Command, parser.args, parser.argsStr);
            commander.run();
            if (commander.output != "") {
                System.out.println(commander.output);
            }
            // String[] parts = parseInput(input);
            // String command = parts[0].trim();
            // String arg = parts[1].trim();
            // String redirect = parts[2].trim();
            // String errRedirect = parts[3].trim();

            // System.out.println("outRedirect: " + redirect);
            // System.out.println("errRedirect: " + errRedirect);
            // handleCommand(command, arg, redirect, errRedirect);
        }
    }
}

    // private static void handleRedirect(String content, String redirect) {
    //     handleRedirect(content, redirect, "", "", false);
    // }

    // private static void handleRedirect(String content, String redirect, boolean newLine) {
    //     handleRedirect(content, redirect, "", "", newLine);
    // }

    // private static void handleRedirect(String content, String redirect, String err, String errRedirect,
    //         String outAppend, String errAppend, boolean newLine) {

    //     if (redirect.equals("")) {
    //         System.out.print(content);
    //         if (newLine) {
    //             System.out.println();
    //         }
    //     } else {
    //         try (FileWriter writer = new FileWriter(redirect)) {
    //             writer.write(content);
    //             if (newLine) {
    //                 writer.write("\n");
    //             }
    //         } catch (IOException e) {
    //             System.err.println("Error saving content to file: " + e.getMessage());
    //         }
    //     }

    //     if (errRedirect.equals("")) {
    //         if (err.length() > 0) {
    //             System.err.print(err);
    //             if (newLine) {
    //                 System.err.println();
    //             }
    //         }
    //     } else {
    //         try (FileWriter writer = new FileWriter(errRedirect)) {
    //             writer.write(err);
    //         } catch (IOException e) {
    //             System.err.println("Error saving content to file: " + e.getMessage());
    //         }
    //     }

    // }

//     public static void handleCommand(String command, String arg, String redirect, String errRedirect)
//             throws Exception {
//         switch (command) {
//             case "exit":
//                 System.exit(0);
//                 break;
//             case "echo":
//                 String formatedArg = formatArg(arg)[0];
//                 // System.out.println("formatedArg:"+formatedArg);
//                 // System.out.println("redirect:"+redirect);

//                 handleRedirect(formatedArg, redirect, "", errRedirect, true);
//                 break;
//             case "type":
//                 if (Arrays.asList(VALID_TYPES).contains(arg)) {
//                     // System.out.println(arg + " is a shell builtin");
//                     handleRedirect(arg + " is a shell builtin", redirect, true);
//                     break;
//                 }
//                 String foundPath = checkInPATH(arg);
//                 if (foundPath != "") {
//                     // System.out.println(arg + " is " + foundPath.split(":")[0] + arg);
//                     handleRedirect(arg + " is " + foundPath.split(":")[0] + arg, redirect, true);
//                     break;
//                 }
//                 // System.out.println(arg + ": not found");
//                 handleRedirect(arg + ": not found", redirect, true);
//                 break;
//             case "pwd":
//                 // System.out.println(System.getProperty("user.dir"));
//                 handleRedirect(System.getProperty("user.dir"), redirect, true);
//                 break;
//             case "cd":
//                 if (arg.startsWith("~")) {
//                     arg = System.getenv("HOME") + arg.substring(1);
//                 } else if (arg.startsWith("..")) {
//                     File file = new File(System.getProperty("user.dir"));
//                     File parent = file.getParentFile();
//                     arg = parent.getAbsolutePath() + arg.substring(2);
//                 } else if (arg.startsWith(".")) {
//                     arg = System.getProperty("user.dir") + arg.substring(1);
//                 }

//                 File file = new File(arg);
//                 File absoluteFile = file.getCanonicalFile(); // Resolves ".." and other relative parts

//                 if (absoluteFile.exists() && absoluteFile.isDirectory()) {
//                     System.setProperty("user.dir", absoluteFile.getPath());
//                     break;
//                 }
//                 // System.out.println("cd: " + arg + ": No such file or directory");
//                 handleRedirect("cd: " + arg + ": No such file or directory", redirect, true);
//                 break;
//             default:
//                 String execPath = checkInPATH(command);
//                 // System.out.println("execPath 71: " + execPath);
//                 if (execPath != "") {
//                     String[] parts = execPath.split(":");
//                     if (parts[1].equals("executable")) {
//                         List<String> commandArgs = new ArrayList<>();
//                         commandArgs.add(command);

//                         String[] result = formatArg(arg, true);

//                         List<String> argsList = Arrays.asList(result);

//                         commandArgs.addAll(argsList);

//                         Process process = new ProcessBuilder(commandArgs).start();
//                         String output = new String(process.getInputStream().readAllBytes());
//                         String error = new String(process.getErrorStream().readAllBytes());
//                         handleRedirect(output, redirect, error, errRedirect, false);
//                         break;
//                     }
//                 }
//                 System.out.println(command + ": command not found");
//         }
//     }

//     private static String checkInPATH(String arg) {
//         String[] paths = System.getenv("PATH").split(":");
//         for (String path : paths) {

//             if (!path.endsWith("/")) {
//                 path += "/";
//             }

//             File file = new File(path + arg);

//             if (file.exists() && file.isFile()) {
//                 // System.out.println("Found " + arg + " in " + path);
//                 if (file.canExecute()) {
//                     return path + ":executable";
//                 } else {
//                     return path + ":executable";
//                 }
//             }
//         }
//         return "";
//     }

//     private static String[] parseInput(String input) {
//         String start = input.substring(0, 1);

//         String command = "";
//         String rest = "";
//         if (start.equals("'") || start.equals("\"")) {
//             for (int i = 1; i < input.length(); i++) {
//                 if (input.charAt(i) == start.charAt(0)) {
//                     command = formatArg(input.substring(0, i))[0];
//                     rest = input.substring(i + 1);
//                     break;
//                 }
//             }

//         } else {
//             String[] parts = input.split(" ", 2);
//             command = parts.length > 0 ? parts[0] : "";
//             rest = parts.length > 1 ? parts[1] : "";
//         }

//         String[] parts = new String[] { rest, "" };
//         String regex = "(1>>|2>>|1>|2>|>)";
//         Pattern pattern = Pattern.compile(regex);
//         Matcher matcher = pattern.matcher(rest);

//         String outRedirect = "";
//         String errRedirect = "";
//         String outAppend = "";
//         String errAppend = "";

//         while (matcher.find()) {
//             String match = matcher.group();

//             if (match.contains("2>>")) {
//                 parts = match.split("2>>", 2);
//                 errAppend = parts[1].trim();
//             } else if (match.contains("1>>")) {
//                 parts = match.split("1>>", 2);
//                 outAppend = parts[1].trim();
//             } else if (match.contains("2>")) {
//                 parts = match.split("2>", 2);
//                 errRedirect = parts[1].trim();
//             } else if (match.contains("1>")) {
//                 parts = match.split("1>", 2);
//                 outRedirect = parts[1].trim();
//             } else if (match.contains(">")) {
//                 parts = match.split(">", 2);
//                 outRedirect = parts[1].trim();
//             }
//         }

//         parts = rest.split("(2>|1>|>)");

//         String arg = parts[0].trim();

//         // if (input.contains("2>"))
//         // if (input.contains("1>")) {
//         // parts = rest.split("1>", 2);
//         // } else if (input.contains(">")) {
//         // parts = rest.split(">", 2);
//         // }

//         return new String[] { command, arg, outRedirect, errRedirect, outAppend, errAppend };
//     }

//     private static String[] formatArg(String arg) {
//         return formatArg(arg, false);
//     }

//     private static String needEscape(String text, int curIndex) {
//         if (curIndex >= text.length() - 1) {
//             return "";
//         }
//         char nextChar = text.charAt(curIndex + 1);
//         for (char c : ESCAPE_CHARS) {
//             if (c == nextChar) {
//                 return String.valueOf(c);
//             }
//         }
//         return "";
//     }

//     private static String[] formatArg(String arg, boolean command) {
//         arg = arg.trim();
//         String toReturn = "";
//         List<String> toReturnList = new ArrayList<>();
//         String toProcess = "";
//         boolean escape = false;
//         // int numOfSingle = 0;
//         // int numOfDouble = 0;
//         String mode = "";
//         for (int i = 0; i < arg.length(); i++) {
//             char c = arg.charAt(i);
//             escape = false;

//             switch (mode) {
//                 case "":
//                     switch (c) {
//                         case '\'':
//                             mode = "single";
//                             // System.out.println("touch here 219");
//                             break;
//                         case '\"':
//                             mode = "double";
//                             break;
//                         case '\\':
//                             i += 1;
//                             c = arg.charAt(i);
//                             // toProcess = addCurrentToProcess(toProcess, c, mode, true);
//                             escape = true;
//                             break;
//                         default:
//                             // toProcess = addCurrentToProcess(toProcess, c, mode);
//                             // System.out.println("toProcess: " + toProcess);
//                             escape = false;
//                             break;
//                     }
//                     if (!escape && (c == '\'' || c == '\"')) {
//                         // System.out.println("touch here 236");
//                         break;
//                     }
//                     if (c != ' ' && (toProcess.length() > 0 && toProcess.charAt(toProcess.length() - 1) == ' ')) {
//                         toReturn += toProcess;
//                         toReturnList.add(toProcess);
//                         toProcess = c + "";
//                     } else {
//                         toProcess = addCurrentToProcess(toProcess, c, mode, escape);
//                     }
//                     // System.out.println("toProcess: " + toProcess);
//                     break;
//                 case "single":
//                     switch (c) {
//                         case '\'':
//                             // end of single quote
//                             if (toProcess != "") {
//                                 toReturn += toProcess;
//                                 toReturnList.add(toProcess);
//                                 toProcess = "";
//                             }
//                             mode = "";
//                             break;
//                         default:
//                             toProcess = addCurrentToProcess(toProcess, c, mode);
//                             break;
//                     }
//                     break;
//                 case "double":
//                     switch (c) {
//                         case ('\"'): {
//                             // end of double quote
//                             if (toProcess != "") {
//                                 toReturn += toProcess;
//                                 toReturnList.add(toProcess);
//                                 toProcess = "";
//                             }
//                             mode = "";
//                             break;
//                         }
//                         case '\\': {
//                             String toEscape = needEscape(arg, i);
//                             if (toEscape != "") {
//                                 i += 1;
//                                 c = arg.charAt(i);
//                                 toProcess = addCurrentToProcess(toProcess, c, mode);
//                             } else {
//                                 toProcess = addCurrentToProcess(toProcess, c, mode);
//                             }
//                             break;
//                         }
//                         default: {
//                             toProcess = addCurrentToProcess(toProcess, c, mode);
//                             break;
//                         }
//                     }
//                     break;
//             }
//         }
//         if (toProcess != "") {
//             // toReturn += handleBackslash(toProcess.trim().replaceAll("\\s+", " "));
//             // toReturnList.add(handleBackslash(toProcess));
//             toReturn += toProcess;
//             toReturnList.add(toProcess);
//         }
//         if (!command) {
//             return new String[] { toReturn };
//         }

//         toReturnList.replaceAll(String::trim);
//         toReturnList.removeIf(String::isEmpty);
//         return toReturnList.toArray(new String[0]);
//     }

//     private static String addCurrentToProcess(String text, char c, String mode, boolean escape) {
//         if (escape) {
//             return text + c;
//         }

//         if (mode == "") {
//             if (c == ' ' && text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
//                 return text;
//             } else {
//                 return text + c;
//             }
//         } else {
//             return text + c;
//         }
//     }

//     private static String addCurrentToProcess(String text, char c, String mode) {
//         return addCurrentToProcess(text, c, mode, false);
//     }
// }
