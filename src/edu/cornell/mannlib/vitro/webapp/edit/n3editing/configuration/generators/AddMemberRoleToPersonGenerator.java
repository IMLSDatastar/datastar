/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.CombinedConstantFieldAndChildVClassOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ConstantFieldOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ChildVClassesOptions;

public class AddMemberRoleToPersonGenerator extends AddRoleToPersonTwoStageGenerator {
	
	private static String template = "addMemberRoleToPerson.ftl";
	private static String VCLASS_URI = "http://xmlns.com/foaf/0.1/Organization";
	private static String DatastarNS = "http://purl.org/datastar/";    

	@Override
	String getTemplate() {
		return template;
	}   
	
	@Override
	String getRoleType() {	
		return "http://vivoweb.org/ontology/core#MemberRole";
	}
	
    @Override
    FieldOptions getRoleActivityFieldOptions(VitroRequest vreq) throws Exception {     
    	//Adding research group as class to this list
    	ConstantFieldOptions cfo = new ConstantFieldOptions(
		        DatastarNS + "ResearchGroup", "ResearchGroup"
		        );	
    	
        ChildVClassesOptions cvo = new ChildVClassesOptions(VCLASS_URI)
            .setDefaultOptionLabel( "Select one");	
        
        CombinedConstantFieldAndChildVClassOptions cco = new CombinedConstantFieldAndChildVClassOptions(cfo,cvo);
        return cco;
        
	}
	
	@Override
	boolean isShowRoleLabelField(){return true;}    
}
