<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

</div> <!-- #wrapper-content -->

        <footer role="contentinfo">
            <div id="footer-wrapper">
                <p class="copyright"> 
                	<#if copyright??>
			            <small>&copy;${copyright.year?c}
			            <#if copyright.url??>
			                <a href="${copyright.url}" title="copyright">${copyright.text}</a>
			            <#else>
			                ${copyright.text}
			            </#if>
		            </#if>
		        </p>
            
                <nav role="navigation">
                    <ul id="footer-nav" role="list">
                        <li class="active" role="listitem"><a href="${urls.home}" title="home">Home</a></li>
                        <li role="listitem"><a href="${urls.about}" title="about">About</a></li>
                        <li role="listitem"><a href="${urls.contact!}" title="contact us">Contact us</a></li>
                        <li role="listitem"><a href="${urls.termsOfUse}" title="terms of use">Terms of use</a></li>
                    </ul>
                </nav>
            </div>
        </footer>   
<#include "scripts.ftl">