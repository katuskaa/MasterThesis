package common;

import application.Application;
import application.ExitCode;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser {

    private Logger logger = Logger.getLogger(Parser.class.getName());
    private String[] args;

    public Parser(String[] args) {
        this.args = args;
        validateArguments();
        parseArguments();
    }

    private void validateArguments() {
        List<String> arguments = Arrays.asList(args);
        if (arguments.contains(Argument.HELP.getShortName()) || arguments.contains(Argument.HELP.getLongName())) {
            printHelpMessage();
        } else {
            boolean validArguments = true;
            for (Argument argument : Argument.values()) {
                if (argument.isMandatory()) {
                    if (!arguments.contains(argument.getShortName()) && !arguments.contains(argument.getLongName())) {
                        validArguments = false;
                        break;
                    }
                }
            }
            if (arguments.size() < Argument.getCountOfMandatory() * 2) {
                validArguments = false;
            }
            if (!validArguments) {
                logger.log(Level.WARNING, LogMessage.ERROR_MISSING_ARGUMENTS);
                Application.finish(ExitCode.ERROR);
            }

        }
    }

    private void parseArguments() {
        for (int i = 0; i < args.length; i++) {
            if (isArgValid(i + 1)) {
                switch (args[i]) {
                    case "-f":
                    case "--file": {
                        Configuration.INPUT_FILE = args[++i];
                        break;
                    }
                    case "-o":
                    case "--observation": {
                        Configuration.OBSERVATION = args[++i];
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else {
                Application.finish(ExitCode.ERROR);
            }
        }
    }

    private boolean isArgValid(int index) {
        return (index < args.length);
    }

    private void printHelpMessage() {
        logger.log(Level.INFO, LogMessage.INFO_HELP_MESSAGE);
        Application.finish(ExitCode.SUCCESS);
    }
}
