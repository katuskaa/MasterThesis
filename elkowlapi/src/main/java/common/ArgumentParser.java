package common;

import application.Application;
import application.ExitCode;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class ArgumentParser {

    @Option(name = "-f", aliases = "--file", usage = "Argument is required")
    private String fileName;

    @Option(name = "-o", aliases = "--observation", usage = "Argument is required")
    private String observation;

    @Option(name = "-h", aliases = "--help", usage = "-f (--f) and -o (--observation) are required arguments. For example: -f pathToFile -o a:C (a,b:R)")
    private Boolean help = false;


    public void parse(String[] args) {
        CmdLineParser cmdLineParser = new CmdLineParser(this);

        try {
            cmdLineParser.parseArgument(args);

            if (help) {
                cmdLineParser.printUsage(System.err);
                Application.finish(ExitCode.HELP);
            }

            if (fileName == null || observation == null) {
                cmdLineParser.printUsage(System.err);
                Application.finish(ExitCode.ERROR);
            }

            Configuration.INPUT_FILE = fileName;
            Configuration.OBSERVATION = observation;

        } catch (CmdLineException exception) {
            exception.printStackTrace();
        }
    }

}
