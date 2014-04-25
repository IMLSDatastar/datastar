/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.PublicationHasAuthorValidator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ChildVClassesWithParent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaObjectPropetyOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaVClassOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.FoafNameToRdfsLabelPreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;

/**
 * Generates the edit configuration for a study design methods for investigation.
 * The n3 should look as follows (where subject = investigation and predicate = has study design execution):
 * ?subject ?predicate ?studyDesignExecution.
 * ?studyDesignExecution datastarCore:hasMethods ?methods.
 * ?methods datastarCore:methodStep ?methodStep .
 * ?methodStep datastarCore:order ?methodStepOrder;
 * 	datastarCore:value ?methodStepValue .
 */
public class AddStudyMethodsGenerator extends VivoBaseGenerator implements EditConfigurationGenerator {
	private Log log = LogFactory.getLog(AddStudyMethodsGenerator.class);	
	final static String datastarCore ="http://purl.org/datastar/";
	private int maxExistingOrder;
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
    	
    	
    	EditConfigurationVTwo config = new EditConfigurationVTwo();
    	initBasics(config, vreq);
        initPropertyParameters(vreq, session, config);

    	config.setVarNameForSubject("dataset");
        config.setVarNameForPredicate("predicate");   
        config.setVarNameForObject("studyDesignExecution");
        //this gets object uri if it exists and sets it
        this.setObjectUri(config, vreq);
    	config.setTemplate( "addStudyMethods.ftl" );
    	
    	//Set number of existing method steps
    	//if this is an editing situation
    	setupExistingMethodStepsCount(config, vreq);
    	
    	//add n3 required and optional
    	addN3(config);
    	//add new resources
    	addNewResources(config);
    	                    
    	
    	    	
    	//uris and literals on form
        this.setLiteralsAndUrisOnForm(config, vreq);
        setLiteralsAndUrisInScope(config, vreq);	
    	
    	//Set fields
    	setFields(config, vreq);
    	
    	//form specific data
        addFormSpecificData(config, vreq);        
       
        //validators
        addValidators(config);
       
       //no sparql queries to set but queries used to get existing values when uris in scope are set


        String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);       
        config.setFormUrl(formUrl);
        
        prepare(vreq, config);
    	return config;
    }
    
   
    
    //get the max order which will be utilized elsewhere
    private void setupExistingMethodStepsCount(EditConfigurationVTwo config, VitroRequest vreq) {
		
		//this is editing an existing study design execution and so we should get the method step count
		int maxOrder = getMaxOrder( EditConfigurationUtils.getObjectUri(vreq), 
                EditConfigurationUtils.getSubjectUri(vreq), vreq );
		this.maxExistingOrder = maxOrder;
		if(EditConfigurationUtils.getObjectUri(vreq) != null && this.maxExistingOrder == 0) {
			log.error("Zero method steps, this should not happen");
		} 
	}



	
	//Return ALL  method steps with value and order, ordered by order
	private String getSparqlForAllMethodStepsSparql() {
		String query = "PREFIX datastarCore: <" + datastarCore + "> \n"
		        + "SELECT ?studyDesignExecution ?methods ?methodStep ?methodStepOrder ?methodStepValue WHERE { \n"
		        + " ?dataset datastarCore:hasStudyDesignExecution ?studyDesignExecution. " 
				+ "?studyDesignExecution datastarCore:hasMethods ?methods. " 
				+ "?methods datastarCore:methodStep ?methodStep . " 
				+ "?methodStep datastarCore:value ?methodStepValue. " 
				+ "?methodStep datastarCore:order ?methodStepOrder. " 
		        + "} ORDER BY DESC(?order)";
		return query;	
	}

	private void addValidators(EditConfigurationVTwo config) {
		// TODO Auto-generated method stub
		
	}


	private void setFields(EditConfigurationVTwo config, VitroRequest vreq) {
		//this should be non-empty, todo: add validator
		config.addField(new FieldVTwo().setName("methodStep"));
		config.addField(new FieldVTwo().setName("methodStepValue").setValidators(list("nonempty")));
		config.addField(new FieldVTwo().setName("methodStepOrder"));
		if(this.maxExistingOrder > 1) {
			addExistingMethodStepFields(config);
		}
		
	}


	private void addExistingMethodStepFields(EditConfigurationVTwo config) {
		for(int i = 2; i <= this.maxExistingOrder; i++) {
			config.addField(new FieldVTwo().setName(this.getMethodStepVar(i)));
			config.addField(new FieldVTwo().setName(this.getMethodStepValueVar(i)));
			config.addField(new FieldVTwo().setName(this.getMethodStepOrderVar(i)));
		}
		
	}

	private void setLiteralsAndUrisOnForm(EditConfigurationVTwo config,
			VitroRequest vreq) {
		config.addLiteralsOnForm("methodStepValue", "methodStepOrder"); 
		//don't expect any uris on form
		config.addUrisOnForm("methodStep", "methods", "studyDesignExecution");
		if(this.maxExistingOrder > 1) {
			addLiteralsAndUrisForExistingMethodSteps(config);
		}

		
	}


	private void addLiteralsAndUrisForExistingMethodSteps(
			EditConfigurationVTwo config) {
		for(int i = 2; i <= this.maxExistingOrder; i++) {
			config.addLiteralsOnForm(this.getMethodStepValueVar(i), this.getMethodStepOrderVar(i));
			config.addUrisOnForm(this.getMethodStepVar(i));
		}
		
	}

	private void addNewResources(EditConfigurationVTwo config) {
		//if the object doesn't already exist, then a new one will be created
		config.addNewResource("methodStep", DEFAULT_NS_FOR_NEW_RESOURCE);
		config.addNewResource("methods", DEFAULT_NS_FOR_NEW_RESOURCE);
		config.addNewResource("studyDesignExecution", DEFAULT_NS_FOR_NEW_RESOURCE);
	}


	private void addN3(EditConfigurationVTwo config) {
		config.addN3Required(list(this.getStudyDesignExecN3(), 
				this.getMethodsN3(), 
				this.getMethodStepN3()));
		//If there is more than one method step, then set up n3 for the additional method steps
		if(this.maxExistingOrder > 1) {
			addN3ForExistingMethodSteps(config);
		}

	}
	
	private String getStudyDesignExecN3() {
		return this.getPrefixes() + 
				"?dataset ?predicate ?studyDesignExecution. ";
	}
	
	private String getMethodsN3() {
		return this.getPrefixes() + "?studyDesignExecution datastarCore:hasMethods ?methods. " + 
				"?methods rdf:type datastarCore:Methods .";
	}
	
	private String getMethodStepN3() {
		return this.getPrefixes() + "?methods datastarCore:methodStep ?methodStep . " + 
				"?methodStep rdf:type datastarCore:MethodStep ." + 
				"?methodStep datastarCore:order ?methodStepOrder; " + 
				" datastarCore:value ?methodStepValue . ";
	}


	

	private void addN3ForExistingMethodSteps(EditConfigurationVTwo config) {
		for(int i = 2; i <= this.maxExistingOrder; i++) {
			addMethodStepN3(config, i);
		}
		
	}

	private void addMethodStepN3(EditConfigurationVTwo config, int i) {
		//making additional method steps optional - easier in case of subsequent removal
		//require only one method step to be present for this form to add correctly
		config.addN3Optional(this.getPrefixes() + 
				"?methods datastarCore:methodStep "  + this.getMethodStepN3Var(i) + ". " + 
				this.getMethodStepN3Var(i) + " datastarCore:order " + this.getMethodStepOrderN3Var(i) + "; " + 
				" datastarCore:value " + this.getMethodStepValueN3Var(i) + " . ");
		
	}
	
	protected String getMethodStepN3Var(int counter) {
		return getN3VariableName(getMethodStepVar(counter));
	}
	
	protected String getMethodStepVar(int counter) {
		return (counter > 1)? "methodStep" + counter: "methodStep";
	}
	
	
	protected String getMethodStepOrderN3Var(int counter) {
		return getN3VariableName(getMethodStepOrderVar(counter));
	}
	
	protected String getMethodStepOrderVar(int counter) {
		return  (counter > 1)?"methodStepOrder" + counter: "methodStepOrder";
	}
	
	protected String getMethodStepValueN3Var(int counter) {
		return getN3VariableName(getMethodStepValueVar(counter));
	}
	
	protected String getMethodStepValueVar(int counter) {
		return  (counter > 1)? "methodStepValue" + counter: "methodStepValue";
	}

	protected String getN3VariableName(String varName) {
		return "?" + varName;
	}
	
	//Form specific data
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		
		formSpecificData.put("newMethodStepOrder", this.maxExistingOrder+ 1 );
		formSpecificData.put("methodStepsCounter", this.maxExistingOrder);
		editConfiguration.setFormSpecificData(formSpecificData);
	}

	

	
	 private void setLiteralsAndUrisInScope(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
	    	HashMap<String, List<String>> urisInScope = new HashMap<String, List<String>>();
	    	//note that at this point the subject, predicate, and object var parameters have already been processed
	    	urisInScope.put(editConfiguration.getVarNameForSubject(), 
	    			Arrays.asList(new String[]{editConfiguration.getSubjectUri()}));
	    	urisInScope.put(editConfiguration.getVarNameForPredicate(), 
	    			Arrays.asList(new String[]{editConfiguration.getPredicateUri()}));
	    	editConfiguration.setUrisInScope(urisInScope);
	    	//Uris in scope include subject, predicate, and object var
	    	
	    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
	    	
	    	//if some method steps exist
	    	if(this.maxExistingOrder > 0) {
	    		//set values for methodstep, methodstep order and methodstep value for more than one method step
	    		setLiteralsAndUrisInScopeForExistingMethodSteps(editConfiguration, vreq);
	    	}
	    }
	

	private void setLiteralsAndUrisInScopeForExistingMethodSteps(
			EditConfigurationVTwo config, VitroRequest vreq) {
		//Execute query and put uris and literals in scope
		String sparqlQuery = getSparqlForAllMethodStepsSparql();
		//Substitute subject, predicate - and object if it exists
		sparqlQuery = QueryUtils.subUriForQueryVar(sparqlQuery, "dataset", EditConfigurationUtils.getSubjectUri(vreq));
		String objectUri = config.getObject();
		//if(objectUri != null) {
		//	sparqlQuery = QueryUtils.subUriForQueryVar(sparqlQuery, "studyDesignExecution", objectUri);
		//}
		
		try {
            ResultSet results = QueryUtils.getQueryResults(sparqlQuery, vreq);
            String methodsUri = null;
            String studyDesignExecutionUri = null;
            if (results != null) {
            	while(results.hasNext()) { 
            		String methodStepUri = null;
            		Literal methodStepValueLiteral = null;
            		Literal methodStepOrderLiteral = null;
	                QuerySolution soln = results.next(); 
	                RDFNode methodStep = soln.get("methodStep");
	                RDFNode methodStepValue = soln.get("methodStepValue");
	                RDFNode methodStepOrder = soln.get("methodStepOrder");
	                if (methodStep != null && methodStep.isResource()) {
	                   methodStepUri = soln.getResource("methodStep").getURI();
	                }
	                if(methodStepValue != null && methodStepValue.isLiteral()) {
	                	methodStepValueLiteral = soln.getLiteral("methodStepValue");
	                }
	                if(methodStepOrder != null && methodStepOrder.isLiteral()) {
	                	methodStepOrderLiteral = soln.getLiteral("methodStepOrder");
	                }
	                int order = methodStepOrderLiteral.getInt();
	                config.addLiteralInScope(this.getMethodStepOrderVar(order), methodStepOrderLiteral);
	                config.addLiteralInScope(this.getMethodStepValueVar(order), methodStepValueLiteral);
	                config.addUrisInScope(this.getMethodStepVar(order), list(methodStepUri));
	                
	                //Check for methods uri and study design execution if not already returned
	                if(methodsUri == null) {
	                	RDFNode methods = soln.get("methods");
	                	if(methods != null && methods.isResource()) {
	                		methodsUri = soln.getResource("methods").getURI();
	    	                config.addUrisInScope("methods", list(methodsUri));
	                	}
	                }
	                
	                if(studyDesignExecutionUri == null && objectUri == null) {
	                	RDFNode studyDesignExecution = soln.get("studyDesignExecution");
	                	if(studyDesignExecution != null && studyDesignExecution.isResource()) {
	                		studyDesignExecutionUri = soln.getResource("studyDesignExecution").getURI();
	    	                config.addUrisInScope("studyDesignExecution", list(studyDesignExecutionUri));
	    	                //Study design execution also needs to be set as object uri if it hasn't already
	    	                config.setObject(studyDesignExecutionUri);
	                	}	
	                }

            	}
            }
        } catch (NumberFormatException e) {
            log.error("Invalid order returned", e);
        } catch (Exception e) {
            log.error(e, e);
        }
	}


	//query to get the max order of method steps
	 //Note: We are assuming that we won't have more than one "Methods" object per study design executiong
	 //If that changes, we will need to change this query and/or generator
	private String MAX_ORDER_QUERY = ""
		        + "PREFIX datastarCore: <" + datastarCore + "> \n"
		        + "SELECT DISTINCT ?order WHERE { \n"
		        + " ?dataset datastarCore:hasStudyDesignExecution ?studyDesignExecution. " 
				+ "?studyDesignExecution datastarCore:hasMethods ?methods. " 
				+ "?methods datastarCore:methodStep ?methodStep . " 
				+ "?methodStep datastarCore:order ?order. " 
		        + "} ORDER BY DESC(?order) LIMIT 1";
	 
	 private int getMaxOrder(String objectUri, String subjectUri, VitroRequest vreq) {

	        int maxOrder = 0; // default value 
	       // if (objectUri == null) { // adding new webpage   
	        //Not sure why add edit webpage was set up this way -but we need the existing greatest order
	            String queryStr = QueryUtils.subUriForQueryVar(MAX_ORDER_QUERY, "dataset", subjectUri);
	            if(objectUri != null) {
	            	queryStr = QueryUtils.subUriForQueryVar(queryStr, "studyDesignExecution", objectUri);
	            }
	            log.debug("Query string is: " + queryStr);
	            try {
	                ResultSet results = QueryUtils.getQueryResults(queryStr, vreq);
	                if (results != null && results.hasNext()) { // there is at most one result
	                    QuerySolution soln = results.next(); 
	                    RDFNode node = soln.get("order");
	                    if (node != null && node.isLiteral()) {
	                        // node.asLiteral().getInt() won't return an xsd:string that 
	                        // can be parsed as an int.
	                        int order = Integer.parseInt(node.asLiteral().getLexicalForm());
	                        if (order > maxOrder) {  
	                            log.debug("setting maxOrder to " + order);
	                            maxOrder = order;
	                        }
	                    }
	                }
	            } catch (NumberFormatException e) {
	                log.error("Invalid rank returned from query: not an integer value.");
	            } catch (Exception e) {
	                log.error(e, e);
	            }
	        //}
	        return maxOrder;
	    }
	
	 //This method checks if there is an object uri and then sets the object uri accordingly
	 public void setObjectUri(EditConfigurationVTwo config, VitroRequest vreq) {
		 String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
		 String query = this.getSubjectDesignExecQuery();
         query = QueryUtils.subUriForQueryVar(query, "dataset", subjectUri);

		 //Substitute subject uri
		 //Execute query - the idea here is there should only be one study design execution object here
		 //If this changes, then we should utilize the regular edit mode
         try {
             ResultSet results = QueryUtils.getQueryResults(query, vreq);
             if (results != null && results.hasNext()) { // there is at most one result
                 QuerySolution soln = results.next(); 
                 RDFNode node = soln.get("studyDesignExecution");
                 if(node != null && node.isResource()) {
                	 config.setObject(soln.getResource("studyDesignExecution").getURI());
                 }
             }
         } catch (NumberFormatException e) {
             log.error("Invalid rank returned from query: not an integer value.");
         } catch (Exception e) {
             log.error(e, e);
         }
	 }
	 
	 private String getSubjectDesignExecQuery() {
		 return  "PREFIX datastarCore: <" + datastarCore + "> \n"
			        + "SELECT ?studyDesignExecution WHERE { \n"
			        + " ?dataset datastarCore:hasStudyDesignExecution ?studyDesignExecution. "  
			        + " } LIMIT 1";
	 }
	 
	 
	static final String DEFAULT_NS_TOKEN=null; //null forces the default NS
	
	public String getPrefixes() {
		return "@prefix vivoCore:<" + vivoCore + "> ." + 
	"@prefix datastarCore:<http://purl.org/datastar/> . " + 
	"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + 
	"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .";
	}
	
}
