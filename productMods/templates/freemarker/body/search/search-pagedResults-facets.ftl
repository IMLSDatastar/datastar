<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--querytext is within body and not used as a parameter - can change that to be consistent and pass as parameter->
<#--Show the facets that have been selected as part of search-->
<#macro showSelectedFacets facetParameterMap typeParam="" isSelectedClassGroup=false classGroupName="" classGroupUri="">
	<#if (facetParameterMap?has_content && (facetParameterMap?keys?size > 0)) || typeParam?has_content || isSelectedClassGroup>
	<h4 class="selectedFacets">Selected Facets</h4>
	<#if typeParam?has_content>
		<#assign typeRemovalLink = generateRemoveLink(facetParameterMap "type") />
		
		<@showSelectedFacet "type" typeParam typeRemovalLink />
	</#if>
	<#if isSelectedClassGroup>
		<#assign classGroupRemovalLink = generateRemoveLink(facetParameterMap "class group") />
		
		<@showSelectedFacet "class group" classGroupName classGroupRemovalLink />
	</#if>
	<#--Debugging-->
	<#--Facet param map:-->
	<#--list facetParameterMap?keys as key>
		${key} : ${facetParameterMap[key]} <br/>
	</#list-->
	<#--End debugging-->
	<#list facetParameterMap?keys as key>
		<#assign removalLink = generateRemoveLink(facetParameterMap key) />
		<@showSelectedFacet key facetParameterMap[key] removalLink />
	</#list>
	</#if>
</#macro>

<#macro showSelectedFacet title value removeLink>
<div class="selectedFacetDisplay">
		<a href="${removeLink}" title="remove facet">${title}: ${value}	 
		<span class="internalInfo roundedDiv remove">Remove</span></a>
		</div>
</#macro>

<#macro showTypeAndClassgroupFacet facetParameterMap typeParam=""  classLinks="" classGroupLinks="" classGroupName="" selectedClassGroupUri="" classLinksMap="">
	<#--if a specific type has not been selected and we do have type links to show-->
	<#if !typeParam?has_content && classLinks?has_content>
        	
        <div class="searchFacet" facet="type">
	        <nav class="scroll-up" role="navigation">
	            <img src="/datastarimls/images/individual/collapse-prop-group.png" facet="type" />
	        </nav>
	        <#--Show class group name if one is selected by user-->
	        <h4>Type  </h4>
	        
            <ul id="typeFacets" facet="type">
            	<#-- Display class groups with class links under them-->
            	<#--The list under selected classgroup name will be selected-->
            	<#--Default class group view where all class groups are visible-->
            	<#if classLinksMap?has_content && !isSelectedClassGroup>
            		<#assign classLinksMapKeys = classLinksMap?keys />
	            	<#list classGroupLinks as link>
	            		<#assign thisClassGroupUri = link.uri />
	            		<#--Check if uri is in classgroup map-->
	            		<#if classLinksMapKeys?seq_contains(thisClassGroupUri)>
	            			<li class="roundedDiv subcategory"><a href="${link.url}" title="class group link">${link.text}<span class="internalInfo roundedDiv">${link.count!}</span></a></li>
	            			<#assign clinks = classLinksMap[thisClassGroupUri] />
	                		<@showClassGroupClasses clinks />
	            		</#if>
	            	</#list>
	            <#--If a specific class group parameter has been selected-->	
	            <#elseif isSelectedClassGroup> 
	            	<#assign classGroupRemovalLink = generateRemoveLink(facetParameterMap "class group") />
	            	<li class="roundedDiv subcategory selectedFacet"><a href="${classGroupRemovalLink}" title="class group remove link">${classGroupName}<span class="internalInfo roundedDiv remove">Remove</span></a></li>
	            	<#--list types under selected class group-->
	            	<#assign clinks = classLinksMap[selectedClassGroupUri] />
	                <@showClassGroupClasses clinks />
            	</#if>
            </ul>
          
        </div>
    </#if>
    <#--If specific type has been selected, show the type under the facet-->
    <#if typeParam?has_content>
	<#assign typeRemovalLink = generateRemoveLink(facetParameterMap "type") />
     <div class="searchFacet" facet="type">
	        <nav class="scroll-up" role="navigation">
	            <img src="/datastarimls/images/individual/collapse-prop-group.png" facet="type" />
	        </nav>
	         <h4>Type </h4>
              <ul id="typeFacets" facet="type">
            	<li class="roundedDiv selectedFacet"><a href="${typeRemovalLink}" title="type remove link">${typeParam} <span class="internalInfo roundedDiv remove">Remove</span></a></li>
	            		
         	  </ul>
       </div>
            
    </#if>
</#macro>

<#macro showClassGroupClasses classLinks>
	<#list classLinks as link>
	            	<li class="roundedDiv"><a href='${link.url}'>${link.text}<span class="internalInfo roundedDiv">${link.count!}</span></a></li>
	</#list>
</#macro>

<#macro showGeographicFacet facetParameterMap>
 	<div class="searchFacet" facet="geographicCoverage">
 	<#assign imgName = "expand-prop-group.png" />
 	<#if geographicCoverageFacet?has_content>
 		<#assign imgName = "collapse-prop-group.png" />
 	</#if>
	<nav class="scroll-up" role="navigation">
		<img src="/datastarimls/images/individual/${imgName}" facet="geographicCoverage"/>
	</nav>
	<h4>Geographic <br/>Coverage</h4>
	<@showFacet "geographiclocation_string" "geographicCoverageFacets" "geographicCoverage" facetParameterMap/>
	<a id="moreGeographicCoverage" href="#" facet="geographicCoverage"> &gt;&gt;More Geographic Coverage</a>
	</div>	
</#macro>

<#macro showSubjectAreaFacet facetParameterMap>
	<div class="searchFacet" facet="subjectAreas">
    <nav class="scroll-up" role="navigation">
		<img src="/datastarimls/images/individual/expand-prop-group.png" facet="subjectAreas" />
	</nav>
    <h4>Subject Areas</h4>
	<@showFacet "subjectarea_string" "subjectAreasFacets" "subjectAreas" facetParameterMap/>
	<a id="moreSubjectAreas" href="#" facet="subjectAreas"> &gt;&gt;More Subject Areas</a>
	</div>
</#macro>

<#macro showKeywordFacet facetParameterMap>
	<div class="searchFacet" facet="keywords">
	<nav class="scroll-up" role="navigation">
		<img src="/datastarimls/images/individual/expand-prop-group.png" facet="keywords" />
	</nav>
	<h4>Keywords</h4>
	<@showFacet "keyword_string" "keywordsFacets" "keywords" facetParameterMap/>
	<a id="moreKeywords" href="#" facet="keywords"> &gt;&gt;More Keywords</a>
	</div>
</#macro>

<#macro showAuthorFacet facetParameterMap>
	<div class="searchFacet" facet="authors">
	<nav class="scroll-up" role="navigation">
		<img src="/datastarimls/images/individual/expand-prop-group.png" facet="authors"/>
	</nav>
	<h4>Authors</h4>
	<@showFacet "author_string" "authorsFacets" "authors" facetParameterMap/>
	 <a id="moreAuthors" href="#" facet="authors"> &gt;&gt;More Authors</a>
	 </div>
</#macro>

<#macro showTemporalCoverageFacet facetParameterMap>
	<div class="searchFacet" facet="temporalCoverage">
	<nav class="scroll-up" role="navigation">
	<img src="/datastarimls/images/individual/expand-prop-group.png" facet="temporalCoverage"/>
	</nav>
	<h4>Temporal <br/>Coverage</h4>
	<@showFacet "temporalCoverageAll_text" "temporalCoverageFacets" "temporalCoverage" facetParameterMap true/>
	<a id="moreTemporalCoverage" href="#" facet="temporalCoverage"> &gt;&gt;More Temporal Coverage</a>
	</div>
</#macro>


<#--display the facet, use range is employed in cases where a range filter query needs to be applied-->
<#macro showFacet facetName listId facetLabel facetParameterMap useFilterQuery=false>
	<#--Keeping this for later when people can use multiple facet values for filtering-->
	<#assign addTypeFacet = ""/>
	<#--if facets returned has facet we want, then display this facet-->
	<#if (facets?keys?seq_contains(facetName))>
		<#--Each facet name corresponds to list of Count objects that have name and count methods-->
		<#assign facetValues = facets[facetName]/>
		<#assign facetsDisplayedCount = 0/>
		<#assign facetItems>
			<#--Displaying highest counts first usually except for temporal coverage-->
			<#assign sortedFacetValues = getSortedValues(facetName) />
			<#list sortedFacetValues as facetValue>
				<#--Make sure key isn't empty string and the number of values is greater than 0 if we are to display-->
				<#if (facetValue.name?length > 0) && (facetValue.count > 0)>
					<#assign facetsDisplayedCount = (facetsDisplayedCount + 1)/>
					<li class="roundedDiv"><@getFacetLink querytext facetName facetValue facetParameterMap/><#--a href='${urls.base}/search?querytext=${querytext}&facetParam=true&${facetName}="${facetValue.name}"<@generateUrlFacetParameters facetParameterMap facetName/>'/-->${facetValue.name} (${facetValue.count})</a></li>
				</#if>
    		</#list>
    	</#assign>    	
    	<#if (facetsDisplayedCount > 0)>
    		<ul id="${listId}" facet="${facetLabel}" class="roundedDiv">
    		${facetItems}
    		</ul>
    	<#else>
    		<ul id="${listId}" facet="${facetLabel}" class="roundedDiv noresults">
    		<li>No Results</li>	
    		</ul>
    	</#if>
	</#if>
</#macro>

<#function getSortedValues facetName>
	<#if facetName = "temporalCoverageAll_text">
		<#--return years in ascending order-->
		<#return facetValues?sort_by("name") />
	<#else>
		<#return facetValues?sort_by("count")?reverse />
	</#if>
</#function>

<#macro getFacetLink querytext facetName facetValue facetParameterMap>
<#--for temporal coverage, utilize a different facet for filter query where we utilize a filter query instead -->
<#if facetName = "temporalCoverageAll_text"><#assign year=facetValue.name/>
<#assign filterQuery="temporalCoverageStart_tint:${year} OR temporalCoverageEnd_tint:${year} OR (temporalCoverageStart_tint:[* TO ${year}] AND temporalCoverageEnd_tint:[${year} TO *])"/>
<a href='${urls.base}/search?querytext=${querytext}&facetParam=true&fq=${filterQuery?url}<@generateUrlFacetParameters facetParameterMap facetName/>'/>
<#else><a href='${urls.base}/search?querytext=${querytext}&facetParam=true&${facetName}="${facetValue.name}"<@generateUrlFacetParameters facetParameterMap facetName/>'/>
</#if>
</#macro>

<#--generate filter query if need be-->
<#--how you would call this macro generateFilterQuery useFilterQuery facetName facetValue-->
<#macro generateFilterQuery useFilterQuery facetName facetValue>
<#if useFilterQuery>
</#if>
</#macro>

<#--Create removal link to move back-->

<#function generateRemoveLink facetParameterMap parameterName>

<#assign removalLinkUrl>
${urls.base}/search?querytext=${querytext}<@generateUrlFacetParameters facetParameterMap parameterName/>
</#assign>
<#return removalLinkUrl />
</#function>

<#--Given the facet being displayed, generate the parameter information for the facets that have already been selected-->
<#macro generateUrlFacetParameters facetParameterMap parameterName>
	<#assign numberExtraParams = 0/>
	<#assign extraParams = ""/>
	<#list facetParamMap?keys as facetParam>
		<#if facetParam != parameterName>
			<#assign numberExtraParams = (numberExtraParams + 1) />
			<#assign extraParams = extraParams + '&' + facetParam + '=' + facetParamMap[facetParam] />
		</#if>
	</#list>
	<#if (numberExtraParams > 0)>${extraParams}</#if> 
</#macro>