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
            List<Writter> outputWritters = new ArrayList<>();
            List<Writter> errorWritters = new ArrayList<>();
            if (commander.output != "") {
                if (parser.outRedirect != "") {
                    outputWritters.add(new FWriter(commander.output, parser.outRedirect));
                } else if (parser.outAppend != "") {
                    outputWritters.add(new FAppender(commander.output, parser.outAppend));
                }
                if (outputWritters.size() == 0) {
                    outputWritters.add(new StdWriter(commander.output));
                }
            }
            if (commander.error != "") {
                if (parser.errRedirect != "") {
                    errorWritters.add(new FWriter(commander.error, parser.errRedirect));
                } else if (parser.errAppend != "") {
                    errorWritters.add(new FAppender(commander.error, parser.errAppend));
                }
                if (errorWritters.size() == 0) {
                    errorWritters.add(new ErrWriter(commander.error));
                }
            }

            for (Writter writter : outputWritters) {
                writter.write();
            }
            for (Writter writter : errorWritters) {
                writter.write();
            }
        }
    }
}
