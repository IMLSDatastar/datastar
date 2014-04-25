<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for core:webpage.
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->
<#assign linkText>
    Data: <#if statement.anchor?has_content>${statement.anchor}<#t>
    <#elseif statement.url?has_content>${statement.url}<#t>
    </#if>    
    <#if statement.format?has_content> (${statement.format})</#if>
</#assign>


<#if statement.url?has_content>
    <a href="${statement.url}" title="link text">${linkText}</a> 
<#else>
    <a href="${profileUrl(statement.uri("link"))}" title="link name">${statement.linkName}</a> (no url provided for link)
</#if>