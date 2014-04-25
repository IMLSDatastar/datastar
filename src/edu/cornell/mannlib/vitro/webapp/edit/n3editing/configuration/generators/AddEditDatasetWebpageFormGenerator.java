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
public class AddEditDatasetWebpageFormGenerator extends AddEditWebpageFormGenerator implements EditConfigurationGenerator {
    public static Log log = LogFactory.getLog( AddEditDatasetWebpageFormGenerator.class );
    private static String formTemplate = "addEditDatasetWebpageForm.ftl";
    static String datastarCore = "http://purl.org/datastar/";


    
    static String N3_FOR_FORMAT = 
            "?link ?dataFileFormatPredicate ?dataFileFormat .";
    
    static String N3_FOR_LINKCLASS = 
    		"?link a <" + getURLLinkClass() + "> .";
    static String FORMAT_QUERY =
            "SELECT ?formatExisting WHERE { ?link ?dataFileFormatPredicate ?formatExisting }";
    
    //Changing the template that is utilized
    @Override
    protected String getTemplate() {
    	return formTemplate;
    }


    
    protected static String getURLLinkClass() {
    	return datastarCore + "DatasetURLLink";
    }
    
    
    
    //Update to this method includes adding the dataset format to the possible options
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) throws Exception {
    	EditConfigurationVTwo config = super.setupConfig(vreq, session);
    	//Add predicate for data file format
    	config.addUrisInScope("dataFileFormatPredicate",       list( datastarCore + "dataFileFormat"));
    	

 	    //where does link class go now? should now be added to the n3 required
 	    //And in the ontology DatasetURLLink should be made a subclass of vcard
 	    //overwrite n3 required
	    config.setN3Required(list( this.getN3ForWebpage(), this.N3_FOR_LINKCLASS ));
    	//Add dataset format as possible n3
    	config.addN3Optional(N3_FOR_FORMAT);
    	//Add literal for form
    	List<String> litForm = new ArrayList<String>();
    	litForm.add("dataFileFormat");
    	config.addLiteralsOnForm(litForm);
    	//Add existing literal query
        config.addSparqlForExistingLiteral("dataFileFormat",   FORMAT_QUERY);
        //Add field object
        config.addField(new FieldVTwo().
                setName("dataFileFormat"));
    	super.prepare(vreq,config);
    	return config;
    }

    
}
