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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
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
 * Generates the edit configuration for a default property form.
 * ModelChangePreprocessor creates the rdfs:label statement. 
 */
public class DatasetProductOfGenerator extends VivoBaseGenerator implements EditConfigurationGenerator {
	private Log log = LogFactory.getLog(DatasetProductOfGenerator.class);	
	final static String datastarCore ="http://purl.org/datastar/";

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
    	
    	
    	EditConfigurationVTwo config = new EditConfigurationVTwo();
    	initBasics(config, vreq);
    	initPropertyParameters(vreq, session, config);
    	config.setVarNameForSubject("dataset");
    	config.setVarNameForPredicate("predicate");
    	//we don't use this on the form, but we do employ this to get the label of the object in case of an edit
    	config.setVarNameForObject("objectURI");
    	config.setTemplate( "datasetProductOf.ftl" );
    	
    	
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
       
        //sparql queries for existing investigation/project if this is an editing function
    	setSparqlQueries(config, vreq);


        String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);       
        config.setFormUrl(formUrl);
        
        prepare(vreq, config);
    	return config;
    }
    
    //sparql queries for existing investigation or project in case this is an edit

	private void setSparqlQueries(EditConfigurationVTwo config,
			VitroRequest vreq) {
		config.addSparqlForExistingUris("objectType", getExistingObjectTypeSparql());
		config.addSparqlForExistingLiteral("objectLabel", getExistingObjectLabelSparql());
		config.addSparqlForExistingLiteral("objectDescription", getExistingObjectDescriptionSparql());
		config.addSparqlForExistingLiteral("projectDescription", getExistingProjectDescriptionSparql());

	}


	private String getExistingProjectDescriptionSparql() {
		String query = "SELECT ?existingProjectDescription WHERE {?objectURI <" + vivoCore + "description> ?existingProjectDescription .}";
		return query;
	}

	private String getExistingObjectDescriptionSparql() {
		String query = "SELECT ?existingObjectDescription WHERE {?objectURI <" + datastarCore + "description> ?existingObjectDescription .}";
		return query;
	}

	private String getExistingObjectTypeSparql() {
		//todo: add rdf:type
		String query = "PREFIX rdfs: <" + RDFS.getURI() + "> \n" + 
				"SELECT ?existingObjectType WHERE {?objectURI <" +  VitroVocabulary.RDF_TYPE  + "> ?existingObjectType . " + 
				"FILTER (?existingObjectType = <" + vivoCore + "Project> || ?existingObjectType = <" + datastarCore + "Investigation>)}";
				return query;
	}

	private String getExistingObjectLabelSparql() {
		String query = "PREFIX rdfs: <" + RDFS.getURI() + "> \n" + 
		"SELECT ?existingObjectLabel WHERE {?objectURI rdfs:label ?existingObjectLabel .}";
		return query;
	}

	

	private void addValidators(EditConfigurationVTwo config) {
		// TODO Auto-generated method stub
		
	}


	private void setFields(EditConfigurationVTwo config, VitroRequest vreq) {
		//this should be non-empty, todo: add validator
		config.addField(new FieldVTwo().setName("objectType"));

		config.addField(new FieldVTwo().setName("objectLabel").setRangeDatatypeUri(XSD.xstring.toString()));
		//See note in literals and uris on form regarding objectLabelDisplay
		config.addField(new FieldVTwo().setName("objectLabelDisplay").setRangeDatatypeUri(XSD.xstring.toString()));

		config.addField(new FieldVTwo().setName("objectDescription").setRangeDatatypeUri(XSD.xstring.toString()));
		config.addField(new FieldVTwo().setName("existingObjectURI"));
		
		//specifically for project
		config.addField(new FieldVTwo().setName("projectDescription"));
	}


	private void setLiteralsAndUrisOnForm(EditConfigurationVTwo config,
			VitroRequest vreq) {
		config.addLiteralsOnForm("objectLabel", "objectDescription", "objectLabelDisplay", "projectDescription"); //unsure what purpose objectLabelDisplay plays, it doesn't show up in n3 anywhere
		//but does appear to have some role within the custom form autocomplete javascript as evidenced in the role forms
		config.addUrisOnForm("objectType", "existingObjectURI");
				

		
	}


	private void addNewResources(EditConfigurationVTwo config) {
		//if the object doesn't already exist, then a new one will be created
		config.addNewResource("object", DEFAULT_NS_FOR_NEW_RESOURCE);
		
	}


	private void addN3(EditConfigurationVTwo config) {
		config.addN3Optional(this.getExistingObjectN3());
		//In case of new investigation or project being created
		config.addN3Optional(this.getProductN3());
		config.addN3Optional(this.getInvestigationN3());
		config.addN3Optional(this.getProjectN3());
		
	}


	//get existing object - only URI is required and no other fields will be populated
	//existing object uri won't require another type to be added
	private String getExistingObjectN3() {
		return this.getPrefixes() + this.getDatasetN3Var() + " datastarCore:isDataProductOf ?existingObjectURI .";
	}

	//object can be either investigation OR project individual
	//If this is an existing investigation or project then should be URI
	//But if new, then it will require a label and type 
	private String getProductN3() {
		return this.getPrefixes() + this.getDatasetN3Var() + " datastarCore:isDataProductOf ?object ." + 
				"?object rdfs:label ?objectLabel . " + 
				"?object rdf:type ?objectType .";
	}
	

	//The n3 for investigation
	private List<String> getInvestigationN3() {
		List<String> investigationN3 = new ArrayList<String>();
		investigationN3.add(this.getPrefixes() + 
		"?object datastarCore:description ?objectDescription .");
		return investigationN3;
	}
	
	private List<String> getProjectN3() {
		List<String> projectN3 = new ArrayList<String>();
		//TODO: Add more interesting information about project
		projectN3.add(this.getPrefixes() + "?object vivoCore:description ?projectDescription .");
		return projectN3;
	}

	//Form specific data
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		//Should edit mode go in here somewhere?
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
	    	//Here, if this is an edit, then we will add objectUri as existingObjectUri
	    	if(EditConfigurationUtils.getObjectUri(vreq) != null) {
	    		urisInScope.put("existingObjectURI", 
	    				Arrays.asList(new String[]{EditConfigurationUtils.getObjectUri(vreq)}));
	    	}
	    	
	    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
	    }
	

	 private String getDatasetN3Var() {
		 return "?" + this.getDatasetVar();
	 }
	 private String getDatasetVar() {
		 return "dataset";
	 }
	 
	static final String DEFAULT_NS_TOKEN=null; //null forces the default NS
	
	public String getPrefixes() {
		return "@prefix vivoCore:<" + vivoCore + "> ." + 
	"@prefix datastarCore:<http://purl.org/datastar/> . " + 
	"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + 
	"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .";
	}
	
}
