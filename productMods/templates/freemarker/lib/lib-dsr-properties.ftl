<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#-- core:webpage
     
     Note that this macro has a side-effect in the call to propertyGroups.pullProperty().
-->
<#-- This changes the method for datasets, so the link is slightly different-->
<#import "lib-properties.ftl" as p>
<#assign datastarCore = "http://purl.org/datastar/">

<#assign core = "http://vivoweb.org/ontology/core#">

<#macro datasetWebpages propertyGroups editable linkListClass="individual-urls">
	<#--Pulling webpage property but will not display for dataset-->
	<#assign webpage = propertyGroups.pullProperty("http://purl.obolibrary.org/obo/ARG_2000028","http://www.w3.org/2006/vcard/ns#URL")!>
    <#--local datasetWebpage = propertyGroups.pullProperty("${datastarCore}datasetWebpage")!-->
    <#local hasAssociatedMetadata = propertyGroups.pullProperty("${datastarCore}hasAssociatedMetadata")!>

    <#if webpage?has_content || hasAssociatedMetadata?has_content> <#-- true when the property is in the list, even if not populated (when editing) -->
        <nav role="navigation">
            <#local label = "Data and Metadata Links">  
            <#--This used to be @p.addLinkWithLabel webpage editable label, we are overwriting to allow hardcoding the generator-->
            <@p.addLinkWithLabel webpage editable label/>
            <#if webpage.statements?has_content || hasAssociatedMetadata.statements?has_content> <#-- if there are any statements -->
                <#include "lib-dsr-property-datasetWebpage.ftl">
            </#if>            
        </nav>
    
    </#if>
</#macro>

<#--Am leaving these to enable hardcoding of url if need be-->
<#macro addManageDatasetLinkWithLabel property editable label>
 <#local addLink><@addManageDatasetLink property editable label /></#local>
    <#local verboseDisplay><@p.verboseDisplay property /></#local>
    <#if editable> 
        <h2 id="${property.localName!}">${label} ${addLink!} ${verboseDisplay!}</h2>         
    </#if>
</#macro>

<#--hardcoding the domain uri in this case to make dataset appear?-->
<#macro addManageDatasetLink property editable label="${property.name}">  
	<#--will need to assemble url ourselves-->  
	<#assign individualUri = individual.uri!""/>
	<#assign individualUri = (individualUri?url)/>
	<#assign individualProfileUrl = individual.profileUrl />
	<#assign profileParameters = individualProfileUrl?substring(individualProfileUrl?index_of("?") + 1)/>
	<#assign extraParameters = ""/>
	<#if profileParameters?contains("uri=")>
		<#assign extraParameters = profileParameters?replace("uri=" + individualUri, "") />
	</#if>
    <#if property.rangeUri?? >
        <#local rangeUri = property.rangeUri /> 
    <#else>
        <#local rangeUri = "" /> 
    </#if>
    <#local domainUri = "http://purl.org/datastar/Dataset" /> 
    
    <#if editable>
    	<#local propertyurl = "http://purl.obolibrary.org/obo/ARG_2000028" />
    	<#local url= "${urls.base}/editRequestDispatch?subjectUri=${individualUri}&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManageWebpagesForDatasetGenerator&predicateUri=${propertyurl}${extraParameters}&domainUri=${domainUri}&rangeUri=${rangeUri}">
        <@p.showAddLink property.localName label url rangeUri domainUri/>
    </#if>
</#macro>