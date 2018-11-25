package algorithms.mergeXPlain;

import algorithms.ISolver;
import models.Explanation;
import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */
public class MergeXPlainSolver implements ISolver {

    private Logger logger = Logger.getLogger(MergeXPlainSolver.class.getSimpleName());

    private ILoader loader;
    private IReasonerManager reasonerManager;

    private OWLOntology base;
    private Literals literals;
    private MergeXPlainHelper mergeXPlainHelper;


    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;

        initialize();
        startSolving();
    }

    private void initialize() {
        mergeXPlainHelper = new MergeXPlainHelper();
        base = loader.getOntology();

        loader.getOntologyManager().addAxiom(base, loader.getNegObservation().getOwlAxiom());
        reasonerManager.updateOntology(base);

        IDataProcessing dataProcessing = new DataProcessing(loader);
        literals = dataProcessing.getLiterals();
    }

    private void startSolving() {
        Conflict conflict = getExplanations();
        Set<Explanation> explanations = conflict.getExplanations();

        logger.log(Level.INFO, "Count of explanations is ".concat(String.valueOf(explanations.size())).concat("\n"));
        logger.log(Level.INFO, "Explanations:\n".concat(explanations.toString()).concat("\n"));
        logger.log(Level.INFO, "Literals:\n".concat(conflict.getLiterals().toString()).concat("\n"));
    }


    private Conflict getExplanations() {
        if (mergeXPlainHelper.isBaseNotConsistent(reasonerManager, base)) {
            return new Conflict();
        }

        if (mergeXPlainHelper.isBaseWithLiteralsConsistent(loader, reasonerManager, base, literals)) {
            return new Conflict();
        }

        return findConflicts(base, literals);
    }

    private Conflict findConflicts(OWLOntology base, Literals literals) {
        if (mergeXPlainHelper.isBaseWithLiteralsConsistent(loader, reasonerManager, base, literals)) {
            return new Conflict(literals, new HashSet<>());
        }

        if (literals.getOwlAxioms().size() == 1) {
            Set<Explanation> explanations = new HashSet<>();
            explanations.add(new Explanation(literals.getOwlAxioms()));
            return new Conflict(new Literals(), explanations);
        }

        Literals[] sets = mergeXPlainHelper.divideIntoSets(literals);
        Literals literals1 = sets[0];
        Literals literals2 = sets[1];

        Conflict conflictC1 = findConflicts(base, literals1);
        Conflict conflictC2 = findConflicts(base, literals2);

        Set<Explanation> explanations = new HashSet<>();
        explanations.addAll(conflictC1.getExplanations());
        explanations.addAll(conflictC2.getExplanations());

        Literals conflictLiterals = new Literals();
        conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());
        conflictLiterals.getOwlAxioms().addAll(conflictC2.getLiterals().getOwlAxioms());

        while (!mergeXPlainHelper.isBaseWithLiteralsConsistent(loader, reasonerManager, base, conflictLiterals)) {

            mergeXPlainHelper.addAxiomsToBase(loader, reasonerManager, base, conflictC2.getLiterals().getOwlAxioms());
            Explanation X = getConflict(base, conflictC2.getLiterals().getOwlAxioms(), conflictC1.getLiterals());
            mergeXPlainHelper.removeAxiomsFromBase(loader, reasonerManager, base, conflictC2.getLiterals().getOwlAxioms());

            mergeXPlainHelper.addAxiomsToBase(loader, reasonerManager, base, X.getOwlAxioms());
            Explanation CS = getConflict(base, X.getOwlAxioms(), conflictC2.getLiterals());
            mergeXPlainHelper.removeAxiomsFromBase(loader, reasonerManager, base, X.getOwlAxioms());

            CS.getOwlAxioms().addAll(X.getOwlAxioms());

            conflictLiterals.getOwlAxioms().removeAll(conflictC1.getLiterals().getOwlAxioms());
            conflictC1.getLiterals().getOwlAxioms().removeAll(X.getOwlAxioms());
            conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());

            explanations.add(CS);
        }

        return new Conflict(conflictLiterals, explanations);
    }


    private Explanation getConflict(OWLOntology base, Set<OWLAxiom> axioms, Literals literals) {
        if (!axioms.isEmpty() && mergeXPlainHelper.isBaseNotConsistent(reasonerManager, base)) {
            return new Explanation();
        }

        if (literals.getOwlAxioms().size() == 1) {
            return new Explanation(literals.getOwlAxioms());
        }

        Literals[] sets = mergeXPlainHelper.divideIntoSets(literals);
        Literals literals1 = sets[0];
        Literals literals2 = sets[1];

        mergeXPlainHelper.addAxiomsToBase(loader, reasonerManager, base, literals1.getOwlAxioms());
        Explanation D2 = getConflict(base, literals1.getOwlAxioms(), literals2);
        mergeXPlainHelper.removeAxiomsFromBase(loader, reasonerManager, base, literals1.getOwlAxioms());

        mergeXPlainHelper.addAxiomsToBase(loader, reasonerManager, base, D2.getOwlAxioms());
        Explanation D1 = getConflict(base, D2.getOwlAxioms(), literals1);
        mergeXPlainHelper.removeAxiomsFromBase(loader, reasonerManager, base, D2.getOwlAxioms());

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getOwlAxioms());
        conflicts.addAll(D2.getOwlAxioms());

        return new Explanation(conflicts);
    }


}
