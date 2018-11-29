package algorithms.abduction;

import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class Utils {

    protected static final Logger logger = Logger.getLogger(Utils.class.getSimpleName());
    protected final OWLDataFactory factory;
    private final AxiomConverter converter;

    /**
     * @param factory the factory to use
     */
    public Utils(OWLDataFactory factory) {
        this.factory = factory;
        converter = new AxiomConverter();
    }

    /**
     * @param axiom axiom to convert
     * @return converted class expression
     */
    public OWLClassExpression convert(OWLAxiom axiom) {
        converter.reset();
        axiom.accept(converter);
        OWLClassExpression result = converter.getResult();
        if (result == null) {
            throw new RuntimeException("Not supported yet");
        }
        return result;
    }

    private class AxiomConverter implements OWLAxiomVisitor {
        private OWLClassExpression result;

        public AxiomConverter() {
        }

        private OWLObjectIntersectionOf and(OWLClassExpression desc1,
                                            OWLClassExpression desc2) {
            return factory.getOWLObjectIntersectionOf(set(desc1, desc2));
        }

        private OWLObjectIntersectionOf and(Set<OWLClassExpression> set) {
            return factory.getOWLObjectIntersectionOf(set);
        }

        OWLClassExpression getResult() {
            return result;
        }

        private OWLObjectComplementOf not(OWLClassExpression desc) {
            return factory.getOWLObjectComplementOf(desc);
        }

        private OWLObjectOneOf oneOf(OWLIndividual ind) {
            return factory.getOWLObjectOneOf(Collections.singleton(ind));
        }

        private OWLObjectUnionOf or(OWLClassExpression desc1, OWLClassExpression desc2) {
            return factory.getOWLObjectUnionOf(set(desc1, desc2));
        }

        void reset() {
            result = null;
        }

        private <T> Set<T> set(T desc1, T desc2) {
            Set<T> set = new HashSet<T>();
            set.add(desc1);
            set.add(desc2);
            return set;
        }

        @Override
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLClassAssertionAxiom axiom) {
            OWLIndividual ind = axiom.getIndividual();
            OWLClassExpression c = axiom.getClassExpression();
            result = and(oneOf(ind), c);
        }

        @Override
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            OWLClassExpression sub = oneOf(axiom.getSubject());
            OWLClassExpression sup = factory.getOWLDataHasValue(axiom.getProperty(),
                    axiom.getObject());
            OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(sub, sup);
            ax.accept(this);
        }

        @Override
        public void visit(OWLDataPropertyDomainAxiom axiom) {
            OWLClassExpression sub = factory.getOWLDataSomeValuesFrom(
                    axiom.getProperty(), factory.getTopDatatype());
            result = and(sub, not(axiom.getDomain()));
        }

        @Override
        public void visit(OWLDataPropertyRangeAxiom axiom) {
            result = factory.getOWLDataSomeValuesFrom(axiom.getProperty(),
                    factory.getOWLDataComplementOf(axiom.getRange()));
        }

        @Override
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLDeclarationAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            Set<OWLClassExpression> nominals = new HashSet<OWLClassExpression>();
            for (OWLIndividual ind : axiom.getIndividuals()) {
                nominals.add(oneOf(ind));
            }
            result = factory.getOWLObjectIntersectionOf(nominals);
        }

        @Override
        public void visit(OWLDisjointClassesAxiom axiom) {
            result = and(axiom.getClassExpressions());
        }

        @Override
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLDisjointUnionAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLAnnotationAssertionAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            Iterator<OWLClassExpression> classes = axiom.getClassExpressions().iterator();
            OWLClassExpression c1 = classes.next();
            OWLClassExpression c2 = classes.next();
            if (classes.hasNext()) {
                logger.warning("EquivalentClassesAxiom with more than two elements not supported!");
            }
            // apply simplification for the cases where either concept is
            // owl:Thing or owlapi:Nothin
            if (c1.isOWLNothing()) {
                result = c2;
            } else if (c2.isOWLNothing()) {
                result = c1;
            } else if (c1.isOWLThing()) {
                result = not(c2);
            } else if (c2.isOWLThing()) {
                result = not(c1);
            } else {
                result = or(and(c1, not(c2)), and(not(c1), c2));
            }
        }

        @Override
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            OWLClassExpression sub = oneOf(axiom.getSubject());
            OWLClassExpression sup = factory.getOWLDataHasValue(axiom.getProperty(),
                    axiom.getObject());
            factory.getOWLSubClassOfAxiom(sub, not(sup)).accept(this);
        }

        @Override
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            OWLClassExpression sub = oneOf(axiom.getSubject());
            OWLClassExpression sup = factory.getOWLObjectHasValue(axiom.getProperty(),
                    axiom.getObject());
            factory.getOWLSubClassOfAxiom(sub, not(sup)).accept(this);
        }

        @Override
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            OWLClassExpression sub = oneOf(axiom.getSubject());
            OWLClassExpression sup = factory.getOWLObjectHasValue(axiom.getProperty(),
                    axiom.getObject());
            OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(sub, sup);
            ax.accept(this);
        }

        @Override
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            result = and(
                    factory.getOWLObjectSomeValuesFrom(axiom.getProperty(),
                            factory.getOWLThing()), not(axiom.getDomain()));
        }

        @Override
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            result = factory.getOWLObjectSomeValuesFrom(axiom.getProperty(),
                    not(axiom.getRange()));
        }

        @Override
        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLSameIndividualAxiom axiom) {
            Set<OWLClassExpression> nominals = new HashSet<OWLClassExpression>();
            for (OWLIndividual ind : axiom.getIndividuals()) {
                nominals.add(not(oneOf(ind)));
            }
            result = and(nominals);
        }

        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            OWLClassExpression sub = axiom.getSubClass();
            OWLClassExpression sup = axiom.getSuperClass();
            if (sup.isOWLNothing()) {
                result = sub;
            } else if (sub.isOWLThing()) {
                result = not(sup);
            } else {
                result = and(sub, not(sup));
            }
        }

        @Override
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(SWRLRule rule) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + rule);
        }

        @Override
        public void visit(OWLHasKeyAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }

        @Override
        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            throw new OWLRuntimeException(
                    "Not implemented: Cannot generate explanation for " + axiom);
        }
    }
}