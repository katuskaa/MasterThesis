package algorithms.mergeXPlain;

import common.Loader;
import models.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts
 */
public class MergeXPlain {

    private Loader loader;
    private OWLOntology base;
    private Set<OWLAxiom> literals;

    public MergeXPlain(Loader loader) {
        this.loader = loader;
        initialize();
        start();
    }

    private void initialize() {
        base = loader.getOntology();
        loader.getOntologyManager().addAxiom(base, loader.getNegObservation().getOwlAxiom());
        loader.updateOntology(base);
        DataProcessing dataProcessing = new DataProcessing(loader);
        literals = dataProcessing.getLiterals();
    }

    private Set<Explanation> start() {
        if (!isBaseConsistent(base)) {
            return null;
        }

        if (isBaseWithLiteralsConsistent(base, literals)) {
            return null;
        }

        Conflict conflict = findConflicts(base, literals);

        return conflict.getExplanations();
    }

    private boolean isBaseConsistent(OWLOntology ontology) {
        loader.updateOntology(ontology);
        return loader.isOntologyConsistent();
    }

    private boolean isBaseWithLiteralsConsistent(OWLOntology ontology, Set<OWLAxiom> literals) {
        loader.getOntologyManager().addAxioms(ontology, literals);
        loader.updateOntology(ontology);
        boolean isConsistent = loader.isOntologyConsistent();
        loader.getOntologyManager().removeAxioms(ontology, literals);
        loader.updateOntology(ontology);
        return isConsistent;
    }

    private Conflict findConflicts(OWLOntology ontology, Set<OWLAxiom> literals) {
        if (isBaseWithLiteralsConsistent(ontology, literals)) {
            return new Conflict(literals, new HashSet<>());
        }

        if (literals.size() == 1) {
            Explanation explanation = new Explanation(literals.iterator().next());
            Set<Explanation> explanations = new HashSet<>();
            explanations.add(explanation);
            return new Conflict(new HashSet<>(), explanations);
        }

        int half = literals.size() / 2;
        int count = 0;
        Set<OWLAxiom> literalsFirstHalf = new HashSet<>();
        Set<OWLAxiom> literalsSecondHalf = new HashSet<>();

        for (OWLAxiom literal : literals) {
            if (count < half) {
                literalsFirstHalf.add(literal);
            } else {
                literalsSecondHalf.add(literal);
            }
            count++;
        }

        Conflict conflictFirstHalf = findConflicts(ontology, literalsFirstHalf);
        Conflict conflictSecondHalf = findConflicts(ontology, literalsSecondHalf);

        Set<Explanation> explanations = new HashSet<>();
        explanations.addAll(conflictFirstHalf.getExplanations());
        explanations.addAll(conflictSecondHalf.getExplanations());

        Set<OWLAxiom> conflictLiterals = new HashSet<>();
        conflictLiterals.addAll(conflictFirstHalf.getLiterals());
        conflictLiterals.addAll(conflictSecondHalf.getLiterals());

        while (!isBaseWithLiteralsConsistent(ontology, conflictLiterals)) {

            loader.getOntologyManager().addAxioms(ontology, conflictSecondHalf.getLiterals());
            loader.updateOntology(ontology);
            Set<OWLAxiom> X = getConflict(ontology, conflictSecondHalf.getLiterals(), conflictFirstHalf.getLiterals());
            loader.getOntologyManager().removeAxioms(ontology, conflictSecondHalf.getLiterals());
            loader.updateOntology(ontology);

            loader.getOntologyManager().addAxioms(ontology, X);
            loader.updateOntology(ontology);
            Set<OWLAxiom> CS = getConflict(ontology, X, conflictSecondHalf.getLiterals());
            CS.addAll(X);
            loader.getOntologyManager().removeAxioms(ontology, X);
            loader.updateOntology(ontology);

            conflictFirstHalf.getLiterals().removeAll(X);

            for (OWLAxiom owlAxiom : CS) {
                explanations.add(new Explanation(owlAxiom));
            }
        }

        Set<OWLAxiom> resultLiterals = new HashSet<>();
        resultLiterals.addAll(conflictFirstHalf.getLiterals());
        resultLiterals.addAll(conflictSecondHalf.getLiterals());

        return new Conflict(resultLiterals, explanations);
    }

    // B = B + C2'
    // D = C2'
    // C = C1'
    private Set<OWLAxiom> getConflict(OWLOntology ontology, Set<OWLAxiom> literalsSecond, Set<OWLAxiom> literalsFirst) {

        if (!literalsSecond.isEmpty() && !loader.isOntologyConsistent()) {
            return new HashSet<>();
        }

        if (literalsFirst.isEmpty()) {
            return new HashSet<>();
        }

        if (literalsFirst.size() == 1) {
            return literalsFirst;
        }

        int half = (literalsFirst.size() + 1) / 2;
        int count = 0;
        Set<OWLAxiom> literalsFirstHalf = new HashSet<>();
        Set<OWLAxiom> literalsSecondHalf = new HashSet<>();

        for (OWLAxiom literal : literalsFirst) {
            if (count < half) {
                literalsFirstHalf.add(literal);
            } else {
                literalsSecondHalf.add(literal);
            }
            count++;
        }

        loader.getOntologyManager().addAxioms(ontology, literalsFirstHalf);
        loader.updateOntology(ontology);
        Set<OWLAxiom> conflictSecond = getConflict(ontology, literalsFirstHalf, literalsSecondHalf);
        loader.getOntologyManager().removeAxioms(ontology, literalsFirstHalf);
        loader.updateOntology(ontology);

        loader.getOntologyManager().addAxioms(ontology, conflictSecond);
        loader.updateOntology(ontology);
        Set<OWLAxiom> conflictFirst = getConflict(ontology, conflictSecond, literalsFirstHalf);
        loader.getOntologyManager().removeAxioms(ontology, conflictSecond);
        loader.updateOntology(ontology);

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(conflictFirst);
        conflicts.addAll(conflictSecond);

        return conflicts;
    }


}
