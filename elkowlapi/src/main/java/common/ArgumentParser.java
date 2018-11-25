package common;

import application.Application;
import application.ExitCode;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import reasoner.ReasonerType;


public class ArgumentParser {

    @Option(name = "-f", aliases = "--file", usage = "Argument is required. Format: -f path.owl")
    private String fileName;

    @Option(name = "-o", aliases = "--observation", usage = "Argument is required. Format: -o a:C (a,b:R)")
    private String observation;

    @Option(name = "-r", aliases = "--reasoner", usage = "If not given, default is hermit. Format: -r hermit|pellet|jfact|elk")
    private ReasonerType reasonerType;

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

            if (reasonerType == null) {
                Configuration.REASONER = ReasonerType.HERMIT;
            } else {
                Configuration.REASONER = reasonerType;
            }

        } catch (CmdLineException exception) {
            exception.printStackTrace();
            Application.finish(ExitCode.ERROR);
        }
    }

}
