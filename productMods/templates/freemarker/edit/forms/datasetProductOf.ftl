<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#import "lib-vivo-form.ftl" as lvf>

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
<#assign objectLabelValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "objectLabel") />
<#assign objectLabelDisplayValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "objectLabelDisplay") />
<#assign existingObjectURIValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "existingObjectURI") />
<#assign objectDescriptionValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "objectDescription") />
<#assign projectDescriptionValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "projectDescription") />
<#assign objectTypeValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "objectType") />


<#--Setting values for titleVerb, submitButonText, and disabled Value-->
<#if editConfiguration.objectUri?has_content>
	<#assign titleVerb = "Edit"/>
	<#assign editMode = "edit" />
	<#assign submitButtonText>Save Changes</#assign>
	
<#else>
	<#assign titleVerb = "Create"/>
	<#assign submitButtonText>Create Entry</#assign>
	<#assign disabledVal = ""/>
	<#assign editMode = "add" />
</#if>

<#--Get existing value for specific data literals and uris-->


<#assign requiredHint = "<span class='requiredHint'> *</span>" />
<#assign yearHint     = "<span class='hint'>(YYYY)</span>" />

<h2>${titleVerb}&nbsp; investigation or project producing ${editConfiguration.subjectName}</h2>

<#--Display error messages if any-->
<#if activityLabelDisplayValue?has_content >
    <#assign activityLabelValue = activityLabelDisplayValue />
</#if>


<#if submissionErrors?has_content>
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>
        <#--below shows examples of both printing out all error messages and checking the error message for a specific field-->
        <#list submissionErrors?keys as errorFieldName>
        	
        </#list>
        <#--Checking if Name field is empty-->
        
        </p>
    </section>
</#if>

<@lvf.unsupportedBrowser urls.base /> 

<section id="addDataProduct" role="region">        
    
    <form id="addDataProductForm" class="customForm noIE67" action="${submitUrl}"  role="addDatasetProductOf">

       <p class="inline">
        <label for="typeSelector">Select type of object that produced dataset</label>
        <#--Code below allows for selection of first 'select one' option if no activity type selected-->
      
            <select id="typeSelector" name="objectType" acGroupName="object">
                	<option value="">Select type</option>
                    <option value="http://vivoweb.org/ontology/core#Project" <#if "http://vivoweb.org/ontology/core#Project" = objectTypeValue> selected="selected" </#if>>Project</option>
                    <option value="http://purl.org/datastar/Investigation" <#if "http://purl.org/datastar/Investigation" = objectTypeValue> selected="selected" </#if>>Investigation</option>
            </select>
       </p>
       
       
   <div class="fullViewOnly">        
            <p>
                <label for="w">### Name ${requiredHint}</label>
                <input class="acSelector" size="50"  type="text" id="object" name="objectLabel"  acGroupName="object" value="${objectLabelValue}" />
                <input class="display" type="hidden" id="objectLabelDisplay" acGroupName="object" name="objectLabelDisplay" value="${objectLabelDisplayValue}">
            	<span id="investigationFields">
            		<label for="objectDescription">Description ${requiredHint}</label>
            		<textarea class="display" type="hidden" id="objectDescription" acGroupName="object" name="objectDescription">${objectDescriptionValue}</textarea>
            	</span>
            	<span id="projectFields">
            		<label for="projectDescription">Description ${requiredHint}</label>
            		<textarea class="display" type="hidden" id="projectDescription" acGroupName="object" name="projectDescription">${projectDescriptionValue}</textarea>
            	</span>
            </p>
            
            

            <div class="acSelection" acGroupName="object">
                <p class="inline">
                    <label></label>
                    <span class="acSelectionInfo"></span>
                    <a href="/vivo/individual?uri=" class="verifyMatch" title="verify match">(Verify this match</a> or 
                    <a href="#" class="changeSelection" id="changeSelection">change selection)</a>

                </p>
                    <input class="acUriReceiver" type="hidden" id="existingObjectURI" name="existingObjectURI"  value="${existingObjectURIValue}" ${flagClearLabelForExisting}="true" />
                    <!-- Field value populated by JavaScript -->
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
	    editMode: '${editMode}',
	    defaultTypeName: 'object producing dataset', // used in repair mode, to generate button text and org name field label
	    baseHref: '${urls.base}/individual?uri=',
        blankSentinel: '${blankSentinel}',
        flagClearLabelForExisting: '${flagClearLabelForExisting}',
        formSteps:2
	};
	</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customFormWithAutocomplete.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/customFormWithAutocomplete.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/datasetProductOf.js"></script>')}

</section>   