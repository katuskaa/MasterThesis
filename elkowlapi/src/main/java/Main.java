import algorithms.ISolver;
import algorithms.abduction.AbductionHSSolver;
import algorithms.mergeXPlain.MergeXPlainSolver;
import common.ArgumentParser;
import common.Configuration;
import models.Explanation;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import reasoner.ILoader;
import reasoner.IReasonerManager;
import reasoner.Loader;
import reasoner.ReasonerManager;

import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        //LogManager.getLogger("org.semanticweb.elk").setLevel(Level.ERROR);
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parse(args);

        ILoader loader = new Loader();
        loader.initialize(Configuration.REASONER);

        IReasonerManager reasonerManager = new ReasonerManager(loader);
        ISolver solver = createSolver();

        if (solver != null) {
            solver.solve(loader, reasonerManager);
            Collection<Explanation> explanations = solver.getExplanations();

            System.out.println("\nResultExplanation are:\n");

            for (Explanation explanation : explanations) {
                System.out.println(explanation);
            }
        }
    }

    private static ISolver createSolver() {
        switch (Configuration.METHOD) {
            case ABDUCTIONHS:
                return new AbductionHSSolver();

            case MERGEXPLAIN:
                return new MergeXPlainSolver();
        }

        return null;
    }
}
