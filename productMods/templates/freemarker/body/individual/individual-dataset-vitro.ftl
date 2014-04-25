<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--Number of labels present-->
<#if !labelCount??>
    <#assign labelCount = 0 >
</#if>
<#--Number of available locales-->
<#if !localesCount??>
	<#assign localesCount = 1>
</#if>
<#--Number of distinct languages represented, with no language tag counting as a language, across labels-->
<#if !languageCount??>
	<#assign languageCount = 1>
</#if>	

<#-- Default individual profile page template -->

<section id="individual-intro" class="vcard" role="region">
	<#--Removed the image section that used to be here-->    
    <section id="individual-info" ${infoClass!} role="region">
        <#include "individual-adminPanel.ftl">
        
        <header>
            <#if relatedSubject??>
                <h2>${relatedSubject.relatingPredicateDomainPublic} for ${relatedSubject.name}</h2>
                <p><a href="${relatedSubject.url}" title="return to subject">&larr; return to ${relatedSubject.name}</a></p>                
            <#else>                
                <h1 class="fn">
                    <#-- Label -->
                    <@p.label individual editable labelCount />

                    <#--  Most-specific types -->
                    <@p.mostSpecificTypes individual /><img id="uriIcon" title="${individual.uri}" class="middle" src="${urls.images}/individual/uriIcon.gif" alt="uri icon"/>
                </h1>
            </#if>
        </header>
                
    <#if individualProductExtension??>
        ${individualProductExtension}
    <#else>
            </section> <!-- individual-info -->
        </section> <!-- individual-intro -->
    </#if>

<#assign nameForOtherGroup = "other"> <#-- used by both individual-propertyGroupMenu.ftl and individual-properties.ftl -->

<#-- Property group menu -->
     <#include "individual-property-group-menus.ftl">

<#--include "individual-propertyGroupMenu.ftl"-->

<#-- Ontology properties -->
<#--include "individual-properties.ftl"-->

<#assign rdfUrl = individual.rdfUrl>

<#if rdfUrl??>
    <script>
        var individualRdfUrl = '${rdfUrl}';
    </script>
</#if>
<script>
    var i18nStringsUriRdf = {
        shareProfileUri: '${i18n().share_profile_uri}',
        viewRDFProfile: '${i18n().view_profile_in_rdf}',
        closeString: '${i18n().close}'
    };
</script>
<script>
	<!--//TODO:Find why we need this in datastar-->
    var individualLocalName = "${individual.localName}";
</script>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />',
'<link rel="stylesheet" href="${urls.base}/css/individual/individual-property-groups.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip-1.0.0-rc3.min.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/imageUpload/imageUploadUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/individual/individualUriRdf.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/propertyGroupControls.js"></script>')}         
 
<script type="text/javascript">
    i18n_confirmDelete = "${i18n().confirm_delete}"
</script>
