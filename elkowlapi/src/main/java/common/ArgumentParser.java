package common;

import application.Application;
import application.ExitCode;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import reasoner.ReasonerType;
import reasoner.Strategy;


public class ArgumentParser {

    @Option(name = "-f", aliases = "--file", usage = "Argument is required. Format: -f path.owl")
    private String fileName;

    @Option(name = "-o", aliases = "--observation", usage = "Argument is required. Format: -o a:C (a,b:R). If you want multi observation as delimiter use \";\".")
    private String observation;

    @Option(name = "-m", aliases = "--multiObservation", usage = "If not given, simple observation is considered.")
    private Boolean multiObservation = false;

    @Option(name = "-r", aliases = "--reasoner", usage = "If not given, default is hermit. Format: -r hermit|pellet|jfact|elk")
    private ReasonerType reasonerType;

    @Option(name = "-s", aliases = "--strategy", usage = "If not given, default is abductionhs. Format: -m abductionhs|mergexplain")
    private Strategy strategy;

    @Option(name = "-h", aliases = "--help", usage = "-f (--f) and -o (--observation) are required arguments. For example: -f pathToFile -o a:C (a,b:R)")
    private Boolean help = false;


    public void parse(String[] args) {
        CmdLineParser cmdLineParser = new CmdLineParser(this);

        try {
            cmdLineParser.parseArgument(args);
            cmdLineParser.setUsageWidth(300);

            if (help) {
                cmdLineParser.printUsage(System.err);
                Application.finish(ExitCode.HELP);
            }

            if (fileName == null || observation == null) {
                cmdLineParser.printUsage(System.err);
                Application.finish(ExitCode.ERROR);
            }

            if ((!multiObservation && observation.contains(Configuration.DELIMITER_OBSERVATION))
                    || (multiObservation && !observation.contains(Configuration.DELIMITER_OBSERVATION))) {
                cmdLineParser.printUsage(System.err);
                Application.finish(ExitCode.ERROR);
            }

            Configuration.MULTI_OBSERVATION = multiObservation;
            Configuration.OBSERVATION = observation;
            Configuration.INPUT_FILE = fileName;

            if (reasonerType == null) {
                Configuration.REASONER = ReasonerType.HERMIT;
            } else {
                Configuration.REASONER = reasonerType;
            }

            if (strategy == null) {
                Configuration.STRATEGY = Strategy.ABDUCTIONHS;
            } else {
                Configuration.STRATEGY = strategy;
            }

        } catch (CmdLineException exception) {
            exception.printStackTrace();
            Application.finish(ExitCode.ERROR);
        }
    }

}
