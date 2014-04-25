<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- This snippet will be included in lib-vivo-properties.ftl, so users will be able to have a 
    different view when extending wilma theme
-->
<#-- This file is specifically for seeing webpage/metadata information for a dataset, so it is important that we are able to override
the regular generator and pass in the generator we wish to use-->

<ul class="${linkListClass}" id="webpages" role="list">
	 <#if webpage.statements?has_content>
    	<@p.objectPropertyList webpage editable />
    </#if>
     <#if hasAssociatedMetadata.statements?has_content>
    	<@p.objectPropertyList hasAssociatedMetadata editable />
    <#else>
    	No metadata links	
    </#if>
</ul>
