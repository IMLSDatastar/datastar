<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#import "lib-vivo-form.ftl" as lvf>
<#import "addStudyMethodsUtils.ftl" as asm>

<#--Retrieve certain edit configuration information-->
<#assign literalValues = editConfiguration.existingLiteralValues />
<#assign uriValues = editConfiguration.existingUriValues />
<#assign htmlForElements = editConfiguration.pageData.htmlForElements />
<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
</#if>
<#--Freemarker variables with default values that can be overridden by specific forms-->

<#assign blankSentinel = "" />
<#if editConfigurationConstants?has_content && editConfigurationConstants?keys?seq_contains("BLANK_SENTINEL")>
	<#assign blankSentinel = editConfigurationConstants["BLANK_SENTINEL"] />
</#if>

<#--This flag is for clearing the label field on submission for an existing object being selected from autocomplete.
Set this flag on the input acUriReceiver where you would like this behavior to occur. -->
<#assign flagClearLabelForExisting = "flagClearLabelForExisting" />

<#-- If form is submitted, some of these values may be filled out already -->
<#assign editMode = "add"/>
<#assign existingStudyDesignExecutionUri = lvf.getFormFieldValue(editSubmission, editConfiguration, "studyDesignExecution") />
<#assign existingMethodsUri = lvf.getFormFieldValue(editSubmission, editConfiguration, "methods") />
<#assign existingMethodStepUri = lvf.getFormFieldValue(editSubmission, editConfiguration, "methodStep") />
<#assign methodStepValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "methodStepValue") />
<#assign methodStepOrderValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "methodStepOrder") />
<#assign newMethodStepOrder = editConfiguration.pageData.newMethodStepOrder />
<#assign methodStepsCounter = editConfiguration.pageData.methodStepsCounter />
<#if methodStepsCounter = 0>
	<#--if no method steps added, first method step is already set up to be handled, so the counter should start at 1-->
	<#assign methodStepsCounter = 1 />
<#else>
	<#assign editMode = "edit"> <#--if more than one method step, then we are editing-->	
</#if>
<#if !methodStepOrderValue?has_content || methodStepOrderValue = "">
	<#assign methodStepOrderValue = newMethodStepOrder />
</#if>

<#--Setting values for titleVerb, submitButonText, and disabled Value-->
<#assign titleVerb = "Add"/>
<#if editMode = "edit">
	<#assign titleVerb = "Edit"/>
</#if>
<#assign submitButtonText = "Save" />

<#--Get existing value for specific data literals and uris-->


<#assign requiredHint = "<span class='requiredHint'> *</span>" />
<#assign yearHint     = "<span class='hint'>(YYYY)</span>" />

<#--This file includes 'required hint' from above so included here-->


<h2>${titleVerb}&nbsp; Study Methods for ${editConfiguration.subjectName}</h2>

<#--Display error messages if any-->

<#assign errorDisplayStyle = "style='display:none;'"/>
<#if submissionErrors?has_content>
	<#assign errorDisplayStyle = ""/>
	
</#if>
<section id="error-alert" role="alert" ${errorDisplayStyle}>
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>
        <#--below shows examples of both printing out all error messages and checking the error message for a specific field-->
        <#if submissionErrors?has_content>
	        <#list submissionErrors?keys as errorFieldName>
	        	<#if errorFieldName?starts_with("methodStepValue")>
	        		All steps must contain text.  
	        	</#if>
	        	
	        </#list>
        </#if>
        </p>
</section>

<@lvf.unsupportedBrowser urls.base /> 

<section id="addStudyMethods" role="region">        
    
    <form id="addStudyMethodsForm" class="customForm noIE67" action="${submitUrl}"  role="addStudyMethodsForm">
    <input type="hidden" name="studyDesignExecution" id="studyDesignExecution" value="${existingStudyDesignExecutionUri}" /> 
	<input type="hidden" name="methods" id="methods" value="${existingMethodsUri}" /> 
   <div class="fullViewOnly">    
   	    
        <p>
        	<div id="methodContainer" name="methodContainer">
            	<div id="methodFields" name="methodFields">
            		<label for="methodStepValue"> Step <span name="stepnumber">1</span> ${requiredHint}</label>
            		<textarea class="display" id="methodStepValue" name="methodStepValue">${methodStepValue}</textarea>
            		<input type="hidden" id="methodStepOrder" name="methodStepOrder" value="${methodStepOrderValue}" />
            		<input type="hidden" id="methodStep" name="methodStep" value="${existingMethodStepUri}" />
            		<span id="removeLink">
            		<a href="${urls.base}/edit/primitiveDelete" class="remove" title="remove author link" uri="${existingMethodStepUri}">Remove</a>
            		</span>
            	</div>
            	
            	<#if (methodStepsCounter > 1)>
            		<@asm.handleAdditionalMethodSteps methodStepsCounter requiredHint/>
            	</#if>
            	
        	</div>
        </p>

	     <div id="showAddForm" role="region">
		    <input type="submit" id="showAddFormButton" value="Add Another Method Step" role="button" />
		    <img id="indicatorOne" class="indicator hidden" title="one" src="${urls.base}/images/indicatorWhite.gif" />
		</div> 
    
   </div>
    <p class="submit">
        <input type="hidden" id="editKey" name="editKey" value="${editKey}" />
        <input type="submit" id="submit" value="${submitButtonText}"/><span class="or"> or </span><a class="cancel" href="${cancelUrl}" title="Cancel">Cancel</a>
    </p>

    <p id="requiredLegend" class="requiredHint">* required fields</p>
    </form>

<#--Specifying form-specific script and adding stylesheets and scripts-->    
    
 <script type="text/javascript">
	var customFormData  = {
	    acUrl: '${urls.base}/autocomplete?tokenize=true',
	    defaultTypeName: 'object producing dataset', // used in repair mode, to generate button text and org name field label
	    baseHref: '${urls.base}/individual?uri=',
	    methodStepsCounter: '${methodStepsCounter}',
	    originalMethodStepsCount: '${methodStepsCounter}',
	    editKey: '${editKey}',
	    editAJAXUrl: '${urls.base}/editRequestAJAX',
	    editAJAXGenerator: 'AddStudyMethodsAJAXGenerator',
	    blankSentinel: '${blankSentinel}',
	   	editMode: '${editMode}'	};
	</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/addDynamicFieldsUtils.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/addStudyMethods.js"></script>')}

</section>   