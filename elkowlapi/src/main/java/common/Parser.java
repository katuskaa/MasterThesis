package common;

public class Parser {

    private String[] args;

    public Parser(String[] args) {
        this.args = args;
        parseArguments();
    }

    private void parseArguments() {
        for (int i = 0; i < args.length; i++) {
            if (isArgValid(i + 1)) {
                switch (args[i]) {
                    case "-o":
                    case "--observation": {
                        Configuration.OBSERVATION = args[++i];
                        break;
                    }
                    case "-f":
                    case "--file": {
                        Configuration.INPUT_FILE = args[++i];
                        break;
                    }
                    case "-h":
                    case "--help": {
                        printHelpMessage();
                    }
                    default: {
                        break;
                    }
                }
            }
        }
    }

    private boolean isArgValid(int index) {
        return (index < args.length);
    }

    private void printHelpMessage() {
        String helpMessage = "";
        System.out.print(helpMessage);
        System.exit(0);
    }
}
