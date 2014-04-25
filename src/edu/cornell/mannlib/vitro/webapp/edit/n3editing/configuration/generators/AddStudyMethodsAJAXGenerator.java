/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

/**
 * Generates the edit configuration for a default property form.
 * ModelChangePreprocessor creates the rdfs:label statement. 
 */
public class AddStudyMethodsAJAXGenerator implements EditConfigurationAJAXGenerator { 
	private Log log = LogFactory.getLog(AddStudyMethodsAJAXGenerator.class);	

    final static String vivoCore ="http://vivoweb.org/ontology/core#" ;
    final static String foaf = "http://xmlns.com/foaf/0.1/";


	public void modifyEditConfiguration(EditConfigurationVTwo config, VitroRequest vreq) {
		//Modify the configuration based on the parameters present here
		int counter = new Integer(vreq.getParameter("counter"));
		//check if there is a parameter for action
		String action = vreq.getParameter("action");
		handleAction(action, counter, config);		
    }
	
	private void handleAction(String action, int counter, 
			EditConfigurationVTwo config) {
		//default is addition
		if(action == null || action.length() == 0 || action.isEmpty() || action.equals("add")) {
			handleAddStudyMethods(config, counter);
		}
		else {
			
		}
	}

	
	//Adding study methods
	private void handleAddStudyMethods(EditConfigurationVTwo config, int counter) {
		//In the scenario that the user deletes a method step and then adds it again, the n3 is already
		//set up for that - or in the scenario where the user deletes a few of the method steps and adds a few
		//but not all back, the n3 is still set up for the newly added steps 
		if(!this.hasConfigurationForCount(config, counter)) {
			//Add new resources
			addNewResources(config, counter);
			//Add new n3 pertaining to new author
			addN3(config, counter);
			//add literals and uris on form
			addLiteralsAndUrisOnForm(config, counter);
			//add fields
			addFields(config, counter);
		}
		//Passing new number of authors to form as well in case of error validation where page needs to show the added authors
		config.addFormSpecificData("methodStepsCounter", counter);
	}
	
	

	protected String getMethodStepN3Var(int counter) {
		return getN3VariableName(getMethodStepVar(counter));
	}
	
	protected String getMethodStepVar(int counter) {
		return "methodStep" + counter;
	}
	
	protected String getMethodStepOrderN3Var(int counter) {
		return getN3VariableName(getMethodStepOrderVar(counter));
	}
	
	protected String getMethodStepOrderVar(int counter) {
		return "methodStepOrder" + counter;
	}
	
	protected String getMethodStepValueN3Var(int counter) {
		return getN3VariableName(getMethodStepValueVar(counter));
	}
	
	protected String getMethodStepValueVar(int counter) {
		return "methodStepValue" + counter;
	}
		
	//Add new resources
	private void addNewResources(EditConfigurationVTwo config, int counter) {
		config.addNewResource(getMethodStepVar(counter), DEFAULT_NS_TOKEN);
	}
	
	//Add n3 strings
	private void addN3(EditConfigurationVTwo config, int counter) {
		//using the same methods variable already connected to study design execution which is related to dataset
		//making this optional instead in case this needs to be removed later
		config.addN3Optional(this.getPrefixes() + 
				"?methods datastarCore:methodStep " + this.getMethodStepN3Var(counter) + " . " + 
				this.getMethodStepN3Var(counter) + " datastarCore:order "  + this.getMethodStepOrderN3Var(counter) + "; " + 
				" datastarCore:value " + this.getMethodStepValueN3Var(counter) + " . ");
	}
	
	
	//Add fields
	private void addFields(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().setName(this.getMethodStepOrderVar(counter)));
		config.addField(new FieldVTwo().setName(this.getMethodStepValueVar(counter)));
    
	}
	
	//Add literals and uris on form
	private void addLiteralsAndUrisOnForm(EditConfigurationVTwo config, int counter) {
		List<String> urisOnForm = new ArrayList<String>();
    	urisOnForm.add(this.getMethodStepVar(counter));
    	config.addUrisOnForm(urisOnForm);
    	
    	//for person who is not in system, need to add first name, last name and middle name
    	//Also need to store authorship rank and label of author
    	List<String> literalsOnForm = new ArrayList<String>();
    	literalsOnForm.add(this.getMethodStepOrderVar(counter));
    	literalsOnForm.add(this.getMethodStepValueVar(counter));
    	config.addLiteralsOnForm(literalsOnForm);	
	}
	
	
	//Specify variable based on count
	//Given variable name and count, return new variable name
	protected String getVariableName(String varName, int counter) {
		return varName + counter;
	}
	
	protected String getN3VariableName(String varName, int counter) {
		return "?" + getVariableName(varName, counter);
	}
	
	protected String getN3VariableName(String varName) {
		return "?" + varName;
	}
	
	
	// Helper methods
	public String getN3PrefixString() {
		return "@prefix core: <" + vivoCore + "> .\n" + 
		 "@prefix foaf: <" + foaf + "> .  \n"   ;
	}
	
	
	public String getPrefixes() {
		return "@prefix vivoCore:<" + vivoCore + "> ." + 
	"@prefix datastarCore:<http://purl.org/datastar/> . " + 
	"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + 
	"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .";
	}
	
	//Method to check whether fields corresponding to this counter are already present
	//which would indicate no requirement to add these fields again
	public boolean hasConfigurationForCount(EditConfigurationVTwo config, int counter) {
		Map<String, FieldVTwo> fields = config.getFields();
		String methodstepOrderVar = this.getMethodStepOrderVar(counter);
		String methodstepValueVar = this.getMethodStepValueVar(counter);
		return (fields.containsKey(methodstepOrderVar) && fields.containsKey(methodstepValueVar));
	}
	
	
	static final String DEFAULT_NS_TOKEN=null; //null forces the default NS

	
	
}
