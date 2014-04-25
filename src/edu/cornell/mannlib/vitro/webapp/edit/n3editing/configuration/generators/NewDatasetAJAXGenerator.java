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
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.FoafNameToRdfsLabelPreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;

/**
 * Generates the edit configuration for a default property form.
 * ModelChangePreprocessor creates the rdfs:label statement. 
 */
public class NewDatasetAJAXGenerator implements EditConfigurationAJAXGenerator { 
	private Log log = LogFactory.getLog(NewDatasetAJAXGenerator.class);	

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
		if(action == null || action.length() == 0 || action.isEmpty()) {
			handleAddAuthors(config, counter);
		}
		else if(action.equals("DataLink")) {
			//handle data link
			handleAddDataLink(config, counter);
		} else {
			//Do nothing
		}
	}

	
	//Adding authors
	private void handleAddAuthors(EditConfigurationVTwo config, int counter) {
		//Add new resources
		addNewResources(config, counter);
		//Add new n3 pertaining to new author
		addN3(config, counter);
		//add literals and uris on form
		addLiteralsAndUrisOnForm(config, counter);
		//add fields
		addFields(config, counter);
		//Passing new number of authors to form as well in case of error validation where page needs to show the added authors
		config.addFormSpecificData("authorCounter", counter);
	}
	
	//ADDING Data links
	private void handleAddDataLink(EditConfigurationVTwo config, int counter) {
		// TODO Auto-generated method stub
		//Add new resources
		addNewDataResources(config, counter);
		//Add new n3 pertaining to new author
		addDataN3(config, counter);
		//add literals and uris on form
		addDataLiteralsAndUrisOnForm(config, counter);
		//add fields
		addDataFields(config, counter);
		//Passing new number of data fields
		config.addFormSpecificData("datalinkCounter", counter);
	}

	
	private void addNewDataResources(EditConfigurationVTwo config, int counter) {
		config.addNewResource(this.getLinkVar(counter), DEFAULT_NS_TOKEN);
	}

	private void addDataN3(EditConfigurationVTwo config, int counter) {
		//hardcoded newInd in but should have a better way of specifying that dynamically
		//as in the original code
		String N3_FOR_WEBPAGE = 
				 "?newInd ?webpageProperty ?vcard . \n"+
				"?vcard    ?inverseProperty ?newInd  . \n"+
				"?vcard a ?vcardType . \n" +
    	        "?vcard <http://www.w3.org/2006/vcard/ns#hasURL> " + this.getLinkN3Var(counter) + " ."+
    	        this.getLinkN3Var(counter) + "    a                ?linkClass  . \n" +      
		        this.getLinkN3Var(counter) + "    ?linkUrlPredicate         " + this.getURLN3Var(counter) + " .";    
		//only one url type used here and that is not cloned so just use regular variable        				
		String N3_FOR_URLTYPE =
				this.getLinkN3Var(counter) + " a ?urlType .";
		String N3_FOR_ANCHOR =
				this.getLinkN3Var(counter) + " ?linkLabelPredicate " + this.getLinkLabelN3Var(counter) +  " .";
		String N3_FOR_RANK = 
				this.getLinkN3Var(counter) + " ?rankPredicate " + this.getDataRankN3Var(counter) + " .";
		String N3_FOR_FORMAT = 
				this.getLinkN3Var(counter) + " ?dataFileFormatPredicate " + this.getDataFileFormatN3Var(counter) + " .";
		config.addN3Optional(N3_FOR_WEBPAGE, N3_FOR_URLTYPE, N3_FOR_ANCHOR, N3_FOR_RANK, N3_FOR_FORMAT);
		
	}
	
	protected String getLinkN3Var(int counter) {
		return getN3VariableName(getLinkVar(counter));
	}
	
	protected String getLinkVar(int counter) {
		return "link" + counter;
	}
	
	protected String getURLN3Var(int counter) {
		return getN3VariableName(getURLVar(counter));
	}
	
	protected String getURLVar(int counter) {
		return "url" + counter;
	}
	
	protected String getLinkLabelN3Var(int counter) {
		return getN3VariableName(getLinkLabelVar(counter));
	}
	
	protected String getLinkLabelVar(int counter) {
		return "linkLabel" + counter;
	}
	
	protected String getDataRankN3Var(int counter) {
		return getN3VariableName(getDataRankVar(counter));
	}
	
	protected String getDataRankVar(int counter) {
		return "dataRank" + counter;
	}
	
	protected String getDataFileFormatN3Var(int counter) {
		return getN3VariableName(getDataFileFormatVar(counter));
	}
	
	protected String getDataFileFormatVar(int counter) {
		return "dataFileFormat" + counter;
	}
	

	private void addDataLiteralsAndUrisOnForm(EditConfigurationVTwo config,
			int counter) {
		List<String> literals = new ArrayList<String>(Arrays.asList(new String[]{
				this.getURLVar(counter),this.getLinkLabelVar(counter),this.getDataRankVar(counter), this.getDataFileFormatVar(counter)}));
		 config.addLiteralsOnForm(literals);
		
	}

	private void addDataFields(EditConfigurationVTwo config, int counter) {
		try {
			List<String> validatorsArray = new ArrayList<String> (Arrays.asList(new String[]{"nonempty", "datatype:"+XSD.anyURI.toString(), "httpUrl"}));
			config.addField(new FieldVTwo().
		            setName("url" + counter).
		            setValidators(validatorsArray).
		            setRangeDatatypeUri(XSD.anyURI.toString()));
		
		    config.addField(new FieldVTwo().
		            setName("linkLabel" + counter));
		    
		    config.addField(new FieldVTwo().
		            setName("dataRank" + counter).
		            setRangeDatatypeUri(XSD.integer.toString()));
		    
		    config.addField(new FieldVTwo().
		                setName("dataFileFormat" + counter));
			
			} catch(Exception ex) {
				log.error("Exception occurred in adding fields to the configuration ", ex);
			}
		
	}

	//ADDING Authors
	//Add new resources
	private void addNewResources(EditConfigurationVTwo config, int counter) {
        config.addNewResource(this.getAuthorshipUriVar(counter), DEFAULT_NS_TOKEN);
        config.addNewResource(this.getNewPersonVar(counter), DEFAULT_NS_TOKEN);
        config.addNewResource(this.getNewOrgVar(counter), DEFAULT_NS_TOKEN);
	}
	
	//Add n3 strings
	private void addN3(EditConfigurationVTwo config, int counter) {
		config.addN3Required(this.getN3NewAuthorship(counter));
		this.addN3Optional(config, counter);
	}
	
	
	//TODO: Update all of these to match the relates/relatedBy
	private String getN3NewAuthorship(int counter) {
		return getN3PrefixString() + 
		this.getAuthorshipUriN3Var(counter) + " a core:Authorship ;\n" + 
        "  core:relates ?newInd .\n" + 
        "?newInd core:relatedBy " + this.getAuthorshipUriN3Var(counter) + " .";
	}
	
	public void addN3Optional(EditConfigurationVTwo config, int counter) {
        config.addN3Optional(getN3NewPersonFirstName(counter)); 
        config.addN3Optional(getN3NewPersonMiddleName(counter));
        config.addN3Optional(getN3NewPersonLastName(counter));             
        config.addN3Optional(getN3NewPerson(counter));
        config.addN3Optional(getN3AuthorshipRank(counter));
        config.addN3Optional(getN3ForExistingPerson(counter));
        config.addN3Optional(getN3NewOrg(counter));
        config.addN3Optional(getN3ForExistingOrg(counter));
	}
	
	
	private String getN3NewPersonFirstName(int counter) {
		return getN3PrefixString() + 
		this.getNewPersonN3Var(counter) + " foaf:firstName "+ this.getFirstNameN3Var(counter) + " .";
	}
	
	private String getN3NewPersonMiddleName(int counter) {
		return getN3PrefixString() +  
		this.getNewPersonN3Var(counter) + " core:middleName " + this.getMiddleNameN3Var(counter) + " .";
	}
	
	private String getN3NewPersonLastName(int counter) {
		return getN3PrefixString() + 
		this.getNewPersonN3Var(counter) + " foaf:lastName " + this.getLastNameN3Var(counter) + " .";
	}
	
	private String getN3NewPerson(int counter) {
		return  getN3PrefixString() + 
		this.getNewPersonN3Var(counter) + " a foaf:Person ;\n" + 
        "<" + RDFS.label.getURI() + "> " + this.getLabelN3Var(counter) + " .\n" + 
        this.getAuthorshipUriN3Var(counter) + " core:relates " + this.getNewPersonN3Var(counter) + " .\n" + 
        this.getNewPersonN3Var(counter) + " core:relatedBy " + this.getAuthorshipUriN3Var(counter) + " . ";
	}
	
	private String getN3ForExistingPerson(int counter) {
		return getN3PrefixString() + 
		this.getAuthorshipUriN3Var(counter) + " core:relates " + this.getExistingPersonN3Var(counter) + " .\n" + 
		this.getExistingPersonN3Var(counter) + " core:relatedBy " + this.getAuthorshipUriN3Var(counter) + " .";
	}
	
	private String getN3NewOrg(int counter) {
		return  getN3PrefixString() + 
        this.getNewOrgN3Var(counter) + " a foaf:Organization ;\n" + 
        "<" + RDFS.label.getURI() + "> " + this.getOrgNameN3Var(counter) + " .\n" + 
        this.getAuthorshipUriN3Var(counter) + " core:relates " + this.getNewOrgN3Var(counter) + " .\n" + 
        this.getNewOrgN3Var(counter) + " core:relatedBy " + this.getAuthorshipUriN3Var(counter) + " . ";
	}
	
	private String getN3ForExistingOrg(int counter) {
		return getN3PrefixString() + 
		this.getAuthorshipUriN3Var(counter) + " core:relates " + this.getExistingOrgN3Var(counter) + " .\n" + 
		this.getExistingOrgN3Var(counter) + " core:relatedBy " + this.getAuthorshipUriN3Var(counter) + " .";
	}
	
	private String getN3AuthorshipRank(int counter) {
		return getN3PrefixString() +   
		this.getAuthorshipUriN3Var(counter) + " core:rank " + this.getRankN3Var(counter) + " .";
	}
	
	
	
	
	//Add fields
	private void addFields(EditConfigurationVTwo config, int counter) {
		setLabelField(config, counter);
    	setFirstNameField(config, counter);
    	setMiddleNameField(config, counter);
    	setLastNameField(config, counter);
    	setRankField(config, counter);
    	setPersonUriField(config, counter);
    	setOrgUriField(config, counter);
    	setOrgNameField(config, counter);
    	setSelectedAuthorLabelField(config, counter);
	}
	
	

	
	private void setLabelField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getLabelVar(counter)).
				setValidators(Arrays.asList(new String[]{"datatype:" + XSD.xstring.toString()})).
				setRangeDatatypeUri(XSD.xstring.toString())
				);
		
	}

	private void setSelectedAuthorLabelField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getSelectedAuthorLabelVar(counter))
				);
		
	}
	
	
	private void setFirstNameField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getFirstNameVar(counter)).
				setValidators(Arrays.asList(new String[]{"datatype:" + XSD.xstring.toString()})).
				setRangeDatatypeUri(XSD.xstring.toString())
				);
		
	}


	private void setMiddleNameField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getMiddleNameVar(counter)).
				setValidators(Arrays.asList(new String[]{"datatype:" + XSD.xstring.toString()})).
				setRangeDatatypeUri(XSD.xstring.toString())
				);
		
	}

	private void setLastNameField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getLastNameVar(counter)).
				setValidators(Arrays.asList(new String[]{"datatype:" + XSD.xstring.toString()})).
				setRangeDatatypeUri(XSD.xstring.toString())
				);
		
	}

	private void setRankField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getRankVar(counter)).
				setValidators(Arrays.asList(new String[]{"nonempty"})).
				setRangeDatatypeUri(XSD.xint.toString())
				);
		
	}


	private void setPersonUriField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getExistingPersonVar(counter))
				//.setObjectClassUri(personClass)
				);
		
	}

	private void setOrgUriField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getExistingOrgVar(counter))
				//.setObjectClassUri(personClass)
				);
		
	}
	private void setOrgNameField(EditConfigurationVTwo config, int counter) {
		config.addField(new FieldVTwo().
				setName(this.getOrgNameVar(counter)).
				setValidators(Arrays.asList(new String[]{"datatype:" + XSD.xstring.toString()})).
				setRangeDatatypeUri(XSD.xstring.toString())
				);
		
	}
	
	//Add literals and uris on form
	private void addLiteralsAndUrisOnForm(EditConfigurationVTwo config, int counter) {
		List<String> urisOnForm = new ArrayList<String>();
    	urisOnForm.add(this.getExistingPersonVar(counter));
    	urisOnForm.add(this.getExistingOrgVar(counter));
    	config.addUrisOnForm(urisOnForm);
    	
    	//for person who is not in system, need to add first name, last name and middle name
    	//Also need to store authorship rank and label of author
    	List<String> literalsOnForm = new ArrayList<String>();
    	literalsOnForm.add(this.getFirstNameVar(counter));
    	literalsOnForm.add(this.getMiddleNameVar(counter));
    	literalsOnForm.add(this.getLastNameVar(counter));
    	literalsOnForm.add(this.getRankVar(counter));
    	literalsOnForm.add(this.getOrgNameVar(counter));
    	literalsOnForm.add(this.getLabelVar(counter));
    	literalsOnForm.add(this.getSelectedAuthorLabelVar(counter));
    	config.addLiteralsOnForm(literalsOnForm);	
	}
	
	
	//Specify variable based on count
	
	
	//Specify n3 variable based on count
	protected String getFirstNameN3Var(int counter) {
		return getN3VariableName(getFirstNameVar(counter));
	}
	
	protected String getFirstNameVar(int counter) {
		return "firstName" + counter;
	}
	
	protected String getMiddleNameN3Var(int counter) {
		return getN3VariableName(getMiddleNameVar(counter));
	}
	
	protected String getMiddleNameVar(int counter) {
		return "middleName" + counter;
	}
	
	protected String getLastNameN3Var(int counter) {
		return getN3VariableName(getLastNameVar(counter));
	}
	
	protected String getLastNameVar(int counter) {
		return "lastName" + counter;
	}
	
	
	protected String getRankN3Var(int counter) {
		return getN3VariableName(getRankVar(counter));
	}
	
	protected String getRankVar(int counter) {
		return "rank" + counter;
	}
	
	protected String getExistingOrgN3Var(int counter) {
		return getN3VariableName(getExistingOrgVar(counter));
	}
	
	protected String getExistingOrgVar(int counter) {
		return "orgUri" + counter;
	}
	
	protected String getOrgNameN3Var(int counter) {
		return getN3VariableName(getOrgNameVar(counter));
	}
	
	protected String getOrgNameVar(int counter) {
		return "orgName" + counter;
	}
	
	
	protected String getLabelN3Var(int counter) {
		return getN3VariableName(getLabelVar(counter));
	}
	
	protected String getLabelVar(int counter) {
		return "label" + counter;
	}
	
	protected String getSelectedAuthorLabelVar(int counter) {
		return "selectedAuthorLabel" + counter;
	}
	
	
	protected String getExistingPersonN3Var(int counter) {
		return getN3VariableName(getExistingPersonVar(counter));
	}
	
	protected String getExistingPersonVar(int counter) {
		return "personUri" + counter;
	}
	
	
	protected String getNewPersonN3Var(int counter) {
		return getN3VariableName(getNewPersonVar(counter));
	}
	
	protected String getNewPersonVar(int counter) {
		return "newPerson" + counter;
	}
	
	
	protected String getNewOrgN3Var(int counter) {
		return getN3VariableName(getNewOrgVar(counter));
	}
	
	protected String getNewOrgVar(int counter) {
		return "newOrg" + counter;
	}
	
	
	protected String getAuthorshipUriN3Var(int counter) {
		return getN3VariableName(getAuthorshipUriVar(counter));
	}
	protected String getAuthorshipUriVar(int counter) {
		return getVariableName("authorshipUri", counter);
	}
	
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
	
	static final String DEFAULT_NS_TOKEN=null; //null forces the default NS

	
	
}
