/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ConstantFieldOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldOptions;

public class AddResearcherRoleToPersonGenerator extends AddRoleToPersonTwoStageGenerator {
	
	private static String template = "addResearcherRoleToPerson.ftl";
	private static String DatastarNS = "http://purl.org/datastar/";    
	@Override
	String getTemplate() {
		return template;
	}

	@Override
	public String getRoleType() {
		return "http://vivoweb.org/ontology/core#ResearcherRole";
	}
		
	/** Researcher role involves hard-coded options for the "right side" of the role or activity. */
	@Override
	FieldOptions getRoleActivityFieldOptions(VitroRequest vreq) throws Exception {
		return new ConstantFieldOptions(
		        "", "Select one", 
		        "http://vivoweb.org/ontology/core#Grant", "Grant", 
		        "http://vivoweb.org/ontology/core#Project", "Project",
		        DatastarNS + "Investigation", "Investigation");		
	}

	@Override  
    boolean isShowRoleLabelField() { return true;  }
   /* 
    * Use the methods below to change the date/time precision in the
    * custom form associated with this generator. When not used, the
    * precision will be YEAR. The other precisons are MONTH, DAY, HOUR,
    * MINUTE, TIME and NONE.
    */
/*
    public String getStartDatePrecision() {
        String precision = VitroVocabulary.Precision.MONTH.uri();
	    return precision;
    }

    public String getEndDatePrecision() {
        String precision = VitroVocabulary.Precision.DAY.uri();
	    return precision;
    }
*/    
}
