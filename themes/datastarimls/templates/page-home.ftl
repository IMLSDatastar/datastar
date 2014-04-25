<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#import "lib-list.ftl" as l>

<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
    </head>
    
    <body class="${bodyClasses!}">
        <#include "identity.ftl">
        <#--Menu is included within identity.ftl-->        
         <section id="content">
            <h2>Welcome to Datastar</h2>

            <p>Datastar is a dataset registry tool that enables collaboration among scientists across all disciplines.</p>
				
			<h4><a href="${urls.base}/editRequestDispatch?typeOfNew=http%3A%2F%2Fpurl.org%2Fdatastar%2FDataset&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DatastarNewIndividualFormGenerator" 
			title="Add New Dataset"> <img 
			src="${urls.base}/images/edit_icons/add.gif">Add New Dataset</a></h4>
        	<p>Browse or search information on datasets, people, reseach groups, and related publications.</p>
        	
        
        
        </section>

        
        <#include "footer.ftl">
    </body>
</html>