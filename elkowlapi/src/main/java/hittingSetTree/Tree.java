package hittingSetTree;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.Set;

public class Tree {
    private Node root;

    public Tree(Set<OWLAxiom> data) {
        this.root = new ModelNode();
        ((ModelNode) this.root).data = data;
        this.root.children = new ArrayList<>();
    }

    public void addChild(Node parent, Node child) {
        parent.children.add(child);
    }

    public Node getRoot() {
        return root;
    }
}
