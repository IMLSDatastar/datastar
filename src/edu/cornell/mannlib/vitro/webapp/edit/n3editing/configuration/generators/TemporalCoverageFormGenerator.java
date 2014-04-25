/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeIntervalValidationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.utils.generators.EditModeUtils;


public class TemporalCoverageFormGenerator extends DateTimeIntervalFormGenerator
       {
	
	final static String datastarCore = "http://purl.org/datastar/";
	final static String toDateTimeInterval = datastarCore + "temporalCoverage";
	final static String toDateTimeValue = datastarCore + "temporalCoverage";

	final static String vivoCore = "http://vivoweb.org/ontology/core#";
	final static String valueType = vivoCore + "DateTimeValue";
	final static String dateTimeValue = vivoCore + "dateTime";
	final static String dateTimePrecision = vivoCore + "dateTimePrecision";

	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
			HttpSession session) {
        EditConfigurationVTwo conf = new EditConfigurationVTwo();
        super.setupEditConfiguration(conf, vreq, session);
       
        conf.setTemplate("temporalCoverageForm.ftl");
        
        //Overriding some of the other settings - subject, temporalCoverage, temporalCoverageNode 
        //which is either an interval or a value object
        //the predicate doesn't actually seem to be employed within the n3 anywhere so 
        //there wouldn't be any substitutions anyway
        conf.setVarNameForPredicate("temporalCoverage");
        //varname for object in original is intervalNode - we will keep that
        //as that is utilized within the N3
        //We will utilize that here too, although the node can either be a value or an interval node
        conf.addN3Optional(Arrays.asList(n3ForValue));
        
        
        conf.addSparqlForExistingLiteral(
        		"dateTimeField-value", existingDateTimeValueQuery);
        conf.addSparqlForExistingUris(
        		"dateTimeField-precision", existingPrecisionQuery);
        conf.addSparqlForExistingUris(getNodeVar(), existingNodeQuery);
        
        FieldVTwo dateTimeField = new FieldVTwo().setName("dateTimeField");
        		dateTimeField.setEditElement(new DateTimeWithPrecisionVTwo(dateTimeField, 
        				VitroVocabulary.Precision.SECOND.uri(), 
        				VitroVocabulary.Precision.NONE.uri()));
        
        conf.addField(dateTimeField);
        
        //Adding additional data, specifically edit mode
        addFormSpecificData(conf, vreq);
        //prepare
        prepare(vreq, conf);
        //Can get information regarding type of temporal coverage after sparql queries executed
        this.addTemporalCoverageTypeToFormData(conf);
        //the interval validator is selected by default and should remain since the form has interval selected first
        //Uncomment below in case the single date time is selected first
        //conf.getValidators().clear();
        return conf;
	}
	
	final  String n3ForValue = 
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
         getNodeN3Var() + " a <" + valueType + "> . \n" +
         getNodeN3Var() + "  <" + dateTimeValue + "> ?dateTimeField-value . \n" +
         getNodeN3Var() + " <" + dateTimePrecision + "> ?dateTimeField-precision .";
	
	final  String existingDateTimeValueQuery = 
        "SELECT ?existingDateTimeValue WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> ?existingValueNode . \n" +
        "?existingValueNode a <" + valueType + "> . \n" +
        "?existingValueNode <" + dateTimeValue + "> ?existingDateTimeValue }";
	
	final  String existingPrecisionQuery = 
        "SELECT ?existingPrecision WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> ?existingValueNode . \n" +
        "?existingValueNode a <" + valueType + "> . \n" +
        "?existingValueNode <"  + dateTimePrecision + "> ?existingPrecision }";
	
	final  String existingNodeQuery =
        "SELECT ?existingNode WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> ?existingNode . \n" +
        "?existingNode a <" + valueType + "> }";


	@Override
	public String getToDateTimeIntervalPredicate() {
		return toDateTimeInterval;
	}
	
	@Override
	public String getNodeVar() {
		return "temporallCoverageNode";
	}
	
	@Override
	public String getNodeN3Var() {
		return "?" + getNodeVar();
	}
	
//Adding form specific data such as edit mode
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		formSpecificData.put("editMode", getEditMode(vreq).name().toLowerCase());
		editConfiguration.setFormSpecificData(formSpecificData);
	}
	
	public void addTemporalCoverageTypeToFormData(EditConfigurationVTwo editConfiguration) {
		String temporalCoverageType = "dateTimeInterval";
		//From edit configuration, this is after prepare has been run
		//check whether dateTimeField-value or startField-value/endField-value exist within the literals
		//Another way to do this is to add another query and field specifically for type
		Map<String, List<Literal>> literalsInScope = editConfiguration.getLiteralsInScope();
		List<Literal> listL = literalsInScope.get("dateTimeField-value");

		if(literalsInScope.containsKey("dateTimeField-value")) {
			//check to make sure the value is not null or empty
			if(listL != null && listL.size() > 0 && listL.get(0) != null) {
				temporalCoverageType = "dateTimeValue";
			}
		} else if(literalsInScope.containsKey("startField-value")) {
			listL = literalsInScope.get("startField-value");
			if(listL != null && listL.size() > 0 && listL.get(0) != null) {
				temporalCoverageType = "dateTimeInterval";
			}
		}  else if(literalsInScope.containsKey("endField-value")) {
			listL = literalsInScope.get("endField-value");
			if(listL != null && listL.size() > 0 && listL.get(0) != null) {
				temporalCoverageType = "dateTimeInterval";
			}
		}
		editConfiguration.addFormSpecificData("temporalCoverageType", temporalCoverageType);
	}
	
	public EditMode getEditMode(VitroRequest vreq) {
		//In this case, the original jsp didn't rely on FrontEndEditingUtils
		//but instead relied on whether or not the object Uri existed
		String objectUri = EditConfigurationUtils.getObjectUri(vreq);
		EditMode editMode = FrontEndEditingUtils.EditMode.ADD;
		if(objectUri != null && !objectUri.isEmpty()) {
			editMode = FrontEndEditingUtils.EditMode.EDIT;
			
		}
		return editMode;
	}
}