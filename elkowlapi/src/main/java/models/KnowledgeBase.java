package models;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public class KnowledgeBase {

    private Set<OWLAxiom> owlAxioms;

    public KnowledgeBase(Set<OWLAxiom> owlAxioms) {
        this.owlAxioms = owlAxioms;
    }

    public Set<OWLAxiom> getOwlAxioms() {
        return owlAxioms;
    }

    public void setOwlAxioms(Set<OWLAxiom> owlAxioms) {
        this.owlAxioms = owlAxioms;
    }
}
