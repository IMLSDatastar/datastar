<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Assumptions - that the lib vivo freemarker has been included as lvf already, and that edit configuration and edit submission available-->
<#macro handleAdditionalMethodSteps numberMethodSteps requiredHint>
	<#if (numberMethodSteps > 1)>
		<#list 2..numberMethodSteps as i>
			<@createTemplateForMethodStep i requiredHint/>
		</#list>
	</#if>
</#macro>

<#macro createTemplateForMethodStep methodStepCount requiredHint>
	<#assign stepValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "methodStepValue" + methodStepCount)/>
	<#assign orderValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "methodStepOrder" + methodStepCount)/>
	<#assign methodStepUri = lvf.getFormFieldValue(editSubmission, editConfiguration, "methodStep" + methodStepCount)/>
	<div id="methodFields${methodStepCount}" name="methodFields${methodStepCount}">
		<label for="methodStepValue${methodStepCount}"> Step <span name="stepnumber${methodStepCount}">${methodStepCount}</span> ${requiredHint}</label>
		<textarea class="display" id="methodStepValue${methodStepCount}" name="methodStepValue${methodStepCount}">${stepValue}</textarea>
		<input type="hidden" id="methodStepOrder${methodStepCount}" name="methodStepOrder${methodStepCount}" value="${orderValue}" />
		<input type="hidden" id="methodStep${methodStepCount}" name="methodStep${methodStepCount}" value="${methodStepUri}" />
		<a href="${urls.base}/edit/primitiveDelete" class="remove" title="remove author link" uri="${methodStepUri}">Remove</a>
		
	</div>

</#macro>
