<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Custom object property statement view for http://vivoweb.org/ontology/core#dateTimeInterval. 
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->

<#import "lib-datetime.ftl" as dt>
<#--
     Added ! statement.label?? to this if statement. Otherwise, we display the incomplete message
     even when there is a valid label (possibly via ingest), such as Spring 2010.  tlw72
-->

<#if ! statement.valueStart?? && ! statement.valueEnd?? && ! statement.label?? && ! statement.dateTime??>
    <a href="${profileUrl(statement.uri("temporalCoverage"))}" title="incomplete temporal coverage">incomplete temporal coverage</a>
<#else>
    <#if statement.label??>
        ${statement.label!} 
    <#else>
    	<#if statement.temporalCoverageNodeType?has_content>
    		<#if statement.temporalCoverageNodeType?ends_with("DateTimeInterval")>
    			${dt.dateTimeIntervalLong("${statement.dateTimeStart!}", "${statement.precisionStart!}", "${statement.dateTimeEnd!}", "${statement.precisionEnd!}")}
    		<#elseif statement.temporalCoverageNodeType?ends_with("DateTimeValue")>
    			 ${dt.formatXsdDateTimeLong(statement.dateTime, statement.precision!)}
    		<#else>
    		</#if>
    	<#else>
    		    <a href="${profileUrl(statement.uri("temporalCoverage"))}" title="incorrect object type">Temporal coverage object does not have correct type.  </a>
    	</#if>
    	
    </#if>
</#if>