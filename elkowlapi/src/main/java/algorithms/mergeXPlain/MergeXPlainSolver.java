package algorithms.mergeXPlain;

import algorithms.ISolver;
import models.Explanation;
import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */
public class MergeXPlainSolver implements ISolver {

    private ILoader loader;
    private IReasonerManager reasonerManager;

    private OWLOntology base;
    private Literals literals;
    private MergeXPlainHelper mergeXPlainHelper;

    private Set<Explanation> explanations;


    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;

        initialize();
        startSolving();
    }

    @Override
    public Set<Explanation> getExplanations() {
        return explanations;
    }

    private void initialize() {
        mergeXPlainHelper = new MergeXPlainHelper();
        base = loader.getOntology();

        loader.getOntologyManager().addAxiom(base, loader.getNegObservation().getOwlAxiom());
        loader.initializeReasoner();

        IDataProcessing dataProcessing = new DataProcessing(loader);
        literals = dataProcessing.getLiterals();
    }

    private void startSolving() {
        Conflict conflict = getMergeConflict();
        explanations = conflict.getExplanations();
    }


    private Conflict getMergeConflict() {
        if (mergeXPlainHelper.isBaseNotConsistent(reasonerManager)) {
            return new Conflict();
        }

        if (mergeXPlainHelper.isBaseWithLiteralsConsistent(reasonerManager, literals)) {
            return new Conflict();
        }

        return findConflicts(base, literals);
    }

    private Conflict findConflicts(OWLOntology base, Literals literals) {
        if (mergeXPlainHelper.isBaseWithLiteralsConsistent(reasonerManager, literals)) {
            return new Conflict(literals, new HashSet<>());
        }

        if (literals.getOwlAxioms().size() == 1) {
            Set<Explanation> explanations = new HashSet<>();
            explanations.add(new Explanation(literals.getOwlAxioms()));
            return new Conflict(new Literals(), explanations);
        }

        List<Literals> sets = mergeXPlainHelper.divideIntoSets(literals);

        Conflict conflictC1 = findConflicts(base, sets.get(0));
        Conflict conflictC2 = findConflicts(base, sets.get(1));

        Set<Explanation> explanations = new HashSet<>();
        explanations.addAll(conflictC1.getExplanations());
        explanations.addAll(conflictC2.getExplanations());

        Literals conflictLiterals = new Literals();
        conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());
        conflictLiterals.getOwlAxioms().addAll(conflictC2.getLiterals().getOwlAxioms());

        while (!mergeXPlainHelper.isBaseWithLiteralsConsistent(reasonerManager, conflictLiterals)) {

            reasonerManager.addAxiomsToOntology(conflictC2.getLiterals().getOwlAxioms());
            Explanation X = getConflict(conflictC2.getLiterals().getOwlAxioms(), conflictC1.getLiterals());
            reasonerManager.removeAxiomsFromOntology(conflictC2.getLiterals().getOwlAxioms());

            reasonerManager.addAxiomsToOntology(X.getOwlAxioms());
            Explanation CS = getConflict(X.getOwlAxioms(), conflictC2.getLiterals());
            reasonerManager.removeAxiomsFromOntology(X.getOwlAxioms());

            CS.getOwlAxioms().addAll(X.getOwlAxioms());

            conflictLiterals.getOwlAxioms().removeAll(conflictC1.getLiterals().getOwlAxioms());
            conflictC1.getLiterals().getOwlAxioms().removeAll(X.getOwlAxioms());
            conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());

            explanations.add(CS);
        }

        return new Conflict(conflictLiterals, explanations);
    }


    private Explanation getConflict(Set<OWLAxiom> axioms, Literals literals) {
        if (!axioms.isEmpty() && mergeXPlainHelper.isBaseNotConsistent(reasonerManager)) {
            return new Explanation();
        }

        if (literals.getOwlAxioms().size() == 1) {
            return new Explanation(literals.getOwlAxioms());
        }

        List<Literals> sets = mergeXPlainHelper.divideIntoSets(literals);

        reasonerManager.addAxiomsToOntology(sets.get(0).getOwlAxioms());
        Explanation D2 = getConflict(sets.get(0).getOwlAxioms(), sets.get(1));
        reasonerManager.removeAxiomsFromOntology(sets.get(0).getOwlAxioms());

        reasonerManager.addAxiomsToOntology(D2.getOwlAxioms());
        Explanation D1 = getConflict(D2.getOwlAxioms(), sets.get(0));
        reasonerManager.removeAxiomsFromOntology(D2.getOwlAxioms());

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getOwlAxioms());
        conflicts.addAll(D2.getOwlAxioms());

        return new Explanation(conflicts);
    }


}
