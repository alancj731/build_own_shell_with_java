import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
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
    public Parser(String input) {
        this.text = input;
    }

    public Parser() {
        this.text = "";
    }

    public parseInput(String input){
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

        String startChar = this.text.substring(0, 1);

        return new String[] { command, rest };

    }

    private seperateBySpace(String text) {
        String[] parts = text.split(" ");
        command = parts[0].trim();
        rest = parts.length > 1 ? parts[1].trim() : "";
        return new String[] { command, rest };
    }

    private seperateByQotation(String text) {
        if (text.length() < 2) {
            return new String[] { "", "" };
        }
        char startChar = text.charAt(0);
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) == startChar) {
                command = formatString(text.substring(0, i+1), false)[0];
                rest = text.substring(i + 1);
                return new String[] { command, rest };
            }
        }
    }

    public static String addCurrentToProcess(String text, Char c, String mode){
        return addCurrentToProcess(text, c, mode, false);
    }

    public static String addCurrentToProcess(String text, Char c, String mode, boolean escape){
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
        
        Consumer<String> addText = (String toAdd) -> {
            if (inArray) {
                toReturnList.add(toAdd);
            } else {
                toReturn.append(toAdd);
            }
        };

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            escape = false;

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
                        // enter single or double quote mode, no need to add c to toProcess
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
