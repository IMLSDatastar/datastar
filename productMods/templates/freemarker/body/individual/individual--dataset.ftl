<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Dataset specific templete (extends individual.ftl in vitro) -->

<#include "individual-setup.ftl">
<#import "lib-vivo-properties.ftl" as vp>
<#import "lib-dsr-properties.ftl" as dsrp>

<#macro displaySparqlResults sparqlResults firstRow resultsLabel extraParams={}>
	<#if sparqlResults?has_content>
		<h4>${resultsLabel}</h4>
		<#list sparqlResults as resultRow>
			<#assign resultKeys = resultRow?keys />
			
			<#if firstRow = false>
				<div class="resultHeading resultRow">
				<#list resultKeys as resultKey>
						<div class="resultCell">
						<#if extraParams?has_content && (extraParams?keys?size > 0) &&
							(extraParams?keys?seq_contains(resultKey))>
							${extraParams[resultKey]}
						<#else>
							${resultKey}
						</#if></div>
				</#list>	
				</div>
				<#assign firstRow = true/>
			</#if>
			<div class="resultRow">
				<#list resultKeys as resultKey>
					<div class="resultCell">${resultRow[resultKey]}</div>
				</#list>
			</div>
		</#list>
	</#if>
</#macro>

<#macro displayCitations sparqlResults>
	<#if sparqlResults?has_content>
		
		
		<#list sparqlResults as resultRow>
			<#assign numberCitations = resultRow["objectCount"]/>
			<div> Cited </div>
			<div class="singleNumber"> ${numberCitations} </div>
			<div> times </div>
		</#list>
	</#if>
</#macro>

<#macro displayRelatedDatasets sparqlResults>
	<#if sparqlResults?has_content>
		
		
		<#list sparqlResults as resultRow>
			<#assign countOrigin = resultRow["countOrigin"]/>
			<#assign countDerived = resultRow["countDerived"]/>
			<#assign totalRelated = (countOrigin?number + countDerived?number) />
			<div> Related datasets </div>
			<div class="singleNumber"> ${totalRelated} </div>
		</#list>
	</#if>
</#macro>

<#assign individualProductExtension>
	<#--Dataset specific extension-->
	<section id="datasetcontent">
	<div class="embeddedResultsContainer">
		<div id="publicationsCitedIn" class="embeddedResultsDiv">	
			<div class="embeddedResultsContent">
				<div class="embeddedResultsDivText">
					<@displayCitations publicationsCitedIn />
				</div>
			</div>
			<div class="embeddedResultsLink"><a title="group name" href="#cited_or_used_by">All citations</a></div>
		</div>
		<div id="relatedDatasets" class="embeddedResultsDiv">
			<div class="embeddedResultsContent">
				<div class="embeddedResultsDivText">
					<@displayRelatedDatasets relatedDatasets/>
				</div>
			</div>
			<div class="embeddedResultsLink"><a title="group name" href="#hasDerivedDataset">Derived</a> and <a title="group name" href="#isDerivedFrom">source</a></div>
		</div>
	</div>
	</section>
	
    <#-- Include for any class specific template additions -->
    ${classSpecificExtension!}
    <@dsrp.datasetWebpages propertyGroups editable />
    <#--Original had call to webpages but not doing that for datasets, will pull property and not show-->
    <!--PREINDIVIDUAL OVERVIEW.FTL-->
    <#include "individual-overview.ftl">
        </section> <!-- #individual-info -->
    </section> <!-- #individual-intro -->
    <!--postindiviudal overiew tfl-->
</#assign>
<#include "individual-dataset-vitro.ftl">

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-vivo.css" />',
'<link rel="stylesheet" href="${urls.base}/css/individual/individual-property-groups.css"/>',
'<link rel="stylesheet" href="${urls.base}/css/menupage/sparqlresults.css" />')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.truncator.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/amplify/amplify.store.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/individualUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/individual/propertyGroupControls.js"></script>')}
                  
