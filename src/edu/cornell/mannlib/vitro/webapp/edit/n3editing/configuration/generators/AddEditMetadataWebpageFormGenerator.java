/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
/**

Custom form for adding or editing a webpage associated with an individual. The primary page,
ManageWebpagesForIndividual, should forward to this page if: (a) we are adding a new page, or 
(b) an edit link in the Manage Webpages view has been clicked. But right now (a) is not implemented. 

Object properties: 
core:webpage (range: core:URLLink)
core:webpageOf (domain: core:URLLink) (inverse of core:webpage)

Class: 
core:URLLink - the link to be added to the individual

Data properties of core:URLLink:
core:linkURI
core:linkAnchorText
core:rank

*/
public class AddEditMetadataWebpageFormGenerator extends AddEditWebpageFormGenerator implements EditConfigurationGenerator {
    public static Log log = LogFactory.getLog( AddEditDatasetWebpageFormGenerator.class );
    private static String formTemplate = "addEditMetadataWebpageForm.ftl";
    static String datastarCore = "http://purl.org/datastar/";

    /* Note on ordering by rank in sparql: if there is a non-integer value on a link, that will be returned,
     * since it's ranked highest. Preventing that would require getting all the ranks and sorting in Java,
     * throwing out non-int values. 
     */
    private static String MAX_RANK_QUERY = ""
        + "PREFIX core: <http://vivoweb.org/ontology/core#> \n"
        + "PREFIX datastarCore: <" + datastarCore + "> \n"
        + "SELECT DISTINCT ?rank WHERE { \n"
        + "    ?subject datastarCore:hasAssociatedMetadata ?metadata . \n" 
        + "    ?metadata <http://purl.obolibrary.org/obo/ARG_2000028> ?vcard . \n"
        + "    ?vcard vcard:hasURL ?link . \n"
        + "    ?link core:rank ?rank .\n"
        + "} ORDER BY DESC(?rank) LIMIT 1";
    
    static String N3_FOR_FORMAT = 
            "?metadata ?associatedMetadataFormatPredicate ?associatedMetadataFormat .";
    
    static String FORMAT_QUERY =
            "SELECT ?formatExisting WHERE { ?metadata ?associatedMetadataFormatPredicate ?formatExisting }";
    
    static String metadataVcardQuery =
    		"PREFIX datastarCore: <" + datastarCore + "> \n" +
            "SELECT ?existingVcard WHERE { \n" +
            "?subject datastarCore:hasAssociatedMetadata ?metadata . \n"   + 
            "?metadata <http://purl.obolibrary.org/obo/ARG_2000028>  ?existingVcard . \n" +
            "}";
    
    static String N3_FOR_METADATA_WEBPAGE = 
    		"@prefix datastarCore: <" + datastarCore + "> ." +
    		"?subject ?hasAssociatedMetadataPredicate ?metadata . \n" + 
            "?metadata  a datastarCore:Metadata. \n" + 
            "?metadata ?webpageProperty ?vcard .\n" + 
            "?vcard <http://www.w3.org/2006/vcard/ns#hasURL> ?link . \n" +
            "?metadata ?inverseHasAssociatedMetadataPredicate ?subject . \n" + 
            "?vcard    ?inverseProperty ?metadata . \n"+
            "?link a <http://www.w3.org/2006/vcard/ns#URL> . \n" +
            "?link ?linkUrlPredicate ?url .";
    //This should overwrite the n3 required, not add to it
    //because in this case the metadata and not the subject has a vcard
    
    static String N3_FOR_LINKCLASS = 
    		"?link a <" + getURLLinkClass() + "> .";
    //Changing the template that is utilized
    @Override
    protected String getTemplate() {
    	return formTemplate;
    }

   
    @Override
    protected String getMaxRankQueryStr() {
    	return MAX_RANK_QUERY;
    }
    
    protected static String getURLLinkClass() {
    	return datastarCore + "MetadataURLLink";
    }

    //Update to this method includes adding the dataset format to the possible options
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) throws Exception {
    	EditConfigurationVTwo config = super.setupConfig(vreq, session);
    	//change var name for object to metadata
        config.setVarNameForObject("metadata");
    	//for metadata	
    	config.addNewResource("metadata", DEFAULT_NS_FOR_NEW_RESOURCE);
    	
       	//Add predicate for data file format
    	config.addUrisInScope("associatedMetadataFormatPredicate",       list( datastarCore + "associatedMetadataFormat"));
    	config.addUrisInScope("hasAssociatedMetadataPredicate",       list( datastarCore + "hasAssociatedMetadata"));
    	config.addUrisInScope("inverseHasAssociatedMetadataPredicate",       list( datastarCore + "isAssociatedMetadataFor"));
    	if ( config.isUpdate() ) {
    		String vcardUri = getVcardUri(vreq);
 	        config.addUrisInScope("vcard",  list( vcardUri ));
 	    }
    	
    	
    	//N3 will be different here, we are keeping the vcard stuff but changing the link class
	    config.setN3Required(list( this.getN3ForWebpage(), this.N3_FOR_LINKCLASS ));
	    //Add dataset format as possible n3
    	config.addN3Optional(N3_FOR_FORMAT);
 	    
    	//Add literal for form
    	List<String> litForm = new ArrayList<String>();
    	litForm.add("associatedMetadataFormat");
    	config.addLiteralsOnForm(litForm);
    	//the vcard is linked to the metadata and not the subject, this will overwrite the one from the original
	    config.addSparqlForAdditionalUrisInScope("vcard", metadataVcardQuery);
    	//Add existing literal query
        config.addSparqlForExistingLiteral("associatedMetadataFormat",   FORMAT_QUERY);
        //Add field object
        config.addField(new FieldVTwo().
                setName("associatedMetadataFormat"));
    	
        //run prepare
        super.prepare(vreq, config);
    	return config;
    }
    
    @Override
    protected String getN3ForWebpage() {
    	return N3_FOR_METADATA_WEBPAGE;
    }
    
    //this can be passed as parameter for existing statement
    private String getVcardUri(VitroRequest vreq) {
	    String vcardUri = vreq.getParameter("vcardUri"); 
        
		return vcardUri;
	}
}
