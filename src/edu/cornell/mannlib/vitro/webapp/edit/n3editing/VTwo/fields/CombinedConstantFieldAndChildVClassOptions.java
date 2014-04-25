/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class CombinedConstantFieldAndChildVClassOptions implements FieldOptions {

    //constant field part
    private ConstantFieldOptions cfo = null;
    //part of this is the ChildVClassesOptions class
    private ChildVClassesOptions cvo = null;
    
    static Log log = LogFactory.getLog(ChildVClassesOptions.class);
    
    public CombinedConstantFieldAndChildVClassOptions(ConstantFieldOptions inputCfo, ChildVClassesOptions inputCvo) {
    	super();
    	this.cfo = inputCfo;
    	this.cvo = inputCvo;
    }
    
 

    @Override
    public Map<String, String> getOptions(            
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception{
        // now create an empty HashMap to populate and return
        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();
        //first get the child vclass options as these include the default label - it's added
        optionsMap.putAll(this.cvo.getOptions(editConfig, fieldName, wDaoFact));
        //get constant field options
        optionsMap.putAll(this.cfo.getOptions(editConfig, fieldName, wDaoFact));
        return optionsMap;
    }

    public ChildVClassesOptions getChildVClassesOptions() {
    	return this.cvo;
    	
    }
    
    public ConstantFieldOptions getConstantFieldOptions() {
    	return this.cfo;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }
}
