package algorithms.abduction.hittingSetTree;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;

public abstract class Node {

    public ArrayList<Node> children;
    public OWLAxiom label;
}
