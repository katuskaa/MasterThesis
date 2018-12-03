package algorithms;

import models.Explanation;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.Collection;

public interface ISolver {

    void solve(ILoader loader, IReasonerManager reasonerManager);

    Collection<Explanation> getExplanations();
}
