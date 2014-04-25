<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<nav id="main-nav" role="navigation">
    <ul role="list">
        <#list menu.items as item>
            <li role="listitem"><a href="${item.url}" title="${item.linkText?lower_case}" <#if item.active> class="active" </#if>>${item.linkText}</a></li>
        </#list>
    </ul>
</nav>