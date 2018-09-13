package models;

import org.semanticweb.owlapi.model.OWLAxiom;

public class Explanation {

    private OWLAxiom owlAxiom;

    public Explanation(OWLAxiom owlAxiom) {
        this.owlAxiom = owlAxiom;
    }

    public OWLAxiom getOwlAxiom() {
        return owlAxiom;
    }

    public void setOwlAxiom(OWLAxiom owlAxiom) {
        this.owlAxiom = owlAxiom;
    }
}
