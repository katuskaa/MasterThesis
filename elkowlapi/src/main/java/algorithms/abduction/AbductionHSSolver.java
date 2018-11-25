package algorithms.abduction;

import algorithms.ISolver;
import reasoner.ILoader;
import reasoner.IReasonerManager;


public class AbductionHSSolver implements ISolver {

    private ILoader loader;
    private IReasonerManager reasonerManager;

    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;
    }


}
