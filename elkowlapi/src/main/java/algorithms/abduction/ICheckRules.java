package algorithms.abduction;

import models.Explanation;

import java.util.List;

public interface ICheckRules {

    boolean isConsistent(Explanation explanation);

    boolean isRelevant(Explanation explanation);

    boolean isInconsistent(Explanation explanation);

    boolean isMinimal(List<Explanation> explanationList, Explanation explanation);
}
