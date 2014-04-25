<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying paged search results -->
<#import "search-pagedResults-facets.ftl" as facetsMacros />
<h2>
<#escape x as x?html>
    Search results for '${querytext}'
    <#--Need mechanism here to check whether specific classgroup has been selected as classgroupname is populated with default value even if no class group selected-->
    <#if classGroupName?has_content && isSelectedClassGroup>limited to type classgroup '${classGroupName}'</#if>
    <#if typeName?has_content>limited to type '${typeName}'</#if>
    
</#escape>
</h2>
<span id="searchHelp"><a href="${urls.base}/searchHelp" title="search help">Narrow search results by</a></span>

<div class="contentsBrowseGroup">
	<div class="searchTOC">
		<@facetsMacros.showSelectedFacets facetParamMap typeName isSelectedClassGroup classGroupName classGroupUri />
		   
        <@facetsMacros.showTypeAndClassgroupFacet facetParamMap typeName classLinks classGroupLinks classGroupName classGroupUri classLinksMap />
        
        <@facetsMacros.showSubjectAreaFacet facetParamMap />
                    
        <@facetsMacros.showKeywordFacet facetParamMap />

		<@facetsMacros.showAuthorFacet facetParamMap />
            
		<@facetsMacros.showTemporalCoverageFacet facetParamMap />
		
		<@facetsMacros.showGeographicFacet facetParamMap />
		   
	</div> 
        


    <#-->ul class="searchhits">
    	<#list displayResults as result>
    		<li>
    			<a href="#" title="individual name">${result.name}</a>
				<br/>
        		<span class="display-title"><b>${result.type?cap_first} Authors: ${result.author}</b></span>

				<p class="snippet">${result.description} ... <br/><b>Keywords:</b> ${result.keyword}  &nbsp;&nbsp;&nbsp;<#if result.citationsNumber?has_content><b>${result.citationsNumber} citations</b></#if></p>   
    			
    		</li>
    	</#list>
    </ul-->




<#-- Search results -->
    <ul class="searchhits">
        <#list individuals as individual>
            <li>                        
            	<@shortView uri=individual.uri viewContext="search" />
            </li>
        </#list>
    </ul>
    

    <#-- Paging controls -->
    <#if (pagingLinks?size > 0)>
        <div class="searchpages">
            Pages: 
            <#if prevPage??><a class="prev" href="${prevPage}" title="previous">Previous</a></#if>
            <#list pagingLinks as link>
                <#if link.url??>
                    <a href="${link.url}" title="page link">${link.text}</a>
                <#else>
                    <span>${link.text}</span> <#-- no link if current page -->
                </#if>
            </#list>
            <#if nextPage??><a class="next" href="${nextPage}" title="next">Next</a></#if>
        </div>
    </#if>
    <br />

    <#-- VIVO OpenSocial Extension by UCSF -->
    <#if openSocial??>
        <#if openSocial.visible>
        <h3>OpenSocial</h3>
            <script type="text/javascript" language="javascript">
                // find the 'Search' gadget(s).
                var searchGadgets = my.findGadgetsAttachingTo("gadgets-search");
                var keyword = '${querytext}';
                // add params to these gadgets
                if (keyword) {
                    for (var i = 0; i < searchGadgets.length; i++) {
                        var searchGadget = searchGadgets[i];
                        searchGadget.additionalParams = searchGadget.additionalParams || {};
                        searchGadget.additionalParams["keyword"] = keyword;
                    }
                }
                else {  // remove these gadgets
                    my.removeGadgets(searchGadgets);
                }
            </script>

            <div id="gadgets-search" class="gadgets-gadget-parent" style="display:inline-block"></div>
        </#if>
    </#if>
<script type="text/javascript">
var customFormData = {
	typeFacet: "${typeFacet!}",
	geographicCoverageFacet : "${geographicCoverageFacet!}"
};
</script>
    
</div> <!-- end contentsBrowseGroup -->
${stylesheets.add('<link rel="stylesheet" href="${urls.theme}/css/modifiedSearch.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
'<script type="text/javascript" src="${urls.base}/js/modifiedSearch.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}