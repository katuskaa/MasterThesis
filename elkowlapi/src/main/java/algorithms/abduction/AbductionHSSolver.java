package algorithms.abduction;

import algorithms.ISolver;
import algorithms.abduction.hittingSetTree.CheckNode;
import algorithms.abduction.hittingSetTree.CrossNode;
import algorithms.abduction.hittingSetTree.ModelNode;
import algorithms.abduction.hittingSetTree.Node;
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
    private Set<OWLAxiom> assertionsAxioms;

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
        assertionsAxioms = new HashSet<>();

        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {
            List<OWLAxiom> assertionAxiom = AxiomManager.createClassAssertionAxiom(loader, axiom);

            if (assertionAxiom != null && assertionAxiom.size() == 2) {
                assertionsAxioms.add(assertionAxiom.get(0));
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
                    boolean isExplanation = checkRules.isExplanation(explanation);

                    if (!isConsistent || !isRelevant) {
                        CrossNode crossNode = new CrossNode();

                        crossNode.label = explanation.getOwlAxioms();
                        queue.add(crossNode);

                    } else if (isExplanation) {
                        CheckNode checkNode = new CheckNode();

                        checkNode.explanation = explanation;
                        checkNode.label = explanation.getOwlAxioms();

                        queue.add(checkNode);

                    } else {
                        ModelNode modelNode = getNegModel(explanation);

                        modelNode.label = explanation.getOwlAxioms();
                        queue.add(modelNode);
                    }
                }

            } else if (CheckNode.class.isAssignableFrom(node.getClass())) {
                explanations.add(((CheckNode) node).explanation);
            }
        }
    }

    private ModelNode getNegModel(Explanation explanation) {
        ModelNode modelNode = new ModelNode();
        Set<OWLAxiom> model = new HashSet<>();

        model.add(loader.getObservation().getOwlAxiom());

        if (explanation != null) {
            for (OWLAxiom owlAxiom : explanation.getOwlAxioms()) {
                OWLAxiom complementOfAxiom = AxiomManager.getComplementOfOWLAxiom(loader, owlAxiom);

                model.add(complementOfAxiom);
            }
        }

        for (OWLAxiom axiom : assertionsAxioms) {
            OWLAxiom complementOfAxiom = AxiomManager.getComplementOfOWLAxiom(loader, axiom);

            if (!model.contains(axiom) && !model.contains(complementOfAxiom)) {
                model.add(axiom);
            }
        }

        modelNode.data = model;

        return modelNode;
    }

//    private OWLClassExpression convertOWLAxiomToOWLClassExpression(OWLAxiom owlAxiom) {
//        Utils utils = new Utils(loader.getOntologyManager().getOWLDataFactory());
//
//        return utils.convert(owlAxiom);
//    }

}
