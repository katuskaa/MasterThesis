package algorithms.abduction;

import models.Explanation;

public interface ICheckRules {

    boolean isConsistent(Explanation explanation);

    boolean isRelevant(Explanation explanation);

    boolean isInconsistent(Explanation explanation);
}
