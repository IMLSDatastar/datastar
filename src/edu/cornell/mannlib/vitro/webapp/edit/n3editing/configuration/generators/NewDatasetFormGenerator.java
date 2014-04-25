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
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.PublicationHasAuthorValidator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaVClassOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;

/**
 * Generates the edit configuration for a default property form.
 * ModelChangePreprocessor creates the rdfs:label statement. 
 */
public class NewDatasetFormGenerator extends VivoBaseGenerator implements EditConfigurationGenerator {
	private Log log = LogFactory.getLog(NewDatasetFormGenerator.class);	

	AddAuthorsToInformationResourceGenerator addAuthorsGenerator = null;
	final static String dateValueType = vivoCore + "DateTimeValue";
	final static String datastarCore ="http://purl.org/datastar/";

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
        //initialize in order to utilize methods
    	addAuthorsGenerator = new AddAuthorsToInformationResourceGenerator();
    	
    	
    	EditConfigurationVTwo config = new EditConfigurationVTwo();
    	config.setVarNameForSubject(this.getDatasetVar());
    	config.setTemplate( "newDatasetForm.ftl" );
    	
    	//required: title, identifier, author
    	//optional: published to repository (should we have a set of repositories and then allow new creation), Data URL location
    	//should we employ autocomplete form?
    	//There is a list of repositories we could get from somewhere but unsure how it would be read in dynamically
    	//And we would need a custom URI for this? 
    	//Dataset has optional elements such as published to repository
    	
    	//Literals and uris on form are "set" as opposed to added within authorship 
    	//generator so calling this first, otherwise the dataset literals will be overridden
    	/*** Authorship ***/
    	this.addAuthorship(config, vreq);
    	/*** Core dataset fields ***/
    	this.addMainDatasetInformation(config, vreq);
    	/*** Publication date***/
    	this.addPublicationDate(config);
    	/*** Published to repository ***/
    	this.addPublishedToRepository(config);
    	/*** Data link ***/
    	this.addDataLinks(config);
    	/*** Registration Date Time ***/
    	this.addRegistrationDateTime(config);
    	
    	//form specific data
        addFormSpecificData(config, vreq);        
       
        //validators
        config.addValidator(new AntiXssValidation());
        //check that authors are actually entered correctly
        config.addValidator(new PublicationHasAuthorValidator());

        //any preprocessors would go here

        String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);       
        config.setFormUrl(formUrl);
        
        //Note, the spaces are important - they were added by ProcessRdfFormController earlier
        //as a means of ensuring the substitution worked correctly - as the regex expects spaces
        config.setEntityToReturnTo(" ?newInd ");
        prepare(vreq, config);
    	return config;
    }
    
    /*****Main dataset information **********/
    
    private void addMainDatasetInformation(EditConfigurationVTwo config, VitroRequest vreq) {
    	this.addMainDatasetN3(config, vreq);
    	//add new resource
    	config.addNewResource(this.getDatasetVar(), vreq.getWebappDaoFactory().getDefaultNamespace());
    	//literals on form
    	config.addLiteralsOnForm( list( "datasetLabel", "datasetDescription" ));
    	//uris and literals in scope
    	setUrisAndLiteralsInScope(config);
    	//No SPARQL queries for existing since this is only used to create new, never for edit  
    	//Add fields
    	config.addField(new FieldVTwo().
                setName("datasetLabel").
    	        setRangeDatatypeUri(XSD.xstring.getURI()).
                setValidators(getDatasetLabelValidators(vreq)));   
    	
    	config.addField(new FieldVTwo().
                setName("datasetDescription").
    	        setRangeDatatypeUri(XSD.xstring.getURI()).
                setValidators(getDatasetDescriptionValidators(vreq)));    	  
		
	}

	private void addMainDatasetN3(EditConfigurationVTwo config,
			VitroRequest vreq) {
		config.addN3Required( list(
    	        this.getDatasetN3Var() + " <" + VitroVocabulary.RDF_TYPE  + "> <" + getTypeOfNew(vreq) + "> .",
    	        N3_PREFIX + this.getDatasetN3Var() + " <" + RDFS.label.getURI() + "> ?datasetLabel .",
    	        this.getDatasetN3Var() + " <" + datastarCore + "description> ?datasetDescription ." 
    	));    
		
	}
	private void setUrisAndLiteralsInScope(EditConfigurationVTwo editConfiguration) {
	    	HashMap<String, List<String>> urisInScope = new HashMap<String, List<String>>();
	    	//note that at this point the subject, predicate, and object var parameters have already been processed
	    	urisInScope.put(editConfiguration.getVarNameForSubject(), 
	    			Arrays.asList(new String[]{editConfiguration.getSubjectUri()}));
	    	urisInScope.put(editConfiguration.getVarNameForPredicate(), 
	    			Arrays.asList(new String[]{editConfiguration.getPredicateUri()}));
	    	editConfiguration.setUrisInScope(urisInScope);
	    	//Uris in scope include subject, predicate, and object var
	    	
	    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
	}
	
	//validate label if person is not true
		private List<String> getDatasetLabelValidators(VitroRequest vreq) {
			List<String> validators = new ArrayList<String>();
			validators.add("nonempty");
			return validators;
		}
		
		private List<String> getDatasetDescriptionValidators(VitroRequest vreq) {
			List<String> validators = new ArrayList<String>();
			validators.add("nonempty");
			return validators;
		}

		//Get parameter from HTTP request for type of new individual
	    private String getTypeOfNew(VitroRequest vreq) {
	        String typeUri = vreq.getParameter("typeOfNew");
	            return typeUri; 
	    }
	    
	    //Form specific data
		public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
			HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
			formSpecificData.put("typeName", getTypeName(vreq));
			//add author specific form specific data 
			this.addAuthorsFormSpecificData(formSpecificData, vreq);
			//add data link specific form data
			this.addDataLinksFormSpecificData(formSpecificData);
			editConfiguration.setFormSpecificData(formSpecificData);
		}

		

		private String getTypeName(VitroRequest vreq) {
			String typeOfNew = getTypeOfNew(vreq);
			VClass type = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(typeOfNew);
			return type.getName();
		}
	
	/******Authorship info********/
	private void addAuthorship(EditConfigurationVTwo config, VitroRequest vreq) {
		// N3 - in this case required b/c at least one author required
		this.addAuthorshipN3(config);
		
		//New Resources
    	this.setAuthorshipNewResources(config);
    	//uris and literals on form
        this.setAuthorsLiteralsAndUrisOnForm(config, vreq);
        //Set fields
    	this.setAuthorshipFields(config, vreq);
		
	}

	private void addAuthorshipN3(EditConfigurationVTwo config) {
		config.addN3Required(this.getNewAuthorshipN3());
		//This is the first optional n3
		config.addN3Optional(list(
	    	     this.getAuthorshipOptional()   
	    	));
	}

	private void setAuthorshipFields(EditConfigurationVTwo config, VitroRequest vreq) {
    	//predicate uri is never utilized in original method so just passing null
		this.addAuthorsGenerator.setFields(config, vreq, null);
		//Adding display label for existing author or organization selected by autocomplete
		//This is not used in the n3 anywhere but is useful in case of validation errors when the label needs to be displayed
		config.addField(new FieldVTwo().setName("selectedAuthorLabel"));
	}

	private List<String> getAuthorshipOptional() {
		return this.addAuthorsGenerator.generateN3Optional();
	}

	private String getNewAuthorshipN3() {
    	return "@prefix core: <" + vivoCore + "> .\n" + 
    	        "@prefix foaf: <" + foaf + "> .  \n" + 
    	        "?authorshipUri a core:Authorship ;\n" + 
    	        "  core:relates " + this.getDatasetN3Var() + " .\n" + 
    	        this.getDatasetN3Var() + " core:relatedBy ?authorshipUri .";
    }
    
	private void setAuthorshipNewResources(EditConfigurationVTwo config) {
		config.addNewResource("authorshipUri", DEFAULT_NS_TOKEN);
        config.addNewResource("newPerson", DEFAULT_NS_TOKEN);
        config.addNewResource("newOrg", DEFAULT_NS_TOKEN);
	}
	
    private void setAuthorsLiteralsAndUrisOnForm(EditConfigurationVTwo config, VitroRequest vreq) {
    	this.addAuthorsGenerator.setUrisAndLiteralsOnForm(config, vreq);
    	//Adding label for author selected by autocomplete, not used in N3 but saved to enable display in case of validation errors
    	config.addLiteralsOnForm("selectedAuthorLabel");
    }
	
    private void addAuthorsFormSpecificData(Map<String, Object> formSpecificData, VitroRequest vreq) {
    	//this is a new dataset form so newRank should start from 1
    	formSpecificData.put("newRank", 1);
		formSpecificData.put("rankPredicate", authorRankPredicate);
		formSpecificData.put("authorCounter", 1);
    }
	
	/****Registration Date Time********/
	private void addRegistrationDateTime(EditConfigurationVTwo config) {
		//n3
		this.setRegistrationTimeN3(config);
		//new resources
    	this.setRegistrationTimeNewResources(config);
    	//literals and uris ins cope
    	this.addRegistrationLiteralsAndUrisInScope(config);
	}

	

	private void setRegistrationTimeNewResources(EditConfigurationVTwo config) {
		config.addNewResource("registrationDateTime", DEFAULT_NS_TOKEN);
	}

	private void setRegistrationTimeN3(EditConfigurationVTwo config) {
		String n3 = this.getDatasetN3Var() + " <" + datastarCore + "registrationDateTimeValue> ?registrationDateTime . " + 
		    	"?registrationDateTime a <" + dateValueType + "> . " + 
		    	"?registrationDateTime  <" + dateTimeValue + "> ?registrationDateTime-value . \n" +
		    	"?registrationDateTime  <" + dateTimePrecision + "> ?registrationDateTime-precision .";
		//registration date time is required
    	config.addN3Required(n3);
    }
    
   private void addRegistrationLiteralsAndUrisInScope(
			EditConfigurationVTwo config) {
		//Get date time value
	   	//Get date time precision
	   //Should construct a value with the current date time
	   DateTime value = new DateTime();
      
       Literal dateTimeLiteral =  ResourceFactory.createTypedLiteral(
               ISODateTimeFormat.dateHourMinuteSecond().print(value), 
               XSDDatatype.XSDdateTime);
       String dateTimePrecisionURI = VitroVocabulary.Precision.SECOND.uri();
       List<String> resources = new ArrayList<String>();
       resources.add(dateTimePrecisionURI);
       config.addLiteralInScope("registrationDateTime-value", dateTimeLiteral);
       config.addUrisInScope("registrationDateTime-precision", resources);
		
	} 
    
	
    	
	
	 
	
/******Publication Date *******/
	 	//publication date field is optinal
	 private void addPublicationDate(EditConfigurationVTwo config) {
		 	//Add N3
		 	this.addPublicationDateN3(config);
		 	//Add new resources
		 	this.addPublicationDateNewResources(config);
		 	//Add literals/uris on form
		 
		 	//Add fields
	       this.addPublicationDateField(config);
    		
    		
	 }
	 
	 private void addPublicationDateN3(EditConfigurationVTwo config) {
		  String n3ForValue = 
			        this.getDatasetN3Var() + " <http://purl.org/ands/ontologies/vivo/dateOfPublication> ?publicationDateNode . \n" +
			        "?publicationDateNode a <" + dateValueType + "> . \n" +
			        "?publicationDateNode  <" + dateTimeValue + "> ?publicationDateField-value . \n" +
			        "?publicationDateNode  <" + dateTimePrecision + "> ?publicationDateField-precision .";
		  config.addN3Optional(new ArrayList<String>(Arrays.asList(new String[]{n3ForValue})));
	 }
	 
	 private void addPublicationDateNewResources(EditConfigurationVTwo config) {
		 config.addNewResource("publicationDateNode", DEFAULT_NS_FOR_NEW_RESOURCE);
	 }
	 
	 private void addPublicationDateField(EditConfigurationVTwo config) {
		 FieldVTwo publicationDate = new FieldVTwo().setName("publicationDateField");
 		publicationDate.setEditElement(new DateTimeWithPrecisionVTwo(publicationDate, 
 			VitroVocabulary.Precision.DAY.uri(), 
 				VitroVocabulary.Precision.NONE.uri()));
 			config.addField(publicationDate);
	 }
	 /******Published To Repository *******/
	 //published to repository field is optional
	 private void addPublishedToRepository(EditConfigurationVTwo config) {
		 	//Add N3
		 	this.addPublishedToRepositoryN3(config);
		 	//Add new resources
		 	this.addPublishedToRepositoryLiteralsAndUrisOnForm(config);
		 	//Add literals/uris on form
		 
		 	//Add fields
	       this.addPublishedToRepositoryField(config);
 		
 		
	 }
	 
	 private void addPublishedToRepositoryN3(EditConfigurationVTwo config) {
		  String n3ForRepository = this.getDatasetN3Var() + " <" + datastarCore + "publishedToRepository> ?repository . \n" +
			        "?repository  <" + datastarCore + "repositoryHasDataset> " + this.getDatasetN3Var() +  " ." ;
			        		
		  config.addN3Optional(new ArrayList<String>(Arrays.asList(new String[]{n3ForRepository})));
	 }
	 
	 private void addPublishedToRepositoryLiteralsAndUrisOnForm(EditConfigurationVTwo config) {
		config.addUrisOnForm("repository");
	 }
	 
	//default object property form expects a subject, in this case we don't have a uri
	 //because the dataset is being created
	 
	 private void addPublishedToRepositoryField(EditConfigurationVTwo config) {
		 FieldVTwo repositoryField = new FieldVTwo().setName("repository");
		 try { 
			 IndividualsViaVClassOptions options = new IndividualsViaVClassOptions(datastarCore + "Repository");
			 options.setDefaultOptionLabel("Select Repository");
			 repositoryField.setOptions( options);
		 } catch(Exception ex) {
			 log.error("Error occurred in setting options for field repository ", ex);
		 }
		 config.addField(repositoryField);
	 }
	 
	 /******Data links *******/
	 //Data links are deemed optional
	 private void addDataLinks(EditConfigurationVTwo config) {
		 	//Add N3
		 	this.addDataLinksN3(config);
		 	//Add new resources
		 	this.addDataLinksNewResources(config);
		 	//Add literals/uris on form
		 	this.addDataLinksLiteralsAndUrisOnForm(config);
		 	//Add literals and uris in scope
		 	this.addDataLinksLiteralsAndUrisInScope(config);
		 
		 	//Add fields
	       this.addDataLinksFields(config);
	      
	 }
	 
	 
	 //Webpage property, inverse property, rank predicate, data file format predicate
	private void addDataLinksLiteralsAndUrisInScope(EditConfigurationVTwo config) {
	    String vcardIndividualType = "http://www.w3.org/2006/vcard/ns#Kind";

		 config.addUrisInScope("webpageProperty",     list( this.getWebpageProperty()));
		 config.addUrisInScope("inverseProperty",     list( this.getWebpageOfProperty()));
		 config.addUrisInScope("linkClass",           list( this.getURLLinkClass()));
		 config.addUrisInScope("linkUrlPredicate",    list( "http://www.w3.org/2006/vcard/ns#url" ));
		 config.addUrisInScope("linkLabelPredicate", list( "http://www.w3.org/2000/01/rdf-schema#label" ));
		 config.addUrisInScope("rankPredicate",       list( vivoCore + "rank"));
		 config.addUrisInScope("dataFileFormatPredicate",       list( datastarCore + "dataFileFormat"));
		 config.addUrisInScope("vcardType",       list( vcardIndividualType ));

	}

	private void addDataLinksNewResources(EditConfigurationVTwo config) {
	    config.addNewResource("link", DEFAULT_NS_FOR_NEW_RESOURCE);
	    config.addNewResource("vcard", DEFAULT_NS_FOR_NEW_RESOURCE);
		
	}

	//Updates based on ISF changes - using vcards instead of webpage property
	private void addDataLinksN3(EditConfigurationVTwo config) {
		String N3_FOR_WEBPAGE = 
		        this.getDatasetN3Var() + " ?webpageProperty ?vcard . \n"+
		                "?vcard    ?inverseProperty " + this.getDatasetN3Var() + " . \n"+
		                "?vcard a ?vcardType . \n" +
		    	        "?vcard <http://www.w3.org/2006/vcard/ns#hasURL> ?link ."+
		                "?link    a                ?linkClass  . \n" +      
		                "?link    ?linkUrlPredicate        ?url .";      
		String N3_FOR_URLTYPE =
		        "?link a ?urlType .";
		String N3_FOR_ANCHOR =
		        "?link ?linkLabelPredicate ?linkLabel .";
		String N3_FOR_RANK = 
		        "?link ?rankPredicate ?dataRank .";
		String N3_FOR_FORMAT = 
	            "?link ?dataFileFormatPredicate ?dataFileFormat .";
		//rank and url type are available by default, so we should
		//add them all to the n3 for webpage, otherwise these statements will be added
		//even when there is no data link
		config.addN3Optional(N3_FOR_WEBPAGE + N3_FOR_RANK + N3_FOR_URLTYPE,  N3_FOR_ANCHOR, N3_FOR_FORMAT);
		
	}

	private void addDataLinksLiteralsAndUrisOnForm(EditConfigurationVTwo config) {
		 config.addUrisOnForm("urlType");
		 config.addLiteralsOnForm(list("url","linkLabel","dataRank", "dataFileFormat"));
		    
		    
		
	}

	private void addDataLinksFields(EditConfigurationVTwo config) {
		try {
		config.addField(new FieldVTwo().
	            setName("url").
	            setValidators(list("datatype:"+XSD.anyURI.toString(), "httpUrl")).
	            setRangeDatatypeUri(XSD.anyURI.toString()));
	    
	    config.addField( new FieldVTwo().
	            setName("urlType"));
	
	    config.addField(new FieldVTwo().
	            setName("linkLabel"));
	    
	    config.addField(new FieldVTwo().
	            setName("dataRank").
	            setRangeDatatypeUri(XSD.integer.toString()));
	    
	    config.addField(new FieldVTwo().
	                setName("dataFileFormat"));
		
		} catch(Exception ex) {
			log.error("Exception occurred in adding fields to the configuration ", ex);
		}
	}

	private void addDataLinksFormSpecificData(
			HashMap<String, Object> formSpecificData) {
		 //add form specific data
	    formSpecificData.put("dataLinkNewRank", 1 );
	    formSpecificData.put("datalinkCounter", 1);
	}
	//copied from AddEditDatasetWebpageForm
    protected String getWebpageProperty() {
    	return "http://purl.obolibrary.org/obo/ARG_2000028";
    }
    
    protected String getWebpageOfProperty() {
    	return "http://purl.obolibrary.org/obo/ARG_2000029";
    }
    
    
    protected String getURLLinkClass() {
    	return datastarCore + "DatasetURLLink";
    }

    /**Helper methods/variables**/
    
	private String N3_PREFIX = "@prefix foaf:<http://xmlns.com/foaf/0.1/> .\n";
	
	//Get N3 variable for dataset
	private String getDatasetN3Var() {
		return "?" + this.getDatasetVar();
	}
	
	private String getDatasetVar() {
		return "newInd";
	}
	
	static final String DEFAULT_NS_TOKEN=null; //null forces the default NS
	
}
