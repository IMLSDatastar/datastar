<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for adding/editing time values -->

<#--Retrieve certain edit configuration information-->
<#assign editMode = editConfiguration.pageData.editMode />
<#assign htmlForElements = editConfiguration.pageData.htmlForElements />
<#assign temporalCoverageType = editConfiguration.pageData.temporalCoverageType />

<#if editMode == "edit">        
        <#assign titleVerb="Edit">        
        <#assign submitButtonText="Edit Date/Time Value">
        <#assign disabledVal="disabled">
<#else>
        <#assign titleVerb="Create">        
        <#assign submitButtonText="Create Date/Time Value">
        <#assign disabledVal=""/>
</#if>
<#assign yearHint     = "<span class='hint'>(YYYY)</span>" />

<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
</#if>


<h2>${titleVerb} temporal coverage for ${editConfiguration.subjectName}</h2>

<#--Display error messages if any-->
<#if submissionErrors?has_content>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>
        <#--below shows examples of both printing out all error messages and checking the error message for a specific field-->
        <#list submissionErrors?keys as errorFieldName>
        	<#if errorFieldName == "startField">
        	    <#if submissionErrors[errorFieldName]?contains("before")>
        	        The Start interval must be earlier than the End interval.
        	    <#else>
        	        ${submissionErrors[errorFieldName]}
        	    </#if>
        	    <br />
        	<#elseif errorFieldName == "endField">
    	        <#if submissionErrors[errorFieldName]?contains("after")>
    	            The End interval must be later than the Start interval.
    	        <#else>
    	            ${submissionErrors[errorFieldName]}
    	        </#if>
	        </#if>
        </#list>
        </p>
    </section>
</#if>

<form id="temporalForm" name="temporalForm" class="customForm" action ="${submitUrl}" class="customForm">
<p></p>
<#--Need to draw edit elements for dates here-->
<h4>Please enter a time interval OR a single date and time value </h4>
<input type="radio" name="temporalCoverageType" value="dateTimeInterval" <#if temporalCoverageType = "dateTimeInterval">checked="checked"</#if>> Interval
OR <input type="radio" name="temporalCoverageType" value="dateTimeValue" <#if temporalCoverageType = "dateTimeValue">checked="checked"</#if>> Single Date/Time
<div id="intervalForm">
 <#if htmlForElements?keys?seq_contains("startField")>
 	<label class="dateTime" for="startField">Start</label>
	${htmlForElements["startField"]} ${yearHint}
 </#if>
 <br /><br />
 <#if htmlForElements?keys?seq_contains("endField")>
	<label class="dateTime" for="endField">End</label>
	${htmlForElements["endField"]} ${yearHint}
 </#if>
 </div>
 
 <br/><br/>
<div id="singleDateForm">
  <#if htmlForElements?keys?seq_contains("dateTimeField")>
		${htmlForElements["dateTimeField"]} ${yearHint}
 </#if>
</div>
    <p class="submit">
        <input type="hidden" name="editKey" value="${editKey}" />
        <input type="submit" id="submit" value="${submitButtonText}" role="button" />
    
        <span class="or"> or </span>
    
        <a class="cancel" href="${editConfiguration.cancelUrl}" title="Cancel">Cancel</a>
    </p>
</form>


  <script type="text/javascript">
  	<!--Form validation depends on whether date time value or date time interval are selected-->
	var customFormData = {
	    editKey: '${editKey}',
	    editAJAXUrl: '${urls.base}/editRequestAJAX',
	    editAJAXGenerator: 'TemporalCoverageFormAJAXGenerator'
	};
	</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/utils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/temporalCoverage.js"></script>')}