/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mysql.jdbc.StringUtils;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeIntervalValidationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.N3ValidatorVTwo;

/**
 * Generates the edit configuration for a default property form.
 * ModelChangePreprocessor creates the rdfs:label statement. 
 */
public class TemporalCoverageFormAJAXGenerator implements EditConfigurationAJAXGenerator { 
	private Log log = LogFactory.getLog(TemporalCoverageFormAJAXGenerator.class);	

    final static String vivoCore ="http://vivoweb.org/ontology/core#" ;
    final static String foaf = "http://xmlns.com/foaf/0.1/";


	public void modifyEditConfiguration(EditConfigurationVTwo config, VitroRequest vreq) {
		//Modify the configuration based on the parameters present here
		//In this case, we are checking whether a date time value or interval have been selected
		String temporalCoverageType = vreq.getParameter("temporalCoverageType");
		handleTemporalCoverageType(temporalCoverageType, config);		
    }
	
	private void handleTemporalCoverageType(String temporalCoverageType,
			EditConfigurationVTwo config) {
		if(!StringUtils.isNullOrEmpty(temporalCoverageType)) {
			if(temporalCoverageType.equals("dateTimeInterval")) {
			//Add validator - the form template must be this one for the validation to work correctly
				config.addValidator(new DateTimeIntervalValidationVTwo("startField","endField","dateTimeIntervalForm.ftl"));
			} else if(temporalCoverageType.equals("dateTimeValue")) {
				//if the configration has a validator attached, remove since dateTimeValue has no validators
				List<N3ValidatorVTwo> validators = config.getValidators();
				if(validators.size() > 0) {
					//this will remove any validators
					//TODO: Remove only datetime interval validation in case other validators are added later
					config.getValidators().clear();
				}
			}
		} else {
		}
	}	
	
}
