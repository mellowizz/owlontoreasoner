package test;

	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.util.Collections;
	import java.util.Set;

	import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
	import org.semanticweb.HermiT.Reasoner;
	import org.semanticweb.owlapi.apibinding.OWLManager;
	import org.semanticweb.owlapi.expression.OWLEntityChecker;
	import org.semanticweb.owlapi.expression.ParserException;
	import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
	import org.semanticweb.owlapi.io.StringDocumentSource;
	import org.semanticweb.owlapi.model.OWLClass;
	import org.semanticweb.owlapi.model.OWLClassExpression;
	import org.semanticweb.owlapi.model.OWLDataFactory;
	import org.semanticweb.owlapi.model.OWLEntity;
	import org.semanticweb.owlapi.model.OWLNamedIndividual;
	import org.semanticweb.owlapi.model.OWLOntology;
	import org.semanticweb.owlapi.model.OWLOntologyManager;
	import org.semanticweb.owlapi.reasoner.Node;
	import org.semanticweb.owlapi.reasoner.NodeSet;
	import org.semanticweb.owlapi.reasoner.OWLReasoner;
	import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
	import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
	import org.semanticweb.owlapi.util.ShortFormProvider;
	import org.semanticweb.owlapi.util.SimpleShortFormProvider;
