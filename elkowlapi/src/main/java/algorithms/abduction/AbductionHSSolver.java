package algorithms.abduction;

import algorithms.ISolver;
import common.Configuration;
import models.Explanation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.AxiomManager;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class AbductionHSSolver implements ISolver {

    private ILoader loader;
    private IReasonerManager reasonerManager;
    private List<Explanation> explanations;
    private List<OWLAxiom> assertionsAxioms;
    private List<OWLAxiom> negAssertionsAxioms;
    //private List<OWLAxiom> inconsistentCandidates;

    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;

        long startTime = System.currentTimeMillis();
        initialize();
        startSolving();

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        System.out.println("Time: " + elapsedTime);
    }

    @Override
    public List<Explanation> getExplanations() {
        return explanations;
    }

    @Override
    public boolean isShowingExplanations() {
        return true;
    }

    private void initialize() {
        assertionsAxioms = new ArrayList<>();
        negAssertionsAxioms = new ArrayList<>();

        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {

            List<OWLAxiom> classAssertionAxiom = AxiomManager.createClassAssertionAxiom(loader, axiom, true);
            List<OWLAxiom> objectPropertyAssertionAxiom = AxiomManager.createObjectPropertyAssertionAxiom(loader, axiom);

            for (int i = 0; i < classAssertionAxiom.size(); i++) {
                if (i % 2 == 0) {
                    assertionsAxioms.add(classAssertionAxiom.get(i));
                } else {
                    negAssertionsAxioms.add(classAssertionAxiom.get(i));
                }
            }

            for (int i = 0; i < objectPropertyAssertionAxiom.size(); i++) {
                if (i % 2 == 0) {
                    assertionsAxioms.add(objectPropertyAssertionAxiom.get(i));
                } else {
                    negAssertionsAxioms.add(objectPropertyAssertionAxiom.get(i));
                }
            }
        });
    }

    private void startSolving() {
        //inconsistentCandidates = new LinkedList<>();
        explanations = new LinkedList<>();
        ICheckRules checkRules = new CheckRules(loader, reasonerManager);
        int currentDepth = 0;

        ModelNode root = getNegModel(null);
        root.label = new LinkedList<>();
        root.depth = 0;

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.poll();

            if (ModelNode.class.isAssignableFrom(node.getClass())) {
                ModelNode model = (ModelNode) node;

                if (model.depth > currentDepth) {
                    showExplanationsWithDepth(model.depth);
                    currentDepth++;
                }

                if (model.depth.equals(Configuration.DEPTH)) {
                    break;
                }

                for (OWLAxiom child : model.data) {
                    Explanation explanation = new Explanation();

                    explanation.addAxioms(model.label);
                    explanation.addAxiom(child);

                    boolean isConsistent = checkRules.isConsistent(explanation);

                    if (isConsistent) {
                        boolean isExplanation = checkRules.isExplanation(explanation);

                        if (isExplanation) {
                            boolean isMinimal = checkRules.isMinimal(explanations, explanation);

                            if (isMinimal) {
                                explanation.setDepth(model.depth + 1);
                                explanations.add(explanation);
                            }

                        } else {
                            ModelNode modelNode = getNegModel(explanation);

                            modelNode.label = explanation.getOwlAxioms();
                            modelNode.depth = model.depth + 1;
                            queue.add(modelNode);
                        }
                    } else {
                        //inconsistentCandidates.addAll(explanation.getOwlAxioms());
                    }
                }
            }
        }

        showExplanationsWithDepth(currentDepth + 1);
    }

    private ModelNode getNegModel(Explanation explanation) {
        List<OWLAxiom> model = new LinkedList<>();

        model.add(loader.getNegObservation().getOwlAxiom());

        if (explanation != null) {
            model.addAll(explanation.getOwlAxioms());
        }

        reasonerManager.addAxiomsToOntology(model);

        for (int i = 0; i < assertionsAxioms.size(); i++) {
            OWLAxiom axiom = assertionsAxioms.get(i);
            OWLAxiom complementOfAxiom = negAssertionsAxioms.get(i);

            //&& !inconsistentCandidates.contains(axiom) && !inconsistentCandidates.contains(complementOfAxiom)
            if (!model.contains(axiom) && !model.contains(complementOfAxiom)) {
                reasonerManager.addAxiomToOntology(axiom);
                boolean isConsistent = reasonerManager.isOntologyConsistent();
                reasonerManager.removeAxiomFromOntology(axiom);

                reasonerManager.addAxiomToOntology(complementOfAxiom);
                boolean isComplementConsistent = reasonerManager.isOntologyConsistent();
                reasonerManager.removeAxiomFromOntology(complementOfAxiom);

                if (!isComplementConsistent && isConsistent) {
                    model.add(axiom);
                    reasonerManager.addAxiomToOntology(axiom);
                } else {
                    model.add(complementOfAxiom);
                    reasonerManager.addAxiomToOntology(complementOfAxiom);
                }
            }
        }

        //reasonerManager.resetOntology(model, true);
        reasonerManager.resetOntology(loader.getOriginalOntology().axioms());
        model.remove(loader.getNegObservation().getOwlAxiom());

        return getComplementOfModel(model);
    }

    private ModelNode getComplementOfModel(List<OWLAxiom> model) {
        ModelNode modelNode = new ModelNode();
        List<OWLAxiom> negModel = new LinkedList<>();

        for (OWLAxiom axiom : model) {
            OWLAxiom complement = AxiomManager.getComplementOfOWLAxiom(loader, axiom);
            negModel.add(complement);
        }

        modelNode.data = negModel;

        return modelNode;
    }

    private void showExplanationsWithDepth(Integer depth) {
        System.out.println("Explanations with depth: " + depth);
        for (Explanation explanation : explanations) {
            if (explanation.getDepth().equals(depth)) {
                System.out.println(explanation);
            }
        }
    }
}
