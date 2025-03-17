import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commander {
    public String command;
    public String[] args;
    public String argsStr;
    public String output;
    public String error;
    public static String[] VALID_TYPES = { "echo", "type", "exit", "pwd", "cd" };

    public Commander(String command, String[] args, String argsStr) {
        this.command = command;
        this.args = args;
        this.argsStr = argsStr;
        this.output = "";
        this.error = "";
    }

    public Commander() {
        this.command = "";
        this.args = new String[0];
        this.argsStr = "";
        this.output = "";
        this.error = "";
    }

    public void set(String command, String[] args, String argsStr) {
        this.command = command;
        this.args = args;
        this.argsStr = argsStr;
        this.output = "";
        this.error = "";
    }

    public void showArgs() {
        System.out.println("args:" + this.args.length);
        for (String item : this.args) {
            System.out.println(item);
        }
    }

    public static String searchPath(String command) {
        String[] paths = System.getenv("PATH").split(":");
        for (String path : paths) {

            if (!path.endsWith("/")) {
                path += "/";
            }

            File file = new File(path + command);

            if (file.exists() && file.isFile()) {
                if (file.canExecute()) {
                    return path + ":executable";
                } else {
                    return path + ":executable";
                }
            }
        }
        return "";
    }

    public String run() {
        this.output = "";
        switch (this.command) {
            case "exit":
                System.exit(0);
                break;
            case "echo":
                this.output = this.argsStr;
                break;
            case "pwd":
                this.output = System.getProperty("user.dir") + "\n";
                break;
            case "type":
                if (Arrays.asList(VALID_TYPES).contains(this.argsStr)) {
                    this.output = (this.argsStr + " is a shell builtin");
                } else {
                    String foundPath = searchPath(this.argsStr);
                    if (foundPath != "") {
                        this.output = (this.argsStr + " is " + foundPath.split(":")[0] + this.argsStr);
                    } else {
                        this.output = this.argsStr + ": not found";
                    }
                }
                break;
            case "cd":
                String target = this.argsStr;
                if (this.argsStr.startsWith("~")) {
                    target = System.getenv("HOME") + this.argsStr.substring(1);
                } else if (this.argsStr.startsWith("..")) {
                    File file = new File(System.getProperty("user.dir"));
                    File parent = file.getParentFile();
                    target = parent.getAbsolutePath() + this.argsStr.substring(2);
                } else if (this.argsStr.startsWith(".")) {
                    target = System.getProperty("user.dir") + this.argsStr.substring(1);
                }
                try {
                    File targetFile = new File(target);
                    File absoluteTargetFile = targetFile.getCanonicalFile();

                    if (absoluteTargetFile.exists() && absoluteTargetFile.isDirectory()) {
                        System.setProperty("user.dir", absoluteTargetFile.getPath());
                        break;
                    }
                    this.output = "cd: " + this.argsStr + ": No such file or directory";
                } catch (IOException e) {
                    this.output = "cd: " + this.argsStr + ": No such file or directory";
                }
                break;
            default:
                // run command
                String execPath = searchPath(this.command);
                if (execPath == "") {
                    this.output = this.command + ": command not found";
                    break;
                }

                String[] parts = execPath.split(":");
                if (parts[1].equals("executable")) {
                    List<String> commandArgs = new ArrayList<>();
                    List<String> argsList = Arrays.asList(this.args);
                    commandArgs.add(this.command);
                    commandArgs.addAll(argsList);

                    try {
                        Process process = new ProcessBuilder(commandArgs).start();
                        this.output = new String(process.getInputStream().readAllBytes());
                        this.error = new String(process.getErrorStream().readAllBytes());
                    } catch (IOException e) {
                        this.error = "Error executing command: " + e.getMessage();
                    }
                    break;
                }

                break;
        }
        return this.output;
    }
}