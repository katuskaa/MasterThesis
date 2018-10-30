package algorithms.mergeXPlain;

import common.Loader;
import models.Explanation;
import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */
public class MergeXPlain {

    private Loader loader;
    private OWLOntology base;
    private Literals literals;
    private MergeXPlainHelper mergeXPlainHelper;

    public MergeXPlain(Loader loader) {
        this.loader = loader;
        initialize();

        Conflict conflict = getConflict();
        Set<Explanation> explanations = conflict.getExplanations();
        System.out.println(explanations.size());
        System.out.println(explanations);
        System.out.println(conflict.getLiterals());
    }

    private void initialize() {
        mergeXPlainHelper = new MergeXPlainHelper();
        base = loader.getOntology();

        loader.getOntologyManager().addAxiom(base, loader.getNegObservation().getOwlAxiom());
        loader.updateOntology(base);

        DataProcessing dataProcessing = new DataProcessing(loader);
        literals = dataProcessing.getLiterals();
    }

    private Conflict getConflict() {
        if (mergeXPlainHelper.isBaseNotConsistent(loader, base)) {
            return new Conflict();
        }

        if (mergeXPlainHelper.isBaseWithLiteralsConsistent(loader, base, literals)) {
            return new Conflict();
        }

        return findConflicts(base, literals);
    }

    private Conflict findConflicts(OWLOntology base, Literals literals) {
        if (mergeXPlainHelper.isBaseWithLiteralsConsistent(loader, base, literals)) {
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

        while (!mergeXPlainHelper.isBaseWithLiteralsConsistent(loader, base, conflictLiterals)) {

            mergeXPlainHelper.addAxiomsToBase(loader, base, conflictC2.getLiterals().getOwlAxioms());
            Explanation X = getConflict(base, conflictC2.getLiterals().getOwlAxioms(), conflictC1.getLiterals());
            mergeXPlainHelper.removeAxiomsFromBase(loader, base, conflictC2.getLiterals().getOwlAxioms());

            mergeXPlainHelper.addAxiomsToBase(loader, base, X.getOwlAxioms());
            Explanation CS = getConflict(base, X.getOwlAxioms(), conflictC2.getLiterals());
            mergeXPlainHelper.removeAxiomsFromBase(loader, base, X.getOwlAxioms());

            //TODO po zakomentovani sa neda urcit, co je spravne vysvetlenie
            CS.getOwlAxioms().addAll(X.getOwlAxioms());

            conflictLiterals.getOwlAxioms().removeAll(conflictC1.getLiterals().getOwlAxioms());
            conflictC1.getLiterals().getOwlAxioms().removeAll(X.getOwlAxioms());
            conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());

            explanations.add(CS);
        }

        return new Conflict(conflictLiterals, explanations);
    }


    private Explanation getConflict(OWLOntology base, Set<OWLAxiom> axioms, Literals literals) {

        if (!axioms.isEmpty() && mergeXPlainHelper.isBaseNotConsistent(loader, base)) {
            return new Explanation(new HashSet<>());
        }

        if (literals.getOwlAxioms().size() == 1) {
            return new Explanation(literals.getOwlAxioms());
        }

        Literals[] sets = mergeXPlainHelper.divideIntoSets(literals);
        Literals literals1 = sets[0];
        Literals literals2 = sets[1];

        mergeXPlainHelper.addAxiomsToBase(loader, base, literals1.getOwlAxioms());
        Explanation D2 = getConflict(base, literals1.getOwlAxioms(), literals2);
        mergeXPlainHelper.removeAxiomsFromBase(loader, base, literals1.getOwlAxioms());

        mergeXPlainHelper.addAxiomsToBase(loader, base, D2.getOwlAxioms());
        Explanation D1 = getConflict(base, D2.getOwlAxioms(), literals1);
        mergeXPlainHelper.removeAxiomsFromBase(loader, base, D2.getOwlAxioms());

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getOwlAxioms());
        conflicts.addAll(D2.getOwlAxioms());

        return new Explanation(conflicts);
    }


}
