<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<meta charset="utf-8" />
<!-- Google Chrome Frame open source plug-in brings Google Chrome's open web technologies and speedy JavaScript engine to Internet Explorer-->
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="robots" content="index,follow" />
<meta name="keywords" content="" />
<meta name="description" content="" />
<title>${title}</title>

<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<!--[if (gte IE 6)&(lte IE 8)]>
    <script type="text/javascript" src="js/selectivizr-min.js"></script>
<![endif]-->


<#include "stylesheets.ftl">
<link rel="stylesheet" href="${urls.theme}/css/normalize.css" />


<!--link rel="stylesheet" href="${urls.theme}/css/screen.css" /-->
<link rel="stylesheet" type="text/css" href="${urls.theme}/css/fonts.css"> 
<link rel="stylesheet" href="${urls.theme}/css/styles.css" />
<link rel="stylesheet" href="${urls.theme}/css/datastarTest.css" />

<#include "headScripts.ftl">


  <!--[if IE 7]>
            <link rel="stylesheet" href="${urls.theme}/css/ie7.css" />
        <![endif]-->

        <!--[if IE 8]>
            <link rel="stylesheet" href="${urls.theme}/css/ie8.css" />
        <![endif]-->


<#-- Inject head content specified in the controller. Currently this is used only to generate an rdf link on 
an individual profile page. -->
${headContent!}

<link rel="shortcut icon" type="image/x-icon" href="${urls.base}/favicon.ico">