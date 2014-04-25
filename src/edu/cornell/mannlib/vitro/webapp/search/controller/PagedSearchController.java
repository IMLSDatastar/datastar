/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONException;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DatastarVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.IndexConstants;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.searchresult.IndividualSearchResult;
import edu.ucsf.vitro.opensocial.OpenSocialManager;

/**
 * Paged search controller that uses Solr
 *  
 * @author bdc34, rjy7
 * 
 */

public class PagedSearchController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(PagedSearchController.class);
    
    protected static final int DEFAULT_HITS_PER_PAGE = 25;
    protected static final int DEFAULT_MAX_HIT_COUNT = 1000;   

    private static final String PARAM_XML_REQUEST = "xml";
    private static final String PARAM_CSV_REQUEST = "csv";
    private static final String PARAM_START_INDEX = "startIndex";
    private static final String PARAM_HITS_PER_PAGE = "hitsPerPage";
    private static final String PARAM_CLASSGROUP = "classgroup";
    private static final String PARAM_RDFTYPE = "type";
    private static final String PARAM_QUERY_TEXT = "querytext";

    //For default classgroup parameter setting
    private static final String DEFAULT_CLASSGROUP_PARAM = "http://vivoweb.org/ontology#vitroClassGrouppublications";
    protected static final Map<Format,Map<Result,String>> templateTable;

    protected enum Format { 
        HTML, XML, CSV; 
    }
    
    protected enum Result {
        PAGED, ERROR, BAD_QUERY         
    }

    
    static{
        templateTable = setupTemplateTable();
    }
         
    /**
     * Overriding doGet from FreemarkerHttpController to do a page template (as
     * opposed to body template) style output for XML requests.
     * 
     * This follows the pattern in AutocompleteController.java.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    	
        VitroRequest vreq = new VitroRequest(request);
        boolean wasXmlRequested = isRequestedFormatXml(vreq);
        boolean wasCSVRequested = isRequestedFormatCSV(vreq);
        
        if( !wasXmlRequested && !wasCSVRequested){
            super.doGet(vreq,response);
        }else if (wasXmlRequested){
            try {                
                ResponseValues rvalues = processRequest(vreq);
                
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/xml;charset=UTF-8");
                writeTemplate(rvalues.getTemplateName(), rvalues.getMap(), request, response);
            } catch (Exception e) {
                log.error(e, e);
            }
        } else if (wasCSVRequested){
          	try {                
                ResponseValues rvalues = processRequest(vreq);
                
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/csv;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=search.csv");
                writeTemplate(rvalues.getTemplateName(), rvalues.getMap(), request, response);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {    	    	
    	
        //There may be other non-html formats in the future
        Format format = getFormat(vreq);            
        
        try {
            
            //make sure an IndividualDao is available 
            if( vreq.getWebappDaoFactory() == null 
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("Could not get webappDaoFactory or IndividualDao");
                throw new Exception("Could not access model.");
            }
            IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
            ApplicationBean appBean = vreq.getAppBean();
            
            log.debug("IndividualDao is " + iDao.toString());
            
            int startIndex = getStartIndex(vreq);            
            int hitsPerPage = getHitsPerPage( vreq );           

            String queryText = vreq.getParameter(VitroQuery.QUERY_PARAMETER_NAME);  
            log.debug("Query text is \""+ queryText + "\""); 


            String badQueryMsg = badQueryText( queryText );
            if( badQueryMsg != null ){
                return doFailedSearch(badQueryMsg, queryText, format);
            }
                
            
            SolrQuery query = getQuery(queryText, hitsPerPage, startIndex, vreq);            
            SolrServer solr = SolrSetup.getSolrServer(getServletContext());
            QueryResponse response = null;           
            
            try {
                response = solr.query(query);
            } catch (Exception ex) {                
                String msg = makeBadSearchMessage(queryText, ex.getMessage());
                log.error("could not run Solr query",ex);
                return doFailedSearch(msg, queryText, format);              
            }
            
            if (response == null) {
                log.error("Search response was null");                                
                return doFailedSearch("The search request contained errors.", queryText, format);
            }
            
            SolrDocumentList docs = response.getResults();
            if (docs == null) {
                log.error("Document list for a search was null");                
                return doFailedSearch("The search request contained errors.", queryText,format);
            }
                       
            long hitCount = docs.getNumFound();
            log.debug("Number of hits = " + hitCount);
            if ( hitCount < 1 ) {                
                return doNoHits(queryText,format);
            }          
            
            
            List<Individual> individuals = this.getIndividualsFromResults(vreq, docs, response);          
    
            /* Compile the data for the templates */
            Map<String, Object> body = new HashMap<String, Object>();
            //get facet information used to populate the template (i.e. which facets selected etc.)
            this.processFacetsForTemplate(body, vreq, response);

            body.put("individuals", IndividualSearchResult
                    .getIndividualTemplateModels(individuals, vreq));

            body.put("querytext", queryText);
            body.put("title", queryText + " - " + appBean.getApplicationName()
                    + " Search Results");
            
            body.put("hitCount", hitCount);
            body.put("startIndex", startIndex);
            
            //paging links
            this.setupPagingLinks(vreq, body, startIndex, hitsPerPage, hitCount);

	        // VIVO OpenSocial Extension by UCSF
	        this.setupOpenSocial(vreq, individuals, body);
	        
	        String template = templateTable.get(format).get(Result.PAGED);
            
            return new TemplateResponseValues(template, body);
        } catch (Throwable e) {
            return doSearchError(e,format);
        }        
    }

    
    //Get individuals
    private List<Individual> getIndividualsFromResults(VitroRequest vreq, SolrDocumentList docs, QueryResponse response) {
    	IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
        log.debug("IndividualDao is " + iDao.toString());
    	List<Individual> individuals = new ArrayList<Individual>(docs.size());
        Iterator<SolrDocument> docIter = docs.iterator();
        while( docIter.hasNext() ){
            try {                                    
                SolrDocument doc = docIter.next();
                String uri = doc.get(VitroSearchTermNames.URI).toString();                    
                Individual ind = iDao.getIndividualByURI(uri);
                if(ind != null) {
                  ind.setSearchSnippet( getSnippet(doc, response) );
                  individuals.add(ind);
                }
            } catch(Exception e) {
                log.error("Problem getting usable individuals from search hits. ",e);
            }
        }
        return individuals;
    }
    
    //Setup paging links
    private void setupPagingLinks(VitroRequest vreq, Map<String, Object> body, int startIndex, int hitsPerPage, long hitCount) {
    	 //Process information needed to create paging links later
        ParamMap pagingLinkParams = this.setupPagingLinksParamMap(vreq);
    	body.put("pagingLinks", 
                getPagingLinks(startIndex, hitsPerPage, hitCount,  
                               vreq.getServletPath(),
                               pagingLinkParams));

        if (startIndex != 0) {
            body.put("prevPage", getPreviousPageLink(startIndex,
                    hitsPerPage, vreq.getServletPath(), pagingLinkParams));
        }
        if (startIndex < (hitCount - hitsPerPage)) {
            body.put("nextPage", getNextPageLink(startIndex, hitsPerPage,
                    vreq.getServletPath(), pagingLinkParams));
        }	
    }
    
    //setup open social
    private void setupOpenSocial(VitroRequest vreq, List<Individual> individuals, Map<String, Object> body) {
    	try {
	        OpenSocialManager openSocialManager = new OpenSocialManager(vreq, "search");
	        // put list of people found onto pubsub channel 
            // only turn this on for a people only search
            if ("http://vivoweb.org/ontology#vitroClassGrouppeople".equals(vreq.getParameter(PARAM_CLASSGROUP))) {
		        List<String> ids = OpenSocialManager.getOpenSocialId(individuals);
		        openSocialManager.setPubsubData(OpenSocialManager.JSON_PERSONID_CHANNEL, 
		        		OpenSocialManager.buildJSONPersonIds(ids, "" + ids.size() + " people found"));
            }
			// TODO put this in a better place to guarantee that it gets called at the proper time!
			openSocialManager.removePubsubGadgetsWithoutData();
	        body.put("openSocial", openSocialManager);
	        if (openSocialManager.isVisible()) {
	        	body.put("bodyOnload", "my.init();");
	        }
        } catch (IOException e) {
            log.error("IOException in doTemplate()", e);
        } catch (SQLException e) {
            log.error("SQLException in doTemplate()", e);
        }  catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    //Trying to get values for vitro request
    private void processFacetsForTemplate(Map<String, Object> body, VitroRequest vreq, QueryResponse response) {
          VClassGroupDao grpDao = vreq.getWebappDaoFactory().getVClassGroupDao();
          VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();
          log.debug(" Public classes in the classgroup are " + grpDao.getPublicGroupsWithVClasses().toString());
          log.debug("VClassDao is "+ vclassDao.toString() );            
          
          boolean wasHtmlRequested = this.getWasHtmlRequested(vreq);
          String queryText = this.getQueryText(vreq);
          this.processFacetsForTemplate(body, vreq, wasHtmlRequested, grpDao, vclassDao, response, queryText);
    }
    
    /*
     * Moving paging link params to own method
    private void processFacetsForTemplate(Map<String, Object> body, VitroRequest vreq, boolean wasHtmlRequested,
    		VClassGroupDao grpDao, VClassDao vclassDao,   QueryResponse response, String queryText,
    		ParamMap pagingLinkParams) {
*/
    private void processFacetsForTemplate(Map<String, Object> body, VitroRequest vreq, boolean wasHtmlRequested,
    		VClassGroupDao grpDao, VClassDao vclassDao,   QueryResponse response, String queryText) {
    	
    	
    	//Get facets and their respective counts
        //Passing in the count object list - this allows freemarker to sort them as list of hashes by value
        //enabling the highest value counts to be displayed first
        HashMap<String, List<Count>> facetValueCounts = handleFacetResults(response);
        //In case of facet ranges, which are currently not implemented
        //HashMap<String,List<Count>> facetRangeCounts = handleFacetRangeResults(response);
        //Put in facets
        body.put("facets", facetValueCounts);
        //body.put("facetRanges", facetRangeCounts);
        //Check what facets have been selected and pass that to body
        //and also put those facets in the paging link parameters
        //so iterating through pages will also get the correct response
    	HashMap<String, String> facetParamMap = this.getFacetParamMap(vreq);
        
    	body.put("facetParamMap", facetParamMap);

    	
        //Handle class group parameter 
        //if a specific parameter has been passed
        String classGroupParam = this.getClassGroupValue(vreq);
        
        /*vreq.getParameter(PARAM_CLASSGROUP);    
        boolean classGroupFilterRequested = !StringUtils.isEmpty(classGroupParam);
        if(!classGroupFilterRequested) {
        	//We want to use the default class group parameter to show types from that class group 
        	//when no classgroup is initially requested
        	classGroupParam = 	DEFAULT_CLASSGROUP_PARAM;
        }*/
        //put classgroup name in body
        if (!StringUtils.isEmpty(classGroupParam)) {
               VClassGroup grp = grpDao.getGroupByURI(classGroupParam);
               if (grp != null && grp.getPublicName() != null) {
                   body.put("classGroupName", grp.getPublicName());
                   body.put("classGroupUri", grp.getURI());
               }
         }
        
        	//if a particular type is requested, send type name to the template
           String typeParam = vreq.getParameter(PARAM_RDFTYPE);
           boolean typeFilterRequested = !StringUtils.isEmpty(typeParam);
           if (typeFilterRequested) {
               VClass type = vclassDao.getVClassByURI(typeParam);
               typeFilterRequested = true;
               if (type != null && type.getName() != null) {
                   body.put("typeName", type.getName());
                   //put VClass type in body as well
                   body.put("vclassType", type);
               }
           }
           
           //provide default class group param to template
           body.put("defaultClassGroupUri", DEFAULT_CLASSGROUP_PARAM);
           /* Add ClassGroup and type refinement links to body */
           if( wasHtmlRequested ){ 
        	   boolean classGroupFilterRequested = this.isClassGroupFilterRequested(vreq);
        	   //is there ever any situation where both can be selected?
        	   boolean isSelectedClassGroup = classGroupFilterRequested && !typeFilterRequested;
        	   body.put("isSelectedClassGroup", isSelectedClassGroup);
        	   //if a specific type is not requested, populate various classes for ?default classgroup? every classgroup?
        	   
        	   if(!typeFilterRequested) {
        		   body.put("classLinks", getVClassLinks(vclassDao,  response, queryText));  
                   body.put("classLinksMap", getVClassLinksMap(vclassDao,  response, queryText)); 
        	   }
        	   
        	   //if a specific classgroup has not been selected, then pick the default classgroup and
        	   //also get a list of the different classgroups possible
               if ( !classGroupFilterRequested && !typeFilterRequested ) {
                   // Search request includes no ClassGroup and no type
            	   // We have a default classgroup we would like to show so
            	   // need to show remaining classgroup refinement links AND types under default
            	   //classgroups
            	   //get links for all classgroups except default
            	   body.put("showDefaultClassGroupView", true);
            	   body.put("classGroupLinks", getClassGroupsLinks(grpDao, response, queryText));          
                   //body.put("classLinks", getVClassLinks(vclassDao,  response, queryText));  
                   //body.put("classLinksMap", getVClassLinksMap(vclassDao,  response, queryText));

               } 
               /*else if ( classGroupFilterRequested && !typeFilterRequested ) {
                   // Search request is for a ClassGroup, so add rdf:type search refinement links
                   // but try to filter out classes that are subclasses
                   body.put("classLinks", getVClassLinks(vclassDao,  response, queryText));                       
                   body.put("classLinksMap", getVClassLinksMap(vclassDao,  response, queryText));
                   //indicate that a parameter for classgroup was passed and we aren't relying on the default
                   
               } */        
          }           

    }
    
    
    private int getHitsPerPage(VitroRequest vreq) {
        int hitsPerPage = DEFAULT_HITS_PER_PAGE;
        try{ 
            hitsPerPage = Integer.parseInt(vreq.getParameter(PARAM_HITS_PER_PAGE)); 
        } catch (Throwable e) { 
            hitsPerPage = DEFAULT_HITS_PER_PAGE; 
        }                        
        log.debug("hitsPerPage is " + hitsPerPage);  
        return hitsPerPage;
    }

    private int getStartIndex(VitroRequest vreq) {
        int startIndex = 0;
        try{ 
            startIndex = Integer.parseInt(vreq.getParameter(PARAM_START_INDEX)); 
        }catch (Throwable e) { 
            startIndex = 0; 
        }            
        log.debug("startIndex is " + startIndex);
        return startIndex;
    }

    private String badQueryText(String qtxt) {
        if( qtxt == null || "".equals( qtxt.trim() ) )
            return "Please enter a search term.";
        
        if( qtxt.equals("*:*") )
            return "Search term was invalid" ;
        
        return null;
    }

    /**
     * Get the class groups represented for the individuals in the documents.
     * @param qtxt 
     */
    private List<VClassGroupSearchLink> getClassGroupsLinks(VClassGroupDao grpDao, QueryResponse rsp, String qtxt) {                                 
        Map<String,Long> cgURItoCount = new HashMap<String,Long>();
        List<VClassGroup> classgroups = new ArrayList<VClassGroup>( );
        //List<FacetField> ffs = rsp.getFacetFields();
        //instead of iterating over all facet fields, get the class group one
        FacetField ff = rsp.getFacetField(VitroSearchTermNames.CLASSGROUP_URI);

        if(ff != null) {
            List<Count> counts = ff.getValues();
            for( Count ct: counts){                    
                VClassGroup vcg = grpDao.getGroupByURI( ct.getName() );
                if( vcg == null ){
                    log.debug("could not get classgroup for URI " + ct.getName());
                }else{
                    classgroups.add(vcg);
                    cgURItoCount.put(vcg.getURI(),  ct.getCount());
                }                    
            }                
        }
        
        grpDao.sortGroupList(classgroups);     
        
        List<VClassGroupSearchLink> classGroupLinks = new ArrayList<VClassGroupSearchLink>(classgroups.size());
        for (VClassGroup vcg : classgroups) {
            long count = cgURItoCount.get( vcg.getURI() );
            if (vcg.getPublicName() != null && count > 0 )  {
                classGroupLinks.add(new VClassGroupSearchLink(qtxt, vcg, count));
            }
        }
        return classGroupLinks;
    }
    
    private List<VClassSearchLink> getVClassLinks(VClassDao vclassDao,  QueryResponse rsp, String qtxt) {
        List<VClass> classes = new ArrayList<VClass>();
        Map<String,Long> typeURItoCount = new HashMap<String,Long>();        
        
        FacetField ff = rsp.getFacetField(VitroSearchTermNames.RDFTYPE);
        List<Count> counts = ff.getValues();
        for( Count ct: counts){  
            String typeUri = ct.getName();
            long count = ct.getCount();
            try{                                                   
                if( VitroVocabulary.OWL_THING.equals(typeUri) ||
                    count == 0 )
                    continue;
                VClass type = vclassDao.getVClassByURI(typeUri);
                if(type != null && ! type.isAnonymous() &&
                        type.getName() != null && !"".equals(type.getName())) {
                	String typeGroupUri = type.getGroupURI();
                	if(typeGroupUri != null) {
                		
                		typeURItoCount.put(typeUri,count);
                        classes.add(type);
                	}
                			
                }
            }catch(Exception ex){
                if( log.isDebugEnabled() )
                    log.debug("could not add type " + typeUri, ex);
            }                                                
        }                
                        
       
        
        //the old sort used VClass's own compare
        //we would like to sort by count instead
        /*
        Collections.sort(classes, new Comparator<VClass>(){
            public int compare(VClass o1, VClass o2) {                
                //return o1.compareTo(o2);
            	long o1Count = typeURItoCount.get(o1.getURI());
            	long o2Count = typeURItoCount.get(o2.getURI());
            	return o1Count > o2Count?1: -1;
            }});
        */
        List<VClassSearchLink> vClassLinks = new ArrayList<VClassSearchLink>(classes.size());
        for (VClass vc : classes) {                        
            long count = typeURItoCount.get(vc.getURI());
            vClassLinks.add(new VClassSearchLink(qtxt, vc, count ));
        }
        
        return vClassLinks;
    }       
    
    /**Get VClassGroup given uri **/
    private VClassGroup getVClassGroupForURI(VClassGroupDao grpDao, String classGroupUri) {
    	VClassGroup vcg = grpDao.getGroupByURI( classGroupUri );
    	return vcg;
    }
    
   
    
    /**
     * Returns map with class group associated with VClass links
     * @param vclassDao
     * @param rsp
     * @param qtxt
     * @return
     */
    
    private Map<String, List<VClassSearchLink>> getVClassLinksMap(VClassDao vclassDao,  QueryResponse rsp, String qtxt) {
    	HashMap<String, List<VClassSearchLink>> classGroupToClassMap = new HashMap<String, List<VClassSearchLink>>();
        //List<VClass> classes = new ArrayList<VClass>();
        //Map<String,Long> typeURItoCount = new HashMap<String,Long>();        
        
        FacetField ff = rsp.getFacetField(VitroSearchTermNames.RDFTYPE);
        List<Count> counts = ff.getValues();
        for( Count ct: counts){  
            String typeUri = ct.getName();
            long count = ct.getCount();
            try{                                                   
                if( VitroVocabulary.OWL_THING.equals(typeUri) ||
                    count == 0 )
                    continue;
                VClass type = vclassDao.getVClassByURI(typeUri);
                if(type != null && ! type.isAnonymous() &&
                        type.getName() != null && !"".equals(type.getName())) {
                	String typeGroupUri = type.getGroupURI();
                	if(typeGroupUri != null) {
                		if(!classGroupToClassMap.containsKey(typeGroupUri)) {
                			classGroupToClassMap.put(typeGroupUri, new ArrayList<VClassSearchLink> ());
                		}
                		List<VClassSearchLink> vClassLinks = classGroupToClassMap.get(typeGroupUri);
                		vClassLinks.add(new VClassSearchLink(qtxt, type, count ));
                		//typeURItoCount.put(typeUri,count);
                        //classes.add(type);
                	}
                			
                }
            }catch(Exception ex){
                if( log.isDebugEnabled() )
                    log.debug("could not add type " + typeUri, ex);
            }                                                
        }                
                        
        //sort vclass links
        Set<String> classGroupUris = classGroupToClassMap.keySet();
        for(String classGroupUri: classGroupUris) {
        	List<VClassSearchLink> vClassLinks = classGroupToClassMap.get(classGroupUri);
        	Collections.sort(vClassLinks, new Comparator<VClassSearchLink>() {
        		public int compare(VClassSearchLink c1, VClassSearchLink c2) {
        			long count1 = c1.count;
        			long count2 = c2.count;
        			return (count2 > count1)?1: -1;
        			
        		}
        	});
        }
        
        //the old sort used VClass's own compare
        //we would like to sort by count instead
        /*
        Collections.sort(classes, new Comparator<VClass>(){
            public int compare(VClass o1, VClass o2) {                
                //return o1.compareTo(o2);
            	long o1Count = typeURItoCount.get(o1.getURI());
            	long o2Count = typeURItoCount.get(o2.getURI());
            	return o1Count > o2Count?1: -1;
            }});
        */
        /*List<VClassSearchLink> vClassLinks = new ArrayList<VClassSearchLink>(classes.size());
        for (VClass vc : classes) {                        
            long count = typeURItoCount.get(vc.getURI());
            vClassLinks.add(new VClassSearchLink(qtxt, vc, count ));
        }*/
        
        return classGroupToClassMap;
    }       
    
    
    private String getSnippet(SolrDocument doc, QueryResponse response) {
        String docId = doc.get(VitroSearchTermNames.DOCID).toString();
        StringBuffer text = new StringBuffer();
        Map<String, Map<String, List<String>>> highlights = response.getHighlighting();
        if (highlights != null && highlights.get(docId) != null) {
            List<String> snippets = highlights.get(docId).get(VitroSearchTermNames.ALLTEXT);
            if (snippets != null && snippets.size() > 0) {
                text.append("... " + snippets.get(0) + " ...");
            }       
        }
        return text.toString();
    }       

    private SolrQuery getQuery(String queryText, int hitsPerPage, int startIndex, VitroRequest vreq) {
        // Lowercase the search term to support wildcard searches: Solr applies no text
        // processing to a wildcard search term.
        SolrQuery query = new SolrQuery( queryText );
        
        query.setStart( startIndex )
             .setRows(hitsPerPage);

       //Based on parameters, had type/class group or other facets to query
       this.handleParametersForQuery(query, vreq);
        
        //Adding facets for search
        this.addFacetsForSearch(query);
        
        log.debug("Query = " + query.toString());
        return query;
    }   
    
    //Handle parameters, including facet parameters
    private void handleParametersForQuery(SolrQuery query, VitroRequest vreq) {
    	 // ClassGroup filtering param
        String classgroupParam = (String) vreq.getParameter(PARAM_CLASSGROUP);
        
        // rdf:type filtering param
        String typeParam = (String) vreq.getParameter(PARAM_RDFTYPE);
        
        //Facet parameter
        //Indicates that one or more facet parameters is being sent out
        String facetParam = (String) vreq.getParameter("facetParam");
        //These values will be true no matter since we will always be checking type parameters
        //except in the case where a specific type is selected
        query.add("facet", "true");
        query.add("facet.limit", "-1");
        //Type will always be added as a facet except in the case where a specific type is selected
        if(! StringUtils.isBlank(typeParam)) {
        	  log.debug("Firing type query ");
              log.debug("request.getParameter(type) is "+ typeParam);   
              query.addFilterQuery(VitroSearchTermNames.RDFTYPE + ":\"" + typeParam + "\"");
        	
        } else {
        	//if a specific type has not been requested, return result facets for types
            query.add("facet.field",VitroSearchTermNames.RDFTYPE);
            
            //if class group param exists, then use it, otherwise employ a default classgroup param for research
            //we don't need to use the classgroup parameter in case the type parameter is set
            if (  StringUtils.isBlank(classgroupParam) ) {
            	//set class group param to research/publications
            	log.debug("No classgroup parameter set so employing default research/publications class group");
            	classgroupParam =	DEFAULT_CLASSGROUP_PARAM;
                //We would also to add class group facets so we can display additional class group options
            	//We do NOT want to filter results by default class group when no parameter is selected, but just
            	//display types under default class group and the remaining class groups
                query.add("facet.field",VitroSearchTermNames.CLASSGROUP_URI);        
            } else {
            	//a class group parameter has been selected and we want to filter requests by this parameter
                query.addFilterQuery(VitroSearchTermNames.CLASSGROUP_URI + ":\"" + classgroupParam + "\"");
            	log.debug("Classgroup parameter exists and is " + classgroupParam);
            }
            
        } 
        
        //parameters other than type will always be added and expected
        if( !StringUtils.isBlank(facetParam)) {
        	this.handleFacetParameters(query, vreq);
        }       
        
        //Can also pass along filter queries that are specified in the url
        //supports specific range filter queries
        this.addExplicitFilterQueriesForSearch(query, vreq);
        	
    }
    
    //Handle facet parameters specifically and add to query
    private void handleFacetParameters(SolrQuery query, VitroRequest vreq) {
    	//type is added always except when a specific type is selected, so that is dealt with in the code above
    	HashMap<String, String> facetParamMap = this.getFacetParamMap(vreq);
    	
    	for(String paramName: facetParamMap.keySet()) {
    		String param = facetParamMap.get(paramName);
    		//Trying 'tagging' and exclusion to see if this works as required
    		//query.addFilterQuery("{!tag=" + paramName + "}" + paramName + ":" + param);
    		query.addFilterQuery(paramName + ":" + param);
    	}
    }
    
    //Get the list for which facet parameters need to be checked
    private List<String> getFacetParamNames() {
    	List<String> paramNames = new ArrayList<String>(Arrays.asList(new String[]{"geographiclocation_string", "subjectarea_string", "keyword_string", "author_string", "temporalCoverageStart_tint", "temporalCoverageEnd_tint", "temporalCoverageAll_text"}));
    	return paramNames;
    }
   
    
    //Returns hash with facet parameters that do exist and the assigned values
    private HashMap<String, String> getFacetParamMap(VitroRequest vreq) {
    	HashMap<String, String> facetParamMap = new HashMap<String, String>();
    	List<String> paramNames = this.getFacetParamNames();
    	for(String paramName: paramNames) {
    		String param = vreq.getParameter(paramName);
    		if(!StringUtils.isBlank(param)) {
    			facetParamMap.put(paramName, param);
    		}
    	}
    	return facetParamMap;
    }
    //Adding facets to search query, also includes facet ranges
    private void addFacetsForSearch(SolrQuery query) {
    	List<String> fieldNames = this.getFacetParamNames();
    	
    	for(String fieldName: fieldNames) {
    		//facet is already set to true by default
    		//query.add("facet","true");
    		//query.add("facet.field","{!ex=" + fieldName + "}" + fieldName);   
    		query.add("facet.field",fieldName);   
    	}
    	
    }
    
    //Add explicit filter query
    private void addExplicitFilterQueriesForSearch(SolrQuery query, VitroRequest vreq) {
    	String[] filterQueries = vreq.getParameterValues("fq");
    	if(filterQueries != null) {
	    	for(String s: filterQueries) {
	    		query.addFilterQuery(s);
	    	}
    	}
    }
    
    //Create object with facet information to pass back to body template
    private HashMap<String, List<Count>> handleFacetResults(QueryResponse response) {
    	 //Get facets and their respective counts
        //Passing in the count object list - this allows freemarker to sort them as list of hashes by value
        //enabling the highest value counts to be displayed first
        HashMap<String, List<Count>> facetValueCounts = new HashMap<String, List<Count>>();
        List<FacetField> facetFieldsList = response.getFacetFields();
        for(FacetField f: facetFieldsList) {
        	String facetName = f.getName();
        	List<Count> values = f.getValues();
        	List<Count> valuesToAdd = new ArrayList<Count>();
        	if(values != null) {
            	for(Count c: values) {
            		//We won't be adding facet values that have 0 count
            		long count = c.getCount();
            		if(count != 0) {
            			valuesToAdd.add(c);
            		}
            	}
        	}
        	//Handle type value facets
        	facetValueCounts.put(facetName, valuesToAdd);
        }
        return facetValueCounts;
    }
    
    //Specifically for ranges such as dates
    //This would be useful if we weren't distilling to years for certain dates
    /*
    private HashMap<String, List<Count>> handleFacetRangeResults(QueryResponse response) {
    	List<RangeFacet> rangeFacets = response.getFacetRanges();
        HashMap<String, List<Count>> facetRangeCounts = new HashMap<String, List<Count>>();

    	for(RangeFacet rf: rangeFacets) {
        	String facetName = rf.getName();
        	List<Count> counts = rf.getCounts();
        	List<Count> valuesToAdd = new ArrayList<Count>();
        	if(counts != null) {
            	for(Count c:counts) {
            		//We won't be adding facet values that have 0 count
            		long count = c.getCount();
            		if(count != 0) {
            			valuesToAdd.add(c);
            		}
            	}
        	}
        	facetRangeCounts.put(facetName, valuesToAdd);
    	}
    	return facetRangeCounts;
    }
    */
    
    //Created a different method that uses the VitroRequest if need to factor this out entirely later
    public ParamMap setupPagingLinksParamMap(VitroRequest vreq) {
    	String facetParam = this.getFacetParam(vreq);
    	HashMap<String, String> facetParamMap = this.getFacetParamMap(vreq);
    	//this is the value to be used for the class group
    	String classGroupParam = this.getClassGroupValue(vreq);
    	String typeParam = this.getTypeParam(vreq);
    	boolean wasXmlRequested = this.getWasXmlRequested(vreq);
    	boolean wasHtmlRequested = this.getWasHtmlRequested(vreq);
    	String queryText = this.getQueryText(vreq);
        int hitsPerPage = getHitsPerPage( vreq );           

    	return this.setupPagingLinksParamMap(queryText, hitsPerPage, wasXmlRequested, facetParam, facetParamMap, wasHtmlRequested, classGroupParam, typeParam);
    }
    
    //Method dedicated to getting paging param links instead of distributing it across methods
    public ParamMap setupPagingLinksParamMap(String queryText, int hitsPerPage, boolean wasXmlRequested,  String facetParam, HashMap<String, String> facetParamMap, boolean wasHtmlRequested,  String classGroupParam, String typeParam) {
        boolean typeFilterRequested = !StringUtils.isEmpty(typeParam);

    	ParamMap pagingLinkParams = new ParamMap();
        pagingLinkParams.put(PARAM_QUERY_TEXT, queryText);
        pagingLinkParams.put(PARAM_HITS_PER_PAGE, String.valueOf(hitsPerPage));
        
        if( wasXmlRequested ){
            pagingLinkParams.put(PARAM_XML_REQUEST,"1");                
        }
        
        if(!StringUtils.isBlank(facetParam)) {
        	//put these in the paging link params
        	pagingLinkParams.putAll(facetParamMap);
        }            
           
       /* Add ClassGroup and type refinement links to body */
       if( wasHtmlRequested ){ 
           if ( !typeFilterRequested) {
        	   pagingLinkParams.put(PARAM_CLASSGROUP, classGroupParam);
           } else {
               pagingLinkParams.put(PARAM_RDFTYPE, typeParam);
           }
       }           

        
        return pagingLinkParams;
    }
    
    public class VClassGroupSearchLink extends LinkTemplateModel {        
        long count = 0;
        String groupUri = null;
        VClassGroupSearchLink(String querytext, VClassGroup classgroup, long count) {
            super(classgroup.getPublicName(), "/search", PARAM_QUERY_TEXT, querytext, PARAM_CLASSGROUP, classgroup.getURI());
            this.count = count;
            this.groupUri = classgroup.getURI();
        }
        
        public String getCount() { return Long.toString(count); }
        
        public String getUri() { return this.groupUri; }
    }
    
    public class VClassSearchLink extends LinkTemplateModel {
        long count = 0;
        VClassSearchLink(String querytext, VClass type, long count) {
            super(type.getName(), "/search", PARAM_QUERY_TEXT, querytext, PARAM_RDFTYPE, type.getURI());
            this.count = count;
        }
        
        public String getCount() { return Long.toString(count); }               
    }
    
    protected static List<PagingLink> getPagingLinks(int startIndex, int hitsPerPage, long hitCount, String baseUrl, ParamMap params) {

        List<PagingLink> pagingLinks = new ArrayList<PagingLink>();
        
        // No paging links if only one page of results
        if (hitCount <= hitsPerPage) {
            return pagingLinks;
        }
        
        int maxHitCount = DEFAULT_MAX_HIT_COUNT ;
        if( startIndex >= DEFAULT_MAX_HIT_COUNT  - hitsPerPage )
            maxHitCount = startIndex + DEFAULT_MAX_HIT_COUNT ;                
            
        for (int i = 0; i < hitCount; i += hitsPerPage) {
            params.put(PARAM_START_INDEX, String.valueOf(i));
            if ( i < maxHitCount - hitsPerPage) {
                int pageNumber = i/hitsPerPage + 1;
                boolean iIsCurrentPage = (i >= startIndex && i < (startIndex + hitsPerPage)); 
                if ( iIsCurrentPage ) {
                    pagingLinks.add(new PagingLink(pageNumber));
                } else {
                    pagingLinks.add(new PagingLink(pageNumber, baseUrl, params));
                }
            } else {
                pagingLinks.add(new PagingLink("more...", baseUrl, params));
                break;
            }
        }   
        
        return pagingLinks;
    }
    
    private String getPreviousPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put(PARAM_START_INDEX, String.valueOf(startIndex-hitsPerPage));
        return UrlBuilder.getUrl(baseUrl, params);
    }
    
    private String getNextPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put(PARAM_START_INDEX, String.valueOf(startIndex+hitsPerPage));
        return UrlBuilder.getUrl(baseUrl, params);
    }
    
    protected static class PagingLink extends LinkTemplateModel {
        
        PagingLink(int pageNumber, String baseUrl, ParamMap params) {
            super(String.valueOf(pageNumber), baseUrl, params);
        }
        
        // Constructor for current page item: not a link, so no url value.
        PagingLink(int pageNumber) {
            setText(String.valueOf(pageNumber));
        }
        
        // Constructor for "more..." item
        PagingLink(String text, String baseUrl, ParamMap params) {
            super(text, baseUrl, params);
        }
    }
   
    private ExceptionResponseValues doSearchError(Throwable e, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Search failed: " + e.getMessage());  
        return new ExceptionResponseValues(getTemplate(f,Result.ERROR), body, e);
    }   
    
    private TemplateResponseValues doFailedSearch(String message, String querytext, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        if ( StringUtils.isEmpty(message) ) {
            message = "Search failed.";
        }        
        body.put("message", message);
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);
    }

    private TemplateResponseValues doNoHits(String querytext, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        body.put("message", "No matching results.");     
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);        
    }

    /**
     * Makes a message to display to user for a bad search term.
     * @param queryText
     * @param exceptionMsg
     */
    private String makeBadSearchMessage(String querytext, String exceptionMsg){
        String rv = "";
        try{
            //try to get the column in the search term that is causing the problems
            int coli = exceptionMsg.indexOf("column");
            if( coli == -1) return "";
            int numi = exceptionMsg.indexOf(".", coli+7);
            if( numi == -1 ) return "";
            String part = exceptionMsg.substring(coli+7,numi );
            int i = Integer.parseInt(part) - 1;

            // figure out where to cut preview and post-view
            int errorWindow = 5;
            int pre = i - errorWindow;
            if (pre < 0)
                pre = 0;
            int post = i + errorWindow;
            if (post > querytext.length())
                post = querytext.length();
            // log.warn("pre: " + pre + " post: " + post + " term len:
            // " + term.length());

            // get part of the search term before the error and after
            String before = querytext.substring(pre, i);
            String after = "";
            if (post > i)
                after = querytext.substring(i + 1, post);

            rv = "The search term had an error near <span class='searchQuote'>"
                + before + "<span class='searchError'>" + querytext.charAt(i)
                + "</span>" + after + "</span>";
        } catch (Throwable ex) {
            return "";
        }
        return rv;
    }
    
    @SuppressWarnings({ "unchecked", "unused" })
    private HashSet<String> getDataPropertyBlacklist(){
        HashSet<String>dpBlacklist = (HashSet<String>)
        getServletContext().getAttribute(IndexConstants.SEARCH_DATAPROPERTY_BLACKLIST);
        return dpBlacklist;        
    }
    
    @SuppressWarnings({ "unchecked", "unused" })
    private HashSet<String> getObjectPropertyBlacklist(){
        HashSet<String>opBlacklist = (HashSet<String>)
        getServletContext().getAttribute(IndexConstants.SEARCH_OBJECTPROPERTY_BLACKLIST);
        return opBlacklist;        
    }
    
    public static final int MAX_QUERY_LENGTH = 500;

    public VitroQueryFactory getQueryFactory() {
        throw new Error("PagedSearchController.getQueryFactory() is unimplemented");
    }

    @SuppressWarnings("rawtypes")
    public List search(VitroQuery query) throws SearchException {
        throw new Error("PagedSearchController.search() is unimplemented");
    }
    
    protected boolean isRequestedFormatXml(VitroRequest req){
        if( req != null ){
            String param = req.getParameter(PARAM_XML_REQUEST);
            if( param != null && "1".equals(param)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    protected boolean isRequestedFormatCSV(VitroRequest req){
        if( req != null ){
            String param = req.getParameter(PARAM_CSV_REQUEST);
            if( param != null && "1".equals(param)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    protected Format getFormat(VitroRequest req){
        if( req != null && req.getParameter("xml") != null && "1".equals(req.getParameter("xml")))
            return Format.XML;
        else if ( req != null && req.getParameter("csv") != null && "1".equals(req.getParameter("csv")))
        	return Format.CSV;
        else 
            return Format.HTML;
    }
    
    protected static String getTemplate(Format format, Result result){
        if( format != null && result != null)
            return templateTable.get(format).get(result);
        else{
            log.error("getTemplate() must not have a null format or result.");
            return templateTable.get(Format.HTML).get(Result.ERROR);
        }
    }
    
    protected static Map<Format,Map<Result,String>> setupTemplateTable(){
        Map<Format,Map<Result,String>> templateTable = 
            new HashMap<Format,Map<Result,String>>();
        
        HashMap<Result,String> resultsToTemplates = new HashMap<Result,String>();
        
        // set up HTML format
        resultsToTemplates.put(Result.PAGED, "search-pagedResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-error.ftl");
        // resultsToTemplates.put(Result.BAD_QUERY, "search-badQuery.ftl");        
        templateTable.put(Format.HTML, Collections.unmodifiableMap(resultsToTemplates));
        
        // set up XML format
        resultsToTemplates = new HashMap<Result,String>();
        resultsToTemplates.put(Result.PAGED, "search-xmlResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-xmlError.ftl");
        // resultsToTemplates.put(Result.BAD_QUERY, "search-xmlBadQuery.ftl");        
        templateTable.put(Format.XML, Collections.unmodifiableMap(resultsToTemplates));
        
        return Collections.unmodifiableMap(templateTable);
    }
    
    
    //Getter methods used
    
    //This will return the value to be used in the code, based on what th
    private String getClassGroupValue(VitroRequest vreq) {
    	
    	 String classGroupValue = this.getClassGroupParam(vreq); 
         boolean classGroupFilterRequested = !StringUtils.isEmpty(classGroupValue);
         if(!classGroupFilterRequested) {
         	//We want to use the default class group parameter to show types from that class group 
         	//when no classgroup is initially requested
         	classGroupValue = 	DEFAULT_CLASSGROUP_PARAM;
         }
    	return classGroupValue;
    }
    
    private String getClassGroupParam(VitroRequest vreq) {
    	 // ClassGroup filtering param
        String classgroupParam = (String) vreq.getParameter(PARAM_CLASSGROUP);
        return classgroupParam;
        // rdf:type filtering param
        
     
    }
    
    private String getTypeParam(VitroRequest vreq) {
        String typeParam = (String) vreq.getParameter(PARAM_RDFTYPE);
        return typeParam;
    }
    
    private String getFacetParam(VitroRequest vreq) {
    	   //Facet parameter
        //Indicates that one or more facet parameters is being sent out
        String facetParam = (String) vreq.getParameter("facetParam");
        return facetParam;
    }
    
    private boolean getWasXmlRequested (VitroRequest vreq) {
    	Format format = getFormat(vreq);    
    	 boolean wasXmlRequested = Format.XML == format;
         log.debug("Requested format was " + (wasXmlRequested ? "xml" : "html"));
         return wasXmlRequested;
        
    }
    
    private boolean getWasHtmlRequested(VitroRequest vreq) {
    	return   ! (this.getWasXmlRequested(vreq));
    }
    
    private String getQueryText(VitroRequest vreq) {
        String queryText = vreq.getParameter(VitroQuery.QUERY_PARAMETER_NAME);  
        return queryText;
    }
    
    private boolean isClassGroupFilterRequested(VitroRequest vreq) {
    	return !StringUtils.isEmpty(this.getClassGroupParam(vreq));
    }
}
