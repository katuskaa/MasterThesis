package algorithms.abduction;

import algorithms.ISolver;
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

    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;

        initialize();
        startSolving();
    }

    @Override
    public List<Explanation> getExplanations() {
        return explanations;
    }

    private void initialize() {
        assertionsAxioms = new ArrayList<>();
        negAssertionsAxioms = new ArrayList<>();

        //TODO add object property
        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {
            List<OWLAxiom> assertionAxiom = AxiomManager.createClassAssertionAxiom(loader, axiom);

            if (assertionAxiom != null && assertionAxiom.size() == 2) {
                assertionsAxioms.add(assertionAxiom.get(0));
                negAssertionsAxioms.add(assertionAxiom.get(1));
            }
        });
    }

    // TODO add optimisations
    private void startSolving() {
        explanations = new LinkedList<>();
        ICheckRules checkRules = new CheckRules(loader, reasonerManager);

        ModelNode root = getNegModel(null);
        root.label = new LinkedList<>();

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.poll();

            if (ModelNode.class.isAssignableFrom(node.getClass())) {
                ModelNode model = (ModelNode) node;

                for (OWLAxiom child : model.data) {
                    Explanation explanation = new Explanation();

                    explanation.addAxioms(model.label);
                    explanation.addAxiom(child);

                    boolean isConsistent = checkRules.isConsistent(explanation);
                    boolean isRelevant = checkRules.isRelevant(explanation);

                    if (isConsistent && isRelevant) {
                        boolean isExplanation = checkRules.isExplanation(explanation);

                        if (isExplanation) {
                            boolean isMinimal = checkRules.isMinimal(explanations, explanation);

                            if (isMinimal) {
                                explanations.add(explanation);
                            }

                        } else {
                            ModelNode modelNode = getNegModel(explanation);

                            modelNode.label = explanation.getOwlAxioms();
                            queue.add(modelNode);
                        }
                    }
                }
            }
        }
    }

    //TODO consider object property
    private ModelNode getNegModel(Explanation explanation) {
        List<OWLAxiom> model = new LinkedList<>(loader.getNegObservation().getOwlAxioms());

        if (explanation != null) {
            model.addAll(explanation.getOwlAxioms());
        }

        reasonerManager.addAxiomsToOntology(model);

        for (int i = 0; i < assertionsAxioms.size(); i++) {
            OWLAxiom axiom = assertionsAxioms.get(i);
            OWLAxiom complementOfAxiom = negAssertionsAxioms.get(i);

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

        reasonerManager.removeAxiomsFromOntology(model);
        model.removeAll(loader.getNegObservation().getOwlAxioms());

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

}
