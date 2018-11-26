import algorithms.ISolver;
import algorithms.abduction.AbductionHSSolver;
import common.ArgumentParser;
import common.Configuration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import reasoner.ILoader;
import reasoner.IReasonerManager;
import reasoner.Loader;
import reasoner.ReasonerManager;

public class Main {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parse(args);

        ILoader loader = new Loader();
        loader.initialize(Configuration.REASONER);

        IReasonerManager reasonerManager = new ReasonerManager(loader);

//        ISolver mergeXPlainSolver = new MergeXPlainSolver();
//        mergeXPlainSolver.solve(loader, reasonerManager);

        ISolver abductionHSSolver = new AbductionHSSolver();
        abductionHSSolver.solve(loader, reasonerManager);
    }
}
