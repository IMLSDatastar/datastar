<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--These macros are called from within the script tag, so we are writing out javascript here-->
<#--Assumptions: that we have access to the variables defined and libraries imported within newDatasetForm.ftl -->
<#--Macro definitions -->

<#macro handleAdditionalAuthorsForFormWithErrors numberAuthors=1>
	<#if (numberAuthors > 1)>
		<#--Create a json object that can be passed back to the javascript, representing the information for this author-->
		customFormData["additionalAuthorsHash"] = {}; 	
		<#list 2..numberAuthors as i>
			<@createJSONForAuthor i />
		</#list>
	</#if>
</#macro>

<#macro createJSONForAuthor counter>
	customFormData.additionalAuthorsHash["author${counter}"] = {};
	<#assign authorFields = ["lastName", "firstName", "middleName", "orgName", "personUri", "orgUri", "rank", "selectedAuthorLabel"] />
	<#list authorFields as authorField>
		<#assign fieldName = authorField + counter />
		<#assign fieldValue = lvf.getFormFieldValue(editSubmission, editConfiguration, fieldName) />
		customFormData.additionalAuthorsHash["author${counter}"]["${fieldName}"] = "${fieldValue}";
	</#list>

</#macro>


<#macro handleAdditionalDataLinksForFormWithErrors numberDataLinks=1>
	<#if (numberDataLinks > 1)>
		<#--Create a json object that can be passed back to the javascript, representing the information for this author-->
		customFormData["additionalDataLinksHash"] = {}; 	
		<#list 2..numberDataLinks as i>
			<@createJSONForDataLink i />
		</#list>
	</#if>
</#macro>

<#macro createJSONForDataLink counter>
	customFormData.additionalDataLinksHash["datalink${counter}"] = {};
	<#assign authorFields = ["url", "linkLabel", "dataFileFormat", "dataRank"] />
	<#list authorFields as authorField>
		<#assign fieldName = authorField + counter />
		<#assign fieldValue = lvf.getFormFieldValue(editSubmission, editConfiguration, fieldName) />
		customFormData.additionalDataLinksHash["datalink${counter}"]["${fieldName}"] = "${fieldValue}";
	</#list>

</#macro>


