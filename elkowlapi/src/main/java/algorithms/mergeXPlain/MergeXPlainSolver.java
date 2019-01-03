package algorithms.mergeXPlain;

import algorithms.ISolver;
import common.Printer;
import models.Explanation;
import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.*;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */
public class MergeXPlainSolver implements ISolver {

    private ILoader loader;
    private IReasonerManager reasonerManager;

    private Literals literals;
    private List<Explanation> explanations;


    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;

        initialize();
        startSolving();
    }

    @Override
    public List<Explanation> getExplanations() {
        return filterExplanations();
    }

    private void initialize() {
        reasonerManager.addAxiomsToOntology(loader.getNegObservation().getOwlAxioms());

        //TODO vyhodit dataprocessing
        IDataProcessing dataProcessing = new DataProcessing(loader);
        literals = dataProcessing.getLiterals();
    }

    private void startSolving() {
        Conflict conflict = getMergeConflict();
        explanations = conflict.getExplanations();
    }


    private Conflict getMergeConflict() {
        if (!reasonerManager.isOntologyConsistent()) {
            return new Conflict();
        }

        if (reasonerManager.isOntologyWithLiteralsConsistent(literals)) {
            return new Conflict();
        }

        return findConflicts(literals);
    }

    private Conflict findConflicts(Literals literals) {
        if (reasonerManager.isOntologyWithLiteralsConsistent(literals)) {
            return new Conflict(literals, new LinkedList<>());
        }

        if (literals.getOwlAxioms().size() == 1) {
            List<Explanation> explanations = new LinkedList<>();
            explanations.add(new Explanation(literals.getOwlAxioms()));
            return new Conflict(new Literals(), explanations);
        }

        List<Literals> sets = divideIntoSets(literals);

        Conflict conflictC1 = findConflicts(sets.get(0));
        Conflict conflictC2 = findConflicts(sets.get(1));

        List<Explanation> explanations = new LinkedList<>();
        explanations.addAll(conflictC1.getExplanations());
        explanations.addAll(conflictC2.getExplanations());

        Literals conflictLiterals = new Literals();
        conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());
        conflictLiterals.getOwlAxioms().addAll(conflictC2.getLiterals().getOwlAxioms());

        while (!reasonerManager.isOntologyWithLiteralsConsistent(conflictLiterals)) {

            reasonerManager.addAxiomsToOntology(conflictC2.getLiterals().getOwlAxioms());
            Explanation X = getConflict(conflictC2.getLiterals().getOwlAxioms(), conflictC1.getLiterals());
            reasonerManager.removeAxiomsFromOntology(conflictC2.getLiterals().getOwlAxioms());

            reasonerManager.addAxiomsToOntology(X.getOwlAxioms());
            Explanation CS = getConflict(X.getOwlAxioms(), conflictC2.getLiterals());
            reasonerManager.removeAxiomsFromOntology(X.getOwlAxioms());

            CS.getOwlAxioms().addAll(X.getOwlAxioms());

            // Conflict temp = new Conflict(conflictC1);

            conflictLiterals.getOwlAxioms().removeAll(conflictC1.getLiterals().getOwlAxioms());
            conflictC1.getLiterals().getOwlAxioms().removeAll(X.getOwlAxioms());
            conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());

            explanations.add(CS);
        }

        return new Conflict(conflictLiterals, explanations);
    }


    private Explanation getConflict(Collection<OWLAxiom> axioms, Literals literals) {
        if (!axioms.isEmpty() && !reasonerManager.isOntologyConsistent()) {
            return new Explanation();
        }

        if (literals.getOwlAxioms().size() == 1) {
            return new Explanation(literals.getOwlAxioms());
        }

        List<Literals> sets = divideIntoSets(literals);

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


    private List<Literals> divideIntoSets(Literals literals) {
        List<Literals> dividedLiterals = new ArrayList<>();

        dividedLiterals.add(new Literals());
        dividedLiterals.add(new Literals());

        int count = 0;

        for (OWLAxiom owlAxiom : literals.getOwlAxioms()) {
            dividedLiterals.get(count % 2).getOwlAxioms().add(owlAxiom);
            count++;
        }

        return dividedLiterals;
    }

    private List<Explanation> filterExplanations() {
        List<Explanation> filteredExplanations = new LinkedList<>();

        for (Explanation explanation : explanations) {
            for (OWLAxiom axiom : explanation.getOwlAxioms()) {
                if (Printer.print(axiom).equals(Printer.print(loader.getNegObservation().getOwlAxioms().get(0)))) {
                    Explanation filter = new Explanation();
                    filter.getOwlAxioms().addAll(explanation.getOwlAxioms());
                    filter.getOwlAxioms().remove(axiom);

                    filteredExplanations.add(filter);
                    break;
                }

            }
        }


        return filteredExplanations;
    }

}
