package algorithms.abduction;

import algorithms.ISolver;
import models.Explanation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.AxiomManager;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.*;


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

        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {
            List<OWLAxiom> assertionAxiom = AxiomManager.createClassAssertionAxiom(loader, axiom);

            if (assertionAxiom != null && assertionAxiom.size() == 2) {
                assertionsAxioms.add(assertionAxiom.get(0));
                negAssertionsAxioms.add(assertionAxiom.get(1));
            }
        });
    }

    // TODO este doplnit optimalizacie
    private void startSolving() {
        explanations = new LinkedList<>();
        ICheckRules checkRules = new CheckRules(loader, reasonerManager);

        ModelNode root = getNegModel(null);
        root.label = new HashSet<>();

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
                        boolean isInconsistent = checkRules.isInconsistent(explanation);

                        if (isInconsistent) {
                            explanations.add(explanation);

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

    private ModelNode getNegModel(Explanation explanation) {
        ModelNode modelNode = new ModelNode();
        Set<OWLAxiom> model = new HashSet<>();

        model.add(loader.getNegObservation().getOwlAxiom());
        reasonerManager.addAxiomToOntology(loader.getNegObservation().getOwlAxiom());

        if (explanation != null) {
            model.addAll(explanation.getOwlAxioms());
            reasonerManager.addAxiomsToOntology(explanation.getOwlAxioms());
        }

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


                if (!isConsistent && isComplementConsistent) {
                    model.add(complementOfAxiom);
                    reasonerManager.addAxiomToOntology(complementOfAxiom);
                } else if (!isComplementConsistent && isConsistent) {
                    model.add(axiom);
                    reasonerManager.addAxiomToOntology(axiom);
                } else {
                    model.add(complementOfAxiom);
                    reasonerManager.addAxiomToOntology(complementOfAxiom);
                }
            }
        }

        reasonerManager.removeAxiomsFromOntology(model);
        model.remove(loader.getNegObservation().getOwlAxiom());

        Set<OWLAxiom> negModel = new HashSet<>();

        for (OWLAxiom axiom : model) {
            OWLAxiom complement = AxiomManager.getComplementOfOWLAxiom(loader, axiom);

            negModel.add(complement);
        }

        modelNode.data = negModel;

        return modelNode;
    }

}
