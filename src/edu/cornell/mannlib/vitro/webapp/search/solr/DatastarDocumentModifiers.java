/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.solr.DatastarObjectProperties;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.search.solr.InvestigationRelationship;

public class DatastarDocumentModifiers extends VivoDocumentModifiers implements javax.servlet.ServletContextListener{
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        ServletContext context = sce.getServletContext();
		RDFServiceFactory rdfServiceFactory = RDFServiceUtils.getRDFServiceFactory(context);
        
        Dataset dataset = DatasetFactory.create(ModelAccess.on(context).getJenaOntModel());
       
        //Get the document modifiers set by the super class      
        List<DocumentModifier> modifiers = (List<DocumentModifier>)context.getAttribute("DocumentModifiers");
        
        /* add DocumentModifiers into servlet context for use later in startup by SolrSetup */        
       // modifiers.add(new CalculateParameters(dataset));        //
        
        modifiers.add(new InvestigationRelationship(rdfServiceFactory));        //
        modifiers.add(new DatastarObjectProperties(rdfServiceFactory));
        context.setAttribute("DocumentModifiers", modifiers);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // do nothing.        
    }    
}
