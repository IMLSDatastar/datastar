/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ContextNodeFields;

/**
 * DocumentModifier that will run SPARQL queries for an
 * Individual and add all the columns from all the rows
 * in the solution set to the ALLTEXT field as well as create dynamic fields
 * that can be sued for faceting.
 *  
 * @author hjk54
 *
 */
public class DatastarObjectProperties extends ContextNodeFields{
    
 static List<String> queriesForDataset = new ArrayList<String>();  
 protected Log log = LogFactory.getLog(DatastarObjectProperties.class);   
 static HashMap<String, String> fieldToQuery = new HashMap<String, String>();
static HashMap<String, String> temporalCoverageFields = new HashMap<String, String>();
    public DatastarObjectProperties(RDFServiceFactory rdfServiceFactory){        
        super(queriesForDataset,rdfServiceFactory);
    }
      
  protected static final String prefix = 
        "prefix owl: <http://www.w3.org/2002/07/owl#> "
      + " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
      + " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
      + " prefix core: <http://vivoweb.org/ontology/core#>  "
      + " prefix foaf: <http://xmlns.com/foaf/0.1/> "
      + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
      + " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
      + " prefix bibo: <http://purl.org/ontology/bibo/>  " 
      + " prefix datastarCore: <http://purl.org/datastar/> "
      + " prefix obo: <http://purl.obolibrary.org/obo/> \n" ;

  
  @Override
  //Add BOTH to alltext AND to Field specifically for this using the dynamic field property
  public void modifyDocument(Individual individual, SolrInputDocument doc, StringBuffer addUri) { 
	  //This executes all the queries and adds all values to the alltext
     super.modifyDocument(individual, doc, addUri);                     
     //In addition, we would like to execute each query separately for each 
     //object property
     addDynamicField(individual, doc, addUri);
  }
  
  private void addDynamicField(Individual individual, SolrInputDocument doc, StringBuffer addUri) {
	  if( individual == null )
          return;
      
	  for(String fieldname:fieldToQuery.keySet()) {
		  //Execute each query separately
		  String q = fieldToQuery.get(fieldname);
		  //
	      List<String> values = executeQueryForDynamicFieldValues(individual, Arrays.asList(new String[]{q}));        
	      
	      SolrInputField field = doc.getField(fieldname);
	      if( field == null ){
	    	  for(String value: values) {
	    		  doc.addField(fieldname, value);
	    	  }
	      }else{
	    	  for(String value: values) {
	    		  field.addValue(value, field.getBoost());
	    	  }
	      }
	  }
	  
	  //Add date time fields as well here
	  addDynamicDateTimeFields(individual, doc, addUri);
  }

  

//Execute query for dynamic values
  protected List<String> executeQueryForDynamicFieldValues( Individual individual, Collection<String> queries){
	  /* execute all the queries on the list and concat the values to add to all text */        
	RDFService rdfService = rdfServiceFactory.getRDFService();
    List<String> allValues = new ArrayList<String>();
    for(String query : queries ){    
    	
        String subInUriQuery = 
    		query.replaceAll("\\?uri", "<" + individual.getURI() + "> ");
    	log.debug("Subbed in URI query: " + subInUriQuery);
        try{
        	
        	ResultSet results = RDFServiceUtils.sparqlSelectQuery(subInUriQuery, rdfService);               
        	while(results.hasNext()){         
        		//values from a row will not have a space between them or have a space added
        		//In this case, we are only asking for one variable in the result set so won't have more than one varname per row
                String value =  getTextForRow( results.nextSolution(), false )  ;
                log.debug("Value is " + value);
                //Not interested in empty strings
                if(!StringUtils.isBlank(value)) {
                	log.debug("Value is not blank so being added to allValues list");
                	allValues.add(value ) ; 
                }
        	}
        	
        }catch(Throwable t){
            if( ! shutdown ) 
                log.error(t,t);
        } 
        
        if(log.isDebugEnabled()){
            log.debug("query: '" + subInUriQuery+ "'");
            log.debug("text for query: '" + allValues.toString() + "'");
        }
    }
    
    rdfService.close();
    return allValues;    
}       

  //There are multiple ways of handling this, but in our case, we want every year the temporal coverage is applicable
  //to show up in the results
  //If we were only interested in the RANGE then we could easily index just the start and end (or single value) dates and then
  //employ facet range between the beginning of time and the current time to get all range counts for each year
  //But that method would not provide us any of the dates that fall between the start and current time
  
  private void addDynamicDateTimeFields(Individual individual,
			SolrInputDocument doc, StringBuffer addUri) {
		 String dateValueQuery = temporalCoverageFields.get("dateValue");
		 String intervalQuery = temporalCoverageFields.get("dateInterval");
		 //Should really just be on evalue
		 List<Date> temporalDateValues = executeQueryForTemporalCoverageDateValues(individual, dateValueQuery);
		 List<HashMap<String, Date>> temporalIntervalValues = executeQueryForTemporalCoverageIntervalValues(individual, intervalQuery);

		 //Add date value but only interested in year so using tint
		 //Consider single value same as start? As well as end?
		 String startField = "temporalCoverageStart_tint";
		 String endField = "temporalCoverageEnd_tint";
		 String allDatesField = "temporalCoverageAll_text"; //delimited string that will contain all the years represented by a range or single point
		 
		
	     //We're interested only in the years for now, could consider how to expand this to dates later
	     //but currently no way to dynamically get date ranges based on values that actually exist
	     //We're adding a separate field with ALL of the values, whether start or end years, as well as the years between them?
		 
	     //For single value dates
		 List<Integer> dateYears = new ArrayList<Integer>();
	     for(Date d: temporalDateValues) {
	    	 if(d != null) {
	    		 dateYears.add(new Integer(getYearForDate(d)));
	    	 }
   	  	}
	     
	     //Add to start field
	    addDateFieldsToDoc(startField, dateYears, doc);
	    //Add to allDatesField
	    //addDateFieldsToDoc(allDatesField, dateYears, doc);
	     //For interval dates
	     
	    List<Integer> intervalAllYears = new ArrayList<Integer>();
	    List<Integer> startYears = new ArrayList<Integer>();
	    List<Integer> endYears = new ArrayList<Integer>();
	     for(HashMap<String, Date> h: temporalIntervalValues) {
	    	 Date startDate = h.get("start");
	    	 Date endDate = h.get("end");
	    	 if(startDate != null && endDate != null) {
	    		 int startYear = getYearForDate(startDate);
	    		 int endYear = getYearForDate(endDate);
	    		 startYears.add(new Integer(startYear));
	    		 endYears.add(new Integer(endYear));
	    		 //Add all years, including start and end and years in between
	    		 for(int y= startYear; y <= endYear; y++) {
	    			 intervalAllYears.add(new Integer(y));
	    		 }
	    	 }
	     }
	     
	     addDateFieldsToDoc(startField, startYears, doc);
	     addDateFieldsToDoc(endField, endYears, doc);
	     //to add all dates as string to all dates field, use both single value dates as well as intervals
	     dateYears.addAll(intervalAllYears);
	     addDateFieldsAsStringToDoc(allDatesField, dateYears, doc);
	}  
  
  //Multiple date field names so using this to handle them
  private void addDateFieldsToDoc(String fieldName, List<Integer> dateYears, SolrInputDocument doc) {
	  SolrInputField field = doc.getField(fieldName);
      if( field == null ){    	 
    	  for(Integer i: dateYears) {
    		doc.addField(fieldName, i.intValue());
    	  }
      }else{
    	  for(Integer i: dateYears) {
    		  field.addValue(i.intValue(), field.getBoost());
      	  }
      }
  }
  
  private int getYearForDate(Date d) {
	  Calendar calendar = Calendar.getInstance();
	  calendar.setTime(d);
	  return calendar.get(Calendar.YEAR);
  }
    
  private void addDateFieldsAsStringToDoc(String fieldName, List<Integer> dateYears, SolrInputDocument doc) {
	  SolrInputField field = doc.getField(fieldName);
	  //Create string representation of date
	  String allDates = StringUtils.join(dateYears, " ");
      if( field == null ){    	 
    		doc.addField(fieldName, allDates);
      }else{
    	  field.addValue(allDates, field.getBoost());
      	  
      }
  }
  
  //should return values for both start and end dates, with hashmap where key = appropriate field name
  private List<HashMap<String, Date>> executeQueryForTemporalCoverageIntervalValues(
		Individual individual, String intervalQuery) {
	// TODO Auto-generated method stub
	  List<HashMap<String, Date>> intervalsList = new ArrayList<HashMap<String, Date>>();
	RDFService rdfService = rdfServiceFactory.getRDFService();
    
        String subInUriQuery = 
    		intervalQuery.replaceAll("\\?uri", "<" + individual.getURI() + "> ");
    	log.debug("Subbed in URI query: " + subInUriQuery);
        try{
        	
        	ResultSet results = RDFServiceUtils.sparqlSelectQuery(subInUriQuery, rdfService);               
        	while(results.hasNext()){         
        		//values from a row will not have a space between them or have a space added
        		//In this case, we are only asking for one variable in the result set so won't have more than one varname per row
        		//TODO: HAndle date time values and also in case of interval multiple years coming back
        		QuerySolution qs = results.nextSolution();
        		Date startDateValue =  getDateRow( qs, "startDateTimeValue", false )  ;
        		Date endDateValue = getDateRow( qs, "endDateTimeValue", false )  ;
        		
        		
                //Not interested in empty strings
                if(startDateValue != null && endDateValue != null) {
                	intervalsList.add(new HashMap<String, Date>());
                	//get last added hashmap to populate
                	HashMap<String, Date> fieldToDate = intervalsList.get(intervalsList.size() - 1);
                	log.debug("Value is not blank so being added to allValues list");
                	fieldToDate.put("start", startDateValue);
                	fieldToDate.put("end", endDateValue);
                }
        	}
        	
        }catch(Throwable t){
            if( ! shutdown ) 
                log.error(t,t);
        } 
        
        if(log.isDebugEnabled()){
            log.debug("query: '" + subInUriQuery+ "'");
            
        }
    
    rdfService.close();
     
	return intervalsList;
}



//This is specific handling for temporal coverage values
  //Execute query for dynamic values
  //This is a single value so should be fine
  protected List<Date> executeQueryForTemporalCoverageDateValues( Individual individual, String query){
	  /* execute all the queries on the list and concat the values to add to all text */        
	RDFService rdfService = rdfServiceFactory.getRDFService();
    List<Date> allValues = new ArrayList<Date>();
    
        String subInUriQuery = 
    		query.replaceAll("\\?uri", "<" + individual.getURI() + "> ");
    	log.debug("Subbed in URI query: " + subInUriQuery);
        try{
        	
        	ResultSet results = RDFServiceUtils.sparqlSelectQuery(subInUriQuery, rdfService);               
        	while(results.hasNext()){         
        		//values from a row will not have a space between them or have a space added
        		//In this case, we are only asking for one variable in the result set so won't have more than one varname per row
        		//TODO: HAndle date time values and also in case of interval multiple years coming back
        		Date dateValue =  getDateRow( results.nextSolution(), false )  ;
                log.debug("Value is " + dateValue.toString());
                //Not interested in empty strings
                if(dateValue != null) {
                	log.debug("Value is not blank so being added to allValues list");
                	allValues.add(dateValue ) ; 
                }
        	}
        	
        }catch(Throwable t){
            if( ! shutdown ) 
                log.error(t,t);
        } 
        
        if(log.isDebugEnabled()){
            log.debug("query: '" + subInUriQuery+ "'");
            log.debug("text for query: '" + allValues.toString() + "'");
        }
    
    rdfService.close();
    return allValues;    
}      
  
  //This will get the date literal and then convert it to time to store in the field?
  //This handles the case where there is only one value per row and really just one row
   protected Date getDateRow( QuerySolution row, boolean addSpace){
	   Date dt = null;
        if( row == null )
            return null;
//new DateTime( dtValue.get(0).getLexicalForm() );
        Iterator<String> iter =  row.varNames() ;
        while( iter.hasNext()){
            String name = iter.next();
            dt = getDateRow(row, name, addSpace);                  
        } 
        return dt;
    }
   
   protected Date getDateRow( QuerySolution qs, String name, boolean addSpace){
	   Date dt = null;
        if( qs == null )
        	return null;
        RDFNode node = qs.get( name );
        if( node != null &&  node.isLiteral()){
        	//Here we assume the value is a date time value
        	DateTime dateTime = new DateTime( node.asLiteral().getLexicalForm());
        	dt = dateTime.toDate();
        }else{
            log.debug(name + " is null");
        }                        
       
        return dt;
    }


//queries 
  static {
	  String geographicLocationQuery = prefix +
	            "SELECT " +
	            "(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
	            //" ?uri rdf:type datastarCore:Dataset.  " +
	            " ?uri datastarCore:geographicCoverage ?location . " +
	            " ?location rdfs:label ?ContextNodeProperty . }";
	  String subjectAreaQuery = prefix +
	            "SELECT " +
	            "(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
	            //" ?uri rdf:type datastarCore:Dataset.  " +
	            " ?uri core:hasSubjectArea ?subjectArea . " +
	            " ?subjectArea rdfs:label ?ContextNodeProperty . }";
	  String keywordQuery = prefix +
	            "SELECT " +
	            "(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
	            //" ?uri rdf:type datastarCore:Dataset.  " +
	            " ?uri core:freetextKeyword ?ContextNodeProperty .  }";
	  //Author query: copied from VivoInformationResourceContextNodeFields
	  //TODO: Find way to access this
	  //TODO: check if author being a person is necessary because it may be
	  //an organization or other?
	 
	  //copied VivoAgentContextNode, instead of just picking datasets, extending to information content entities to
	  //make more general
	  //Updating here so that we can get the label of the author and NOT the label 
	  //of the dataset
	  //And also really looking at uris which are information entities
	  String authorQuery =
			  prefix +        "SELECT " +
			            "(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
			            " ?uri rdf:type obo:IAO_0000030  ; ?b ?c . " +
			            " ?c rdf:type core:Authorship . " +
			            " ?c core:relates ?h . " +
			            " ?h rdf:type foaf:Agent . ?h rdfs:label ?ContextNodeProperty . }";
			  
         
	  //Temporal coverage queries
	  //Could convert these to a union query as well
	 
	  String temporalCoverageDateValueQuery = prefix +
	            "SELECT " +
	            "?dateTimeValue WHERE {" +
	            " ?uri datastarCore:temporalCoverage ?tempCoverage . " +
	            " ?tempCoverage rdf:type core:DateTimeValue . " + 
	            " ?tempCoverage core:dateTime ?dateTimeValue ." + 
	            "}";
	  String temporalCoverageDateIntervalQuery = prefix +
	            "SELECT " +
	            "?startDateTimeValue ?endDateTimeValue WHERE {" +
	            " ?uri datastarCore:temporalCoverage ?tempCoverage . " +
	            " ?tempCoverage rdf:type core:DateTimeInterval . " + 
	            " ?tempCoverage core:start ?startValue . " + 
	            " ?startValue core:dateTime ?startDateTimeValue ." + 
	            " ?tempCoverage core:end ?endValue . " + 
	            " ?endValue core:dateTime ?endDateTimeValue ." + 
	            "}";
	            
	  //Add queries - these will be executed to add field values to all text
	  //Subject area is already added in Vitro/VIVO to AllText so does not need to be added here as well
	  queriesForDataset.add(geographicLocationQuery); 
	  //Fieldname to query for dynamic fields
	  //not adding keyword but keyword does not appear to be added in regular setup?
	  fieldToQuery.put("geographiclocation_string", geographicLocationQuery);
	  fieldToQuery.put("subjectarea_string", subjectAreaQuery);
	  fieldToQuery.put("keyword_string", keywordQuery);
	  fieldToQuery.put("author_string", authorQuery);
	  //fieldToQuery.put("temporalcoverage_tdate", temporalCoverageQuery);
	temporalCoverageFields.put("dateValue", temporalCoverageDateValueQuery);
	temporalCoverageFields.put("dateInterval", temporalCoverageDateIntervalQuery);

  }
}
