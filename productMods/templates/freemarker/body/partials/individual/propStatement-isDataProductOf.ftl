<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- VIVO-specific default object property statement template. 
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->

<@showStatement statement />

<#macro showStatement statement>
   
    <a href="${profileUrl(statement.uri("object"))}" title="name">${statement.label!statement.localName!}</a> <#if statement.title?has_content>${statement.title?cap_first}<#elseif statement.type?has_content>${statement.type?cap_first}</#if>
	<#if statement.description?has_content>
	 <br/> ${statement.description!}
	 </#if>
</#macro>


