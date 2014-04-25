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
                
        ${body}
        </section>
        <#include "footer.ftl">
    </body>
</html>