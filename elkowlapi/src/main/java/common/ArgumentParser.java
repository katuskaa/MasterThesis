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

    @Option(name = "-o", aliases = "--observation", usage = "Argument is required. Format: -o a:C (a,b:R). For more complicated observation use manchester syntax.")
    private String observation;

    @Option(name = "-r", aliases = "--reasoner", usage = "If not given, default is hermit. Format: -r hermit|pellet|jfact")
    private ReasonerType reasonerType;

    @Option(name = "-s", aliases = "--strategy", usage = "If not given, default is abductionhs. Format: -m abductionhs|mergexplain")
    private Strategy strategy;

    @Option(name = "-h", aliases = "--help", usage = "-f (--f) and -o (--observation) are required arguments. For example: -f pathToFile -o a:C (a,b:R)")
    private Boolean help = false;

    @Option(name = "-d", aliases = "--depth", usage = "-d (--depth) if not given, whole tree is searched")
    private Integer depth;

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

            Configuration.OBSERVATION = observation;
            Configuration.INPUT_FILE = fileName;

            Configuration.DEPTH = (depth != null) ? depth : Integer.MAX_VALUE;

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
