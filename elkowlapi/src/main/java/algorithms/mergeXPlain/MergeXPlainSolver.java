package algorithms.mergeXPlain;

import algorithms.ISolver;
import common.DLSyntax;
import common.Printer;
import models.Explanation;
import models.Literals;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import reasoner.AxiomManager;
import reasoner.ILoader;
import reasoner.IReasonerManager;
import timer.ThreadTimes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */
public class MergeXPlainSolver implements ISolver {

    private ILoader loader;
    private IReasonerManager reasonerManager;
    private OWLOntology ontology;

    private Literals literals;
    private List<Explanation> explanations;

    private ThreadTimes threadTimes;

    public MergeXPlainSolver(ThreadTimes threadTimes) {
        this.threadTimes = threadTimes;
    }

    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;
        this.ontology = this.loader.getOriginalOntology();

        initialize();
        startSolving();
        showExplanations();
    }

    private void initialize() {
        loader.getOntologyManager().addAxiom(ontology, loader.getNegObservation().getOwlAxiom());
        reasonerManager.addAxiomToOntology(loader.getNegObservation().getOwlAxiom());

        Set<OWLAxiom> allLiterals = new HashSet<>();

        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {
            List<OWLAxiom> classAssertionAxiom = AxiomManager.createClassAssertionAxiom(loader, axiom, false);
            allLiterals.addAll(classAssertionAxiom);
        });

        literals = new Literals(allLiterals);
    }

    private void startSolving() {
        Conflict conflict = getMergeConflict();
        explanations = conflict.getExplanations();
    }


    private Conflict getMergeConflict() {
        if (!reasonerManager.isOntologyConsistent()) {
            return new Conflict();
        }

        if (reasonerManager.isOntologyWithLiteralsConsistent(literals.getOwlAxioms(), ontology)) {
            return new Conflict();
        }

        return findConflicts(literals);
    }

    private Conflict findConflicts(Literals literals) {
        if (reasonerManager.isOntologyWithLiteralsConsistent(literals.getOwlAxioms(), ontology)) {
            return new Conflict(literals, new LinkedList<>());
        }

        if (literals.getOwlAxioms().size() == 1) {
            List<Explanation> explanations = new LinkedList<>();
            explanations.add(new Explanation(literals.getOwlAxioms(), literals.getOwlAxioms().size()));
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

        while (!reasonerManager.isOntologyWithLiteralsConsistent(conflictLiterals.getOwlAxioms(), ontology)) {

            reasonerManager.addAxiomsToOntology(conflictC2.getLiterals().getOwlAxioms());
            Explanation X = getConflict(conflictC2.getLiterals().getOwlAxioms(), conflictC1.getLiterals());
            reasonerManager.resetOntology(this.ontology.axioms());

            reasonerManager.addAxiomsToOntology(X.getOwlAxioms());
            Explanation CS = getConflict(X.getOwlAxioms(), conflictC2.getLiterals());
            reasonerManager.resetOntology(this.ontology.axioms());

            CS.getOwlAxioms().addAll(X.getOwlAxioms());

            conflictLiterals.getOwlAxioms().removeAll(conflictC1.getLiterals().getOwlAxioms());
            X.getOwlAxioms().stream().findFirst().ifPresent(axiom -> conflictC1.getLiterals().getOwlAxioms().remove(axiom));
            conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());

            if (explanations.contains(CS)) {
                break;
            }

            explanations.add(CS);
        }

        return new Conflict(conflictLiterals, explanations);
    }

    private Explanation getConflict(Collection<OWLAxiom> axioms, Literals literals) {
        if (!axioms.isEmpty() && !reasonerManager.isOntologyConsistent()) {
            return new Explanation();
        }

        if (literals.getOwlAxioms().size() == 1) {
            return new Explanation(literals.getOwlAxioms(), 1);
        }

        List<Literals> sets = divideIntoSets(literals);

        reasonerManager.addAxiomsToOntology(sets.get(0).getOwlAxioms());
        Explanation D2 = getConflict(sets.get(0).getOwlAxioms(), sets.get(1));
        reasonerManager.resetOntology(this.ontology.axioms());

        reasonerManager.addAxiomsToOntology(D2.getOwlAxioms());
        Explanation D1 = getConflict(D2.getOwlAxioms(), sets.get(0));
        reasonerManager.resetOntology(this.ontology.axioms());

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getOwlAxioms());
        conflicts.addAll(D2.getOwlAxioms());

        return new Explanation(conflicts, conflicts.size());
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
        loader.getOntologyManager().removeAxiom(ontology, loader.getNegObservation().getOwlAxiom());
        List<Explanation> filteredExplanations = new ArrayList<>();

        for (Explanation explanation : explanations) {
            if (isExplanation(explanation)) {
                if (reasonerManager.isOntologyWithLiteralsConsistent(explanation.getOwlAxioms(), ontology)) {
                    filteredExplanations.add(explanation);
                }
            }
        }

        return filteredExplanations;
    }

    private boolean isExplanation(Explanation explanation) {
        if (explanation.getOwlAxioms().size() == 1) {
            return true;
        }

        for (OWLAxiom axiom1 : explanation.getOwlAxioms()) {
            String name1 = getClassName(axiom1);

            boolean negated1 = containsNegation(name1);
            if (negated1) {
                name1 = name1.substring(1);
            }

            for (OWLAxiom axiom2 : explanation.getOwlAxioms()) {
                if (!axiom1.equals(axiom2)) {
                    String name2 = getClassName(axiom2);

                    boolean negated2 = containsNegation(name2);
                    if (negated2) {
                        name2 = name2.substring(1);
                    }

                    if (name1.equals(name2) && ((!negated1 && negated2) || (negated1 && !negated2))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private String getClassName(OWLAxiom axiom) {
        return Printer.print(axiom).split(DLSyntax.DELIMITER_ASSERTION)[1];
    }

    private boolean containsNegation(String name) {
        return name.contains(DLSyntax.DISPLAY_NEGATION);
    }

    private void showExplanations() {
        List<Explanation> filteredExplanations = filterExplanations();
        int depth = 1;
        while (filteredExplanations.size() > 0) {
            List<Explanation> currentExplanations = removeExplanationsWithDepth(filteredExplanations, depth);
            String currentExplanationsFormat = StringUtils.join(currentExplanations, ",");
            System.out.println(String.format("%d;%d;%.2f;{%s}", depth, currentExplanations.size(), threadTimes.getTotalUserTimeInSec(), currentExplanationsFormat));
            depth++;
        }
    }

    private List<Explanation> removeExplanationsWithDepth(List<Explanation> filteredExplanations, Integer depth) {
        List<Explanation> currentExplanations = filteredExplanations.stream().filter(explanation -> explanation.getDepth().equals(depth)).collect(Collectors.toList());
        filteredExplanations.removeAll(currentExplanations);
        return currentExplanations;
    }
}
