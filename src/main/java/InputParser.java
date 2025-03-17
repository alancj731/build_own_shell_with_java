import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
// import java.util.Scanner;
// import java.io.File;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.Arrays;

public class InputParser {
    public String text;
    public String Command = "";
    public String argsStr = "";
    public String[] args = new String[0];
    public String outRedirect = "";
    public String outAppend = "";
    public String errRedirect = "";
    public String errAppend = "";
    public static char[] ESCAPE_CHARS = { '\"', '\\' };


    // Constructor
    public InputParser(String input) {
        this.text = input;
    }

    public InputParser() {
        this.text = "";
    }

    public void parseInput(String input){
        if (input != null) {
            this.text = input.trim();
            if (this.text.length() > 0) {
                this.parse();
            }
        } else {
            this.text = "";
        }
    }

    public void parse(){
        String[] cmdSeperated = this.seperateCommand();
        this.Command = cmdSeperated[0];
        String rest = cmdSeperated[1];

        String[] parts = new String[] { rest, "" };
        String regex = "(1>>|2>>|1>(?!>)|2>(?!>)|(?<!1|2|1>|2>)>)\\s*(\\S+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rest);

        while (matcher.find()) {
            String operator = matcher.group(1).trim();
            String value = matcher.group(2).trim();

            if (operator.equals("2>>")) {
                this.errAppend = value;
            } if (operator.equals("1>>")) {
                this.outAppend = value;;
            } else if (operator.equals("2>")) {
                this.errRedirect = value;
            } else if (operator.equals("1>")) {
                this.outRedirect = value;
            } else if (operator.equals(">")) {
                this.outRedirect = value;
            }
        }

        parts = rest.split("(2>|1>|>)", 2);

        this.argsStr =  formatString(parts[0].trim(), false)[0];
        this.args = formatString(parts[0].trim(), true);  
    }

    public String[] seperateCommand() {
        String command = "";
        String rest = "";
        if (this.text.length() == 0) {
            return new String[] { command, rest };
        }

        if (!(this.text.startsWith("\"")
                ||
                this.text.startsWith("\'"))) {
            return this.seperateBySpace(this.text);
        }

        return this.seperateByQotation(this.text);
    }

    private String [] seperateBySpace(String text) {
        String[] parts = text.split(" ", 2);
        String command = parts[0].trim();
        String rest = parts.length > 1 ? parts[1].trim() : "";
        return new String[] { command, rest };
    }

    private String[] seperateByQotation(String text) {
        if (text.length() < 2) {
            return new String[] { "", "" };
        }
        char startChar = text.charAt(0);
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) == startChar) {
                String command = formatString(text.substring(0, i+1), false)[0];
                String rest = text.substring(i + 1);
                return new String[] { command, rest };
            }
        }
        return new String[] { "", "" };
    }

    public static String addCurrentToProcess(String text, char c, String mode){
        return addCurrentToProcess(text, c, mode, false);
    }

    public static String addCurrentToProcess(String text, char c, String mode, boolean escape){
        if (escape) {
            return text + c;
        }
        if (mode == "") {
            if (c == ' ' && text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
                // make sure only one space is kept in the end of text
                return text;
            } else {
                return text + c;
            }
        }
        return text + c;
    }

    private static boolean needEscape(String text, int curIndex) {
        if (curIndex >= text.length() - 1) {
            // end of the string, impossible to escape
            return false;
        }
        char nextChar = text.charAt(curIndex + 1);
        for (char c : ESCAPE_CHARS) {
            if (c == nextChar) {
                return true;
            }
        }
        return false;
    }


    public static String[] formatString(String text, boolean inArray) {
        text = text.trim();
        StringBuilder toReturn = new StringBuilder();
        List<String> toReturnList = new ArrayList<String>();
        String toProcess = "";
        String mode = "";
        
        Consumer <String> addText = (String toAdd) -> {
            if (inArray) {
                toReturnList.add(toAdd);
            } else {
                toReturn.append(toAdd);
            }
        };

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean escape = false;

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
                            c = text.charAt(i);
                            escape = true; // need to escape in  "" mode
                            break;
                        default:
                            break;
                    }
                    if (escape){
                        // add next char to toProcess if escape
                        toProcess = addCurrentToProcess(toProcess, c, mode, true);
                        break;
                    }
                    if (c == '\'' || c == '\"') {
                        if (toProcess != "") {
                            addText.accept(toProcess);
                            toProcess = "";
                        }
                        break;
                    }

                    if (c != ' ' && (toProcess.length() > 0 && toProcess.charAt(toProcess.length() - 1) == ' ')) {
                        // seperating point, add toProcess to toReturn
                        addText.accept(toProcess);
                        // keep current char to toProcess
                        toProcess = c + "";
                        break;
                    }
                    // default case    
                    toProcess = addCurrentToProcess(toProcess, c, mode);
                    break;

                case "single":
                    switch (c) {
                        case '\'':
                            // end of single quote
                            if (toProcess != "") {
                                addText.accept(toProcess);
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
                                addText.accept(toProcess);
                                toProcess = "";
                            }
                            mode = "";
                            break;
                        }
                        case '\\': {
                            if (needEscape(text, i)) {
                                i += 1;
                                c = text.charAt(i);
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
            } // end of switch
        } // end of loop
        if (toProcess != "") {
            addText.accept(toProcess);
        }

        if (!inArray){
            return new String[] { toReturn.toString() };
        }

        toReturnList.replaceAll(String::trim);
        toReturnList.removeIf(String::isEmpty);
        return toReturnList.toArray(new String[0]);
    }
}
