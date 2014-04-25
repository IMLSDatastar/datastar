/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.DatastarVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 * This is an odd controller that is just drawing a page with links on it.
 * It is not an example of the normal use of the RDF editing system and
 * was just migrated over from an odd use of the JSP RDF editing system
 * during the 1.4 release. 
 * 
 * This mainly sets up pageData for the template to use.
 */
public class ManageWebpagesForDatasetGenerator extends ManageWebpagesForIndividualGenerator implements EditConfigurationGenerator {
    public static Log log = LogFactory.getLog(ManageWebpagesForDatasetGenerator.class);
    static String datastarCore = "http://purl.org/datastar/";

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
    	 EditConfigurationVTwo config = super.getEditConfiguration(vreq, session);
         List<Map<String,String>> metadataWebpages = getMetadataWebpages(config.getSubjectUri(), vreq);
    	 config.addFormSpecificData("metadataWebpages",metadataWebpages);
    	 config.addFormSpecificData("associatedMetadataPredicate", DatastarVocabulary.DatastarNS + "hasAssociatedMetadata");
    	 //Add form specific data: metadataEditForm link
         ParamMap paramMap = new ParamMap();
         paramMap.put("subjectUri", config.getSubjectUri());
         paramMap.put("editForm", this.getMetadataEditForm());
         paramMap.put("view", "form");
         String path = UrlBuilder.getUrl( UrlBuilder.Route.EDIT_REQUEST_DISPATCH ,paramMap);

         config.addFormSpecificData("baseEditMetadataWebpageUrl", path);                 

         paramMap = new ParamMap();
         paramMap.put("subjectUri", config.getSubjectUri());
         paramMap.put("predicateUri", config.getPredicateUri());
         paramMap.put("editForm" , this.getMetadataEditForm() );
         paramMap.put("cancelTo", "manage");
         path = UrlBuilder.getUrl( UrlBuilder.Route.EDIT_REQUEST_DISPATCH ,paramMap);

         config.addFormSpecificData("showAddMetadataFormUrl", path);          

    	 
    	 
    	 
    	 return config;
    }
    
    
    @Override
    //Putting this into a method allows overriding it in subclasses
    protected String getEditForm() {
    	return AddEditDatasetWebpageFormGenerator.class.getName();
    }
    

    //for metadata
    protected String getMetadataEditForm() {
    	return AddEditMetadataWebpageFormGenerator.class.getName();
    }
    
    
    private static String WEBPAGE_QUERY = ""
            + "PREFIX core: <http://vivoweb.org/ontology/core#> \n"
            + "PREFIX vcard: <http://www.w3.org/2006/vcard/ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
            + "PREFIX datastarCore: <http://purl.org/datastar/> \n"
            + "SELECT DISTINCT ?vcard ?link ?url ?label ?rank ?format WHERE { \n"
            + "    ?subject <http://purl.obolibrary.org/obo/ARG_2000028> ?vcard . \n"
            + "    ?vcard vcard:hasURL ?link . \n"
            + "    ?link a vcard:URL . \n"
            + "    ?link a <" + getDataWebpageURLLinkClass() + "> .\n"
            + "    OPTIONAL { ?link vcard:url ?url } \n"
            + "    OPTIONAL { ?link rdfs:label ?label } \n"
            + "    OPTIONAL { ?link core:rank ?rank } \n"
            + "    OPTIONAL { ?link datastarCore:dataFileFormat ?format } \n"
            + "} ORDER BY ?rank";
            
            
            
    
    //Also need to include metadata webpage query
    private static String METADATA_WEBPAGE_QUERY = ""
            + "PREFIX core: <http://vivoweb.org/ontology/core#> \n"
            + "PREFIX datastarCore: <http://purl.org/datastar/> \n"
            + "PREFIX vcard: <http://www.w3.org/2006/vcard/ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
            + "SELECT DISTINCT ?metadata ?vcard ?link ?url ?label ?rank ?format WHERE { \n"
            + "    ?subject datastarCore:hasAssociatedMetadata ?metadata. \n"  
            + "    ?metadata <http://purl.obolibrary.org/obo/ARG_2000028> ?vcard . \n"
            + "    ?vcard vcard:hasURL ?link . \n"
            + "    ?link a vcard:URL . \n"
            + "    ?link a <" + getMetadataURLLinkClass() + "> .\n"
            + "    OPTIONAL { ?link vcard:url ?url } \n"
            + "    OPTIONAL { ?link rdfs:label ?label } \n"
            + "    OPTIONAL { ?link core:rank ?rank } \n"
            + "    OPTIONAL { ?link datastarCore:associatedMetadataFormat ?format } \n"
            + "} ORDER BY ?rank";
    
    @Override
    protected String getQuery() {
    	return WEBPAGE_QUERY;
    }
    
    //Get query for metadata
    private String getMetadataQuery() {
    	return METADATA_WEBPAGE_QUERY;
    }
    
    private List<Map<String, String>> getMetadataWebpages(String subjectUri, VitroRequest vreq) {
        
        String queryStr = QueryUtils.subUriForQueryVar(this.getMetadataQuery(), "subject", subjectUri);
        log.debug("Query string is: " + queryStr);
        List<Map<String, String>> webpages = new ArrayList<Map<String, String>>();
        try {
            ResultSet results = QueryUtils.getQueryResults(queryStr, vreq);
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                RDFNode node = soln.get("link");
                if (node.isURIResource()) {
                    webpages.add(QueryUtils.querySolutionToStringValueMap(soln));        
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        }    
        
        return webpages;
    }
    
    @Override
    protected String getTemplate() {
    	return "manageDatasetWebpagesForIndividual.ftl";
    }
    
    protected static String getDataWebpageURLLinkClass() {
    	return datastarCore + "DatasetURLLink";
    }
    protected static String getMetadataURLLinkClass() {
    	return datastarCore + "MetadataURLLink";
    }
}
