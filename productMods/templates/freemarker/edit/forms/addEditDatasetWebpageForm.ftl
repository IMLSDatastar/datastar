<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding/editing core:webpages -->
<#import "lib-vivo-form.ftl" as lvf>

<#assign subjectName=editConfiguration.pageData.subjectName!"an Individual" />

<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
    <#assign submissionErrors = editSubmission.validationErrors/>
</#if>

<#--Retrieve variables needed-->
<#assign url = lvf.getFormFieldValue(editSubmission, editConfiguration, "url")/>
<#assign urlTypeValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "urlType")/>
<#assign label = lvf.getFormFieldValue(editSubmission, editConfiguration, "label") />
<#assign newRank = editConfiguration.pageData.newRank!"" />
<#assign dataFileFormat = lvf.getFormFieldValue(editSubmission, editConfiguration, "dataFileFormat") />

<#if url?has_content>
    <#assign editMode = "edit">
<#else>
    <#assign editMode = "add">
</#if>

<#if editMode == "edit">        
        <#assign titleVerb="${i18n().edit_wbpage_of}">        
        <#assign submitButtonText="${i18n().save_changes}">
        <#assign disabledVal="disabled">
<#else>
        <#assign titleVerb="${i18n().add_webpage_for}">
        <#assign submitButtonText="${i18n().add_webpage}">
        <#assign disabledVal=""/>
</#if>

<#assign requiredHint="<span class='requiredHint'> *</span>" />

<h2>${titleVerb} ${subjectName}</h2>

<#if submissionErrors??>
<section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>       
        <#list submissionErrors?keys as errorFieldName>
            ${errorFieldName}: ${submissionErrors[errorFieldName]}  <br/>           
        </#list>
        </p>
</section>
</#if>    

<#--For now, limiting this to data and metadata links - other webpages can be categorized separately-->    
<form class="customForm" action ="${submitUrl}">

	<input type="hidden" name="urlType" id="urlType" value="http://purl.org/datastar/DatasetURLLink" />
    <#--This included a select before but have removed that as currently one URL link type supported here-->
         
    <label for="url">URL ${requiredHint}</label>
    <input  size="70"  type="text" id="url" name="url" value="${url}" role="input" />
   
    <label for="label">Webpage Name</label>
    <input  size="70"  type="text" id="label" name="label" value="${label}" role="input" />
    
    <label forproperty="dataFileFormat" for="dataFileFormat">Data file format</label>
    <input  forproperty="dataFileFormat" size="70"  type="text" id="dataFileFormat" name="dataFileFormat" value="${dataFileFormat!}" role="input" />

    <#if editMode="add">
        <input type="hidden" name="rank" value="${newRank}" />
    </#if>
    
    <input type="hidden" id="editKey" name="editKey" value="${editConfiguration.editKey}"/>
    <p class="submit">
        <input type="submit" id="submit" value="${submitButtonText}"/><span class="or"> or </span>
        <a class="cancel" href="${editConfiguration.cancelUrl}" title="Cancel">Cancel</a>
    </p>    
</form>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/userMenu/userMenuUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
