<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Custom form for managing web pages for individuals -->

<#if (editConfiguration.pageData.webpages?size > 1) >
  <#assign ulClass="class='dd'">
<#else>
  <#assign ulClass="">
</#if>

<#if (editConfiguration.pageData.metadataWebpages?size > 1) >
  <#assign ulMetadataClass="class='dd'">
<#else>
  <#assign ulMetadataClass="">
</#if>


<#assign baseEditWebpageUrl=editConfiguration.pageData.baseEditWebpageUrl!"baseEditWebpageUrl is undefined">
<#assign deleteWebpageUrl=editConfiguration.pageData.deleteWebpageUrl!"deleteWebpageUrl is undefined">
<#assign showAddFormUrl=editConfiguration.pageData.showAddFormUrl!"showAddFormUrl is undefined">

<#assign baseEditMetadataWebpageUrl=editConfiguration.pageData.baseEditMetadataWebpageUrl!"baseEditMetadataWebpageUrl is undefined">
<#assign showAddMetadataFormUrl=editConfiguration.pageData.showAddMetadataFormUrl!"showAddMetadataFormUrl is undefined">
<#assign domainUri = editConfiguration.predicateProperty.domainVClassURI!"">
<#assign predicateUri=editConfiguration.predicateUri!"undefined">

<#if (editConfiguration.pageData.subjectName??) >
<h2><em>${editConfiguration.pageData.subjectName}</em></h2>
</#if>

<h3>${i18n().manage_data_and_metadata_web_pages}</h3>
       
<script type="text/javascript">
    var webpageData = [];
    var mwebpageData = [];
</script>

<#if !editConfiguration.pageData.webpages?has_content && !editConfiguration.pageData.metadataWebpages?has_content>
    <p>${i18n().has_no_data_or_metadata_webpages}</p>
</#if>

<h5> Data Links </h5>
<ul id="webpageList" ${ulClass} role="list">
	<#--dataset links-->
    <#list editConfiguration.pageData.webpages as webpage>
        <li class="webpage data" role="listitem" >
            <#if webpage.label??>
                <#assign label=webpage.label >
            <#else>
                <#assign label=webpage.url >
            </#if>
            
            <span class="webpageName">
                <a href="${webpage.url}" title="${i18n().webpage_url}">${label}</a>
            </span>
            <span class="editingLinks">
                <a href="${baseEditWebpageUrl}&objectUri=${webpage.vcard}&predicateUri=${predicateUri}&linkUri=${webpage.link}" class="edit" title="${i18n().edit_data_webpage_link}">${i18n().edit_capitalized}</a> | 
                <a href="${urls.base}${deleteWebpageUrl}" class="remove" title="${i18n().delete_data_webpage_link}">${i18n().delete_button}</a> 
            </span>
            
        </li>    
        
        <script type="text/javascript">
            webpageData.push({
                "webpageUri": "${webpage.link}"              
            });
        </script>      
    </#list>
 </ul>   
 
 <h5> Metadata Links </h5>
 <ul id="metadataWebpageList" ${ulMetadataClass} role="list">
    
    <#--metadata links-->
    <#list editConfiguration.pageData.metadataWebpages as mwebpage>
        <li class="webpage metadata" role="listitem" >
            <#if mwebpage.label??>
                <#assign label=mwebpage.label >
            <#else>
                <#assign label=mwebpage.url >
            </#if>
            
              <span class="webpageName metadata">
                <a href="${mwebpage.url}" title="${i18n().webpage_url}">${label}</a>
            </span>
            <span class="editingLinks">
                <a href="${baseEditMetadataWebpageUrl}&objectUri=${mwebpage.metadata}&predicateUri=${editConfiguration.pageData.associatedMetadataPredicate!}&vcardUri=${mwebpage.vcard}&linkUri=${mwebpage.link}" class="edit" title="${i18n().edit_metadata_webpage_link}">${i18n().edit_capitalized}</a> | 
                <a href="${urls.base}${deleteWebpageUrl}" class="remove metadata" title="${i18n().delete_metadata_webpage_link}">${i18n().delete_button}</a> 
            </span>
        </li>    
        
        <script type="text/javascript">
            mwebpageData.push({
                "webpageUri": "${mwebpage.link}"              
            });
        </script>      
    </#list>    
</ul>

<section id="addAndCancelLinks" role="section">
 
    <a href="${showAddFormUrl}<#if (domainUri?length > 0)>&domainUri=${domainUri}</#if>" id="showAddForm" class="button green" title="add new data link">Add New Data Link</a>
    <a href="${showAddMetadataFormUrl}<#if (domainUri?length > 0)>&domainUri=${domainUri}</#if>" id="showAddMetadataForm" class="button green" title="add new metadata link">Add New Metadata Link</a>
       
    <a href="${cancelUrl}" id="returnToIndividual" class="return" title="return to individual">Return to Individual</a>
    <img id="indicator" class="indicator hidden" src="${urls.base}/images/indicatorWhite.gif" />
</section>


<script type="text/javascript">
var customFormData = {
    rankPredicate: '${editConfiguration.pageData.rankPredicate}',
    reorderUrl: '${urls.base}/edit/reorder'
};
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
                  '<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/manageWebpagesForIndividual.css" />',
				  '<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/manageDatasetWebpages.css" />',
                  '<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/manageWebpagesForIndividual.js"></script>',
              '<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/manageMetadataWebpagesForIndividual.js"></script>')
              }