/* $This file is distributed under the terms of the license in /doc/license.txt$ */
/**Extends original NewIndividualFormGenerator to allow datasets to use custom form**/

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.List;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 * Generates the edit configuration for a default property form.
 * ModelChangePreprocessor creates the rdfs:label statement. 
 */
public class DatastarNewIndividualFormGenerator extends NewIndividualFormGenerator implements EditConfigurationGenerator {

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
        //Check if dataset type
    	if(isDatasetType(vreq)) {
    		return new NewDatasetFormGenerator().getEditConfiguration(vreq, session);
    	} else {
    		return super.getEditConfiguration(vreq, session);
    	}
    }
   
	
	public String getDatasetClassURI() {
		return "http://purl.org/datastar/Dataset";
	}
	
	public boolean isDatasetType(VitroRequest vreq) {
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		Boolean isDatasetType = Boolean.FALSE;
		String datasetType = getDatasetClassURI();
		String typeOfNew = getTypeOfNew(vreq);
	    List<String> superTypes = wdf.getVClassDao().getAllSuperClassURIs(typeOfNew);   
	    //Add the actual type as well to the list of super types
	    superTypes.add(typeOfNew);
	    if( superTypes != null ){
	    	for( String typeUri : superTypes){
	    		if( datasetType.equals(typeUri)) {
	    			isDatasetType = Boolean.TRUE;
	    			break;
	    		}
	    	}    	
	    }
	    return isDatasetType;
	}
	
	//Unclear whether this is accurate here or not
    private String getTypeOfNew(VitroRequest vreq) {
        String typeUri = vreq.getParameter("typeOfNew");
        if( typeUri == null || typeUri.trim().isEmpty() )
            return getFOAFPersonClassURI();
        else
            return typeUri; 
    }
    
    public String getFOAFPersonClassURI() {
		return "http://xmlns.com/foaf/0.1/Person";
	}
	
}
