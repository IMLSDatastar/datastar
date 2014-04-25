<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Custom form for adding authors to information resources -->

<#import "lib-vivo-form.ftl" as lvf>
<#----Heading and any Submission errors===-->
<#--If edit submission exists, then retrieve validation errors if they exist-->
<#if editSubmission?has_content && editSubmission.submissionExists = true && editSubmission.validationErrors?has_content>
	<#assign submissionErrors = editSubmission.validationErrors/>
</#if>

<#--Retrieve certain edit configuration information-->
<#assign typeName = editConfiguration.pageData.typeName />
<#--Get existing value for specific data literals and uris - helpful if error on submission-->
<#assign datasetLabelValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "datasetLabel")/>
<#assign datasetDescriptionValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "datasetDescription")/>

<#--------------Authors Specific Variables----------------->

<#--Retrieve certain page specific information information-->
<#assign newRank = editConfiguration.pageData.newRank />
<#assign rankPredicate = editConfiguration.pageData.rankPredicate />

<#--Submission values for these fields may be returned if user did not fill out fields for new person-->
<#assign lastNameValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "lastName") />
<#assign firstNameValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "firstName") />
<#assign middleNameValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "middleName") />
<#assign orgNameValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "orgName") />
<#assign existingPersonUri = lvf.getFormFieldValue(editSubmission, editConfiguration, "personUri") />
<#assign existingOrgUri = lvf.getFormFieldValue(editSubmission, editConfiguration, "orgUri") />
<#assign authorCounter = editConfiguration.pageData.authorCounter />
<#assign selectedAuthorLabel = lvf.getFormFieldValue(editSubmission, editConfiguration, "selectedAuthorLabel") />

<#--------------Publication date specific variables -->
<#assign htmlForElements = editConfiguration.pageData.htmlForElements />
<#assign datalinkCounter = editConfiguration.pageData.datalinkCounter />

<#-------------- Repository specific variables -->
<#assign repositoryOptions = editConfiguration.pageData.repository />
<#assign selectedRepositoryOption = lvf.getFormFieldValue(editSubmission, editConfiguration, "repository") />

<#-------------- Data link specific variables -->
<#assign url = lvf.getFormFieldValue(editSubmission, editConfiguration, "url")/>
<#assign urlTypeValue = lvf.getFormFieldValue(editSubmission, editConfiguration, "urlType")/>
<#assign linkLabel = lvf.getFormFieldValue(editSubmission, editConfiguration, "linkLabel") />
<#assign dataLinkNewRank = editConfiguration.pageData.dataLinkNewRank />
<#assign dataFileFormat = lvf.getFormFieldValue(editSubmission, editConfiguration, "dataFileFormat") />


<h2>Add a new ${typeName}</h2>

<@lvf.unsupportedBrowser urls.base/>


<#if submissionErrors?has_content >
    <section id="error-alert" role="alert">
        <img src="${urls.images}/iconAlert.png" width="24" height="24" alert="Error alert icon" />
        <p>
        <#list submissionErrors?keys as errorFieldName>
    	    <#if errorFieldName == "firstName">
    	        Please enter a First Name for the first author.
    	    <#elseif  errorFieldName == "lastName">
    	        Please enter a Last Name for the first author.
        	<#elseif  errorFieldName == "label">
        	    Please enter a value in the name field.
        	<#elseif  errorFieldName == "datasetLabel">
        	    Please enter a value for the dataset title.  
        	<#elseif  errorFieldName == "datasetDescription">    
        		Please enter a value for the dataset description.   
    	    <#else>
    	    	${errorFieldName} : ${submissionErrors[errorFieldName]}
    	    </#if>
    	    <br />
    	</#list>
        </p>
    </section>
</#if>

<#assign requiredHint = "<span class='requiredHint'> *</span>" />
<#assign ulClass = ""/>
<#assign initialHint="<span class='hint'>(initial okay)</span>" />



<#---New Dataset---->






<section id="newIndividual" role="region">     


    
    <form id="addAuthorForm" class="customForm noIE67" action="${submitUrl}"  role="add new individual">
 		
        <p>
            <label for="name">Name ${requiredHint}</label>
            <input size="30"  type="text" id="datasetLabel" name="datasetLabel" value="${datasetLabelValue}" />
        </p>
        
         <p>
            <label for="name">Description/Abstract ${requiredHint}</label>
            <textarea rows="8" type="text" id="datasetDescription" name="datasetDescription" value="${datasetDescriptionValue}" role="textarea"></textarea>
        </p>
        
        
    <h4>Author(s)</h4>
	<!--We're identifying this section to enable copying this over-->
	<div id="authorsContainer">
	<div id="mainAuthorFields">

    <div style="display:inline">
        <input type="radio" name="authorType" class="person-radio" value="" role="radio" checked style="display:inline;margin-top:20px" />
        <label class="inline" for="Person" >Person</label>
        <input type="radio" name="authorType" class="org-radio" value="http://xmlns.com/foaf/0.1/Organization" role="radio" style="display:inline;margin-left:18px" />
        <label class="inline" for="Organization">Organization</label>
        <input type="hidden" name="selectedAuthorLabel" id="selectedAuthorLabel" value="${selectedAuthorLabel}" />
    </div>

    <section id="personFields" role="personContainer">
    		<#--These wrapper paragraph elements are important because javascript hides parent of these fields, since last name
    		should be visible even when first name/middle name are not, the parents should be separate for each field-->
    		<p class="inline">
        <label for="lastName">Last name <span class='requiredHint'> *</span></label>
        <input class="acSelector" size="35"  type="text" id="lastName" name="lastName" value="${lastNameValue}" role="input" />
        </p>
				
				<p class="inline">
        <label for="firstName">First name ${requiredHint} ${initialHint}</label>
        <input  size="20"  type="text" id="firstName" name="firstName" value="${firstNameValue}"  role="input" />
        </p>
        
				<p class="inline">
				<label for="middleName">Middle name <span class='hint'>(initial okay)</span></label>
        <input  size="20"  type="text" id="middleName" name="middleName" value="${middleNameValue}"  role="input" />
        </p>
      
        <div id="selectedAuthor" class="acSelection">
            <p class="inline">
                <label>Selected author:&nbsp;</label>
                <span class="acSelectionInfo" id="selectedAuthorName"></span>
                <a href="${urls.base}/individual?uri=" id="personLink" class="verifyMatch"  title="verify match">(Verify this match)</a> or 
            	<a href="#" class="changeSelection" id="changePersonSelection">change selection)</a>
                <input type="hidden" id="personUri" name="personUri" value="${existingPersonUri}"  role="input" /> <!-- Field value populated by JavaScript -->
            </p>
        </div>
    </section>
    <section id="organizationFields" role="organization">
    		<p class="inline">
        <label for="orgName">Organization name <span class='requiredHint'> *</span></label>
        <input size="38"  type="text" id="orgName" name="orgName" value="${orgNameValue}" role="input" />
        </p>
				      
        <div id="selectedOrg" class="acSelection">
            <p class="inline">
                <label>Selected organization:&nbsp;</label>
                <span  id="selectedOrgName"></span>
                <a href="${urls.base}/individual?uri=" id="orgLink"  title="verify match">(Verify this match)</a> or 
            	<a href="#" class="changeSelection" id="changeOrgSelection">change selection)</a>
                <input type="hidden" id="orgUri" name="orgUri" value="${existingOrgUri}"  role="input" /> <!-- Field value populated by JavaScript -->
            </p>
        </div>
    </section>

    <input type="hidden" id="label" name="label" value=""  role="input" />  <!-- Field value populated by JavaScript -->
	<input type="hidden" name="rank" id="rank" value="${newRank}" role="input" />
    </div>
    </div>
    <!--To enable addition of other authors - this should dynamically enable updating the configuration
    if they click save-->
    <br/>
    <div id="showAddForm" role="region">
	    <input type="submit" id="showAddFormButton" value="Add Another Author" role="button" />
	    <img id="indicatorOne" class="indicator hidden" title="one" src="${urls.base}/images/indicatorWhite.gif" />
	</div> 
    
    
    <#-------------------------------Publication Date-------------------------------------------->
    <section id="publicationDateSection">
    <#if htmlForElements?keys?seq_contains("publicationDateField")>
		<h4> Publication Date:</h4> ${htmlForElements["publicationDateField"]}
 	</#if>
    </section>
    
    
    <#-------------------------------Data Link-------------------------------------------->
    <section id="dataLinks">
    <input type="hidden" name="urlType" id="urlType" value="http://purl.org/datastar/DatasetURLLink" />
    <div id="mainDataLinksContainer">
    <div id="mainDataLinksForm">
    <h4> Data URL Location</h4>
    If entering a data URL location, the URL is required. 
   
    <#--This included a select before but have removed that as currently one URL link type supported here-->
         
    <label for="url">URL </label>
    <input  size="70"  type="text" id="url" name="url" value="${url}" role="input" />
   
    <label for="linkLabel">Webpage Name</label>
    <input  size="70"  type="text" id="linkLabel" name="linkLabel" value="${linkLabel}" role="input" />
    
    <label forproperty="dataFileFormat" for="dataFileFormat">Data file format</label>
    <input  forproperty="dataFileFormat" size="70"  type="text" id="dataFileFormat" name="dataFileFormat" value="${dataFileFormat!}" role="input" />
	<input type="hidden" name="dataRank" value="${dataLinkNewRank}" />
    </div>
    </div>
    <!--To enable addition of other authors - this should dynamically enable updating the configuration
    if they click save-->
    <br/>
    <div id="showAddDataForm" role="region">
	    <input type="submit" id="showAddDataFormButton" value="Add Another Data URL Location" role="button" />
	    <img id="indicatorOne" class="indicator hidden" title="one" src="${urls.base}/images/indicatorWhite.gif" />
	</div> 
    
    </section>
    
    <#-------------------------------Repository-------------------------------------------->
    <h4> Repository </h4>
    <section id="repository">
    
    <#if repositoryOptions?has_content && ((repositoryOptions?keys?size) > 0) >
    	<#assign repositoryOptionsKeys = repositoryOptions?keys />
    	<select id="repository" name="repository" role="select">
     	<#list repositoryOptionsKeys as key>
                 <option value="${key}" role="option" <#if key = selectedRepositoryOption>selected="selected"</#if>>${repositoryOptions[key]}</option>
     	</#list>
    	</select>
    </#if>
    
   
    </section>
    
    
    <#--Custom form data to be passed to script-->
    <#import "newDatasetFormUtils.ftl" as ndf>
    
    <script type="text/javascript">
	var customFormData = {
	    rankPredicate: '${rankPredicate}',
	    acUrl: '${urls.base}/autocomplete?type=',
	    tokenize: '&tokenize=true',
	    personUrl: 'http://xmlns.com/foaf/0.1/Person',
	    orgUrl: 'http://xmlns.com/foaf/0.1/Organization',
	    reorderUrl: '${urls.base}/edit/reorder',
	    baseUrl: '${urls.base}',
	    editKey: '${editKey}',
	    editAJAXUrl: '${urls.base}/editRequestAJAX',
	    editAJAXGenerator: 'NewDatasetAJAXGenerator',
	    newRank: '${newRank}', //This enables added authors to then get a new rank based on this 
		newDataRank: '${dataLinkNewRank}',  //for data links
		authorFieldsCounter: '${authorCounter}',  //already within javascript for adding authors, so using that same field
		dataFieldsCounter: '${datalinkCounter}',
		selectedAuthorLabelValue: '${selectedAuthorLabel}'
	};
	//This is included here to allow the form to send information
	//in case of validation errors if multiple authors and/or data links were included
	<#--Call macros-->
	<@ndf.handleAdditionalAuthorsForFormWithErrors authorCounter />
	<@ndf.handleAdditionalDataLinksForFormWithErrors datalinkCounter />
	
	</script>
   		
   	      
	<#--Submission-->
    <p class="submit">
        <input type="hidden" name = "editKey" value="${editKey}"/>
        <input type="submit" id="submit" value="Add New ${typeName}"/>
        <span class="or"> or </span><a class="cancel" href="${urls.base}/siteAdmin" title="Cancel">Cancel</a>
        
    </p>

    <p id="requiredLegend" class="requiredHint">* required fields</p>

    </form>
   <!-- form id="testform">
   	<div id="blah">Test</div>
   	<div class="multiple">M</div>
   	<div class="multiple">S</div>
   	<div id="resultsDiv"></div>
   </form-->
</section>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />',
									'<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
									'<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/autocomplete.css" />',
									'<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/addAuthorsToInformationResource.css" />')}


${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.fix.clone.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/json2.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/addAuthorsForNewDataset.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/addDataLinksForNewDataset.js"></script>')}