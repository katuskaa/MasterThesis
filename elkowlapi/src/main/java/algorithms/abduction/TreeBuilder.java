package algorithms.abduction;

import algorithms.hittingSetTree.Node;
import algorithms.hittingSetTree.Tree;
import models.KnowledgeBase;
import models.Observation;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.LinkedHashSet;
import java.util.Set;

public class TreeBuilder {

    private KnowledgeBase knowledgeBase;
    private Observation negObservation;

    public TreeBuilder(KnowledgeBase knowledgeBase, Observation negObservation) {
        this.knowledgeBase = knowledgeBase;
        this.negObservation = negObservation;
    }

    public void build() {
        Set<OWLAxiom> data = new LinkedHashSet<>(knowledgeBase.getOwlAxioms());
        data.add(negObservation.getOwlAxiom());
        Tree tree = new Tree(data);
        for (OWLAxiom owlAxiom : data) {
            Node node = new Node();
            node.edge = owlAxiom;
            tree.addChild(tree.getRoot(), node);
        }
    }

}
