<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<header role="banner">
	<div id="branding-wrapper">
	    <h1>
	        <a title="Datastar: Data Discovery to advance research" href="${urls.home}"><span class="displace">${siteName}</span></a>
	    </h1>
	
	    <nav id="header-nav" role="navigation">
	        <div class="top-border"></div>
	
	        <ul role="list">
	        	<!-- Getting rid fo this completely for now-->
	            <!--li role="listitem"><a href="${urls.base}/editRequestDispatch?typeOfNew=http%3A%2F%2Fpurl.org%2Fdatastar%2FDataset&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DatastarNewIndividualFormGenerator" title="Register">Register</a></li-->
	            
	            <#if user.loggedIn>
	               <#--Removed edit page link that was here before-->
	                <#if user.hasSiteAdminAccess>
	                    <li role="listitem"><a href="${urls.siteAdmin}" title="site admin">Site Admin</a></li>
	                </#if>
                    <li>
                        <ul class="dropdown">
                            <li id="user-menu"><a href="#" title="user">${user.loginName}</a>
                                <ul class="sub_menu">
                                     <#if user.hasProfile>
                                         <li role="listitem"><a href="${user.profileUrl}" title="my profile">My profile</a></li>
                                     </#if>
                                     <#if urls.myAccount??>
                                         <li role="listitem"><a href="${urls.myAccount}" title="my account">My account</a></li>
                                     </#if>
                                     <li role="listitem"><a href="${urls.logout}" title="log out">Log out</a></li>
                                </ul>
                            </li>
                         </ul>
                     </li>
                     ${scripts.add('<script type="text/javascript" src="${urls.base}/js/userMenu/userMenuUtils.js"></script>')}
     			<#else>
	            	<li role="listitem"><a href="${urls.login}" title="Login">Login</a></li>
	            </#if>
	          	<li role="listitem"><a href="${urls.index}" title="index">Index</a></li>
	            
	        </ul>
	    </nav>
	
	
	    <section id="search" role="region">
	        <fieldset>
	            <legend>Search form</legend>
	    
	            <form id="search-form" action="${urls.search}" name="search" role="search" accept-charset="UTF-8" method="POST"> 
	                <div id="datastar-search-field">
	                    <input type="text" name="querytext" value="${querytext!}" autocapitalize="off" />
	                    <button type="submit" onClick='javascript:this.submit();'/>
	                    
	                </div>
	            </form>
	        </fieldset>
	    </section>
	   
	    
	</div>
	
	<#include "menu.ftl"/>
	

</header>