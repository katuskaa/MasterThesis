import algorithms.ISolver;
import algorithms.abduction.AbductionHSSolver;
import algorithms.mergeXPlain.MergeXPlainSolver;
import common.ArgumentParser;
import common.Configuration;
import fileLogger.FileLogger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import reasoner.ILoader;
import reasoner.IReasonerManager;
import reasoner.Loader;
import reasoner.ReasonerManager;
import timer.ThreadTimes;


public class Main {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        FileLogger.deleteLogs();

        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parse(args);

        ILoader loader = new Loader();
        loader.initialize(Configuration.REASONER);

        ThreadTimes threadTimes = new ThreadTimes(100);
        threadTimes.start();

        IReasonerManager reasonerManager = new ReasonerManager(loader);
        ISolver solver = createSolver(threadTimes);

        if (solver != null) {
            solver.solve(loader, reasonerManager);
        }

        threadTimes.interrupt();
    }

    private static ISolver createSolver(ThreadTimes threadTimes) {
        switch (Configuration.STRATEGY) {
            case ABDUCTIONHS:
                return new AbductionHSSolver(threadTimes);

            case MERGEXPLAIN:
                return new MergeXPlainSolver(threadTimes);
        }

        return null;
    }
}
