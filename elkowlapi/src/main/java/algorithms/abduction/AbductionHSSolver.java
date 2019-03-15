package algorithms.abduction;

import algorithms.ISolver;
import common.Configuration;
import fileLogger.FileLogger;
import models.Explanation;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.AxiomManager;
import reasoner.ILoader;
import reasoner.IReasonerManager;
import timer.ThreadTimes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class AbductionHSSolver implements ISolver {

    private ILoader loader;
    private IReasonerManager reasonerManager;
    private List<Explanation> explanations;
    private List<OWLAxiom> assertionsAxioms;
    private List<OWLAxiom> negAssertionsAxioms;
    private ThreadTimes threadTimes;
    private long currentTimeMillis;

    public AbductionHSSolver(ThreadTimes threadTimes, long currentTimeMillis) {
        this.threadTimes = threadTimes;
        this.currentTimeMillis = currentTimeMillis;
    }

    @Override
    public void solve(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;

        initialize();
        startSolving();
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
        explanations = new LinkedList<>();
        ICheckRules checkRules = new CheckRules(loader, reasonerManager);
        Integer currentDepth = 0;

        ModelNode root = getNegModel(null);
        root.label = new LinkedList<>();
        root.depth = 0;

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.poll();

            if (Configuration.TIMEOUT != null && threadTimes.getTotalUserTimeInSec() > Configuration.TIMEOUT) {
                showExplanationsWithDepth(currentDepth + 1, true);
                currentDepth = null;
                break;
            }

            if (ModelNode.class.isAssignableFrom(node.getClass())) {
                ModelNode model = (ModelNode) node;

                if (model.depth > currentDepth) {
                    showExplanationsWithDepth(model.depth, false);
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
                                String line = String.format("%.2f;%s\n", threadTimes.getTotalUserTimeInSec(), explanation);
                                FileLogger.appendToFile(FileLogger.MHS_PARTIAL_EXPLANATIONS_LOG_FILE__PREFIX, currentTimeMillis, line);
                            }

                        } else {
                            ModelNode modelNode = getNegModel(explanation);

                            modelNode.label = explanation.getOwlAxioms();
                            modelNode.depth = model.depth + 1;
                            queue.add(modelNode);
                        }
                    }
                }
            }
        }

        if (currentDepth != null && currentDepth + 1 <= Configuration.DEPTH) {
            showExplanationsWithDepth(currentDepth + 1, false);
        }

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

    private void showExplanationsWithDepth(Integer depth, boolean timeout) {
        List<Explanation> currentExplanations = explanations.stream().filter(explanation -> explanation.getDepth().equals(depth)).collect(Collectors.toList());
        String currentExplanationsFormat = StringUtils.join(currentExplanations, ",");
        String line = String.format("%d;%d;%.2f%s;{%s}\n", depth, currentExplanations.size(), threadTimes.getTotalUserTimeInSec(), timeout ? "-TIMEOUT" : "", currentExplanationsFormat);
        FileLogger.appendToFile(FileLogger.MHS_LOG_FILE__PREFIX, currentTimeMillis, line);
        System.out.print(line);
    }
}
