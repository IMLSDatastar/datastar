/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var addDataLinksForNewDataset = {
    /* *** Initial page setup *** */
   
    onLoad: function() {
        
        if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }        
        this.mixIn();
        this.initObjects();                 
        this.initPage();       
    },

    disableFormInUnsupportedBrowsers: function() {       
        var disableWrapper = $('#ie67DisableWrapper');
        
        // Check for unsupported browsers only if the element exists on the page
        if (disableWrapper.length) {
            if (vitro.browserUtils.isIELessThan8()) {
                disableWrapper.show();
                $('.noIE67').hide();
                return true;
            }
        }            
        return false;      
    },
        
    mixIn: function() {
        // Mix in the custom form utility methods
        $.extend(this, vitro.customFormUtils);
        
        // Get the custom form data from the page
        $.extend(this, customFormData);
    },
    
    // On page load, create references for easy access to form elements.
    // NB These must be assigned after the elements have been loaded onto the page.
    initObjects: function() {
        
        this.form = $('#addAuthorForm');
        this.dataLinkSection = $("#dataLinks");
        this.showFormButtonWrapper = $('#showAddDataForm');
        this.showFormButton = $('#showAddDataFormButton');
    },
    
    // Initial page setup. Called only at page load.
    initPage: function() {            
        
        this.bindEventListeners();
        
        //if validation errors, will need to display the additional data links as well
        if (this.findValidationErrors()) {
            this.initFormAfterInvalidSubmission();
        } 
    },
    initFormAfterInvalidSubmission:function() {
    	if(this.dataFieldsCounter > 1) {
    		this.handleErrorViewWithAdditionalLinks();
    	}
    },
    
    /* *** Event listeners *** */
    
    bindEventListeners: function() {
        
        this.showFormButton.click(function() {
         //   addAuthorForm.initFormView();
         //   return false;
        	addDataLinksForNewDataset.addAdditionalDataLinks();
        	return false;
        });     

    },
  
	addAdditionalDataLinks: function() {
		addDataLinksForNewDataset.dataFieldsCounter++;
		var newId =  "mainDataLinksForm" + addDataLinksForNewDataset.dataFieldsCounter;
    	var clonedFields = $("#mainDataLinksForm").clone();
    	clonedFields.attr("id", newId);
    	addAuthorForm.renameIds(clonedFields, addDataLinksForNewDataset.dataFieldsCounter);
    	addAuthorForm.clearInputs(clonedFields);
    	//bind event listeners
    	//no event listeners here
    	//addAuthorForm.bindEventListenersForClone(clonedFields, addAuthorForm.authorFieldsCounter);
    	//append
    	clonedFields.appendTo($("#mainDataLinksContainer"));
    	//Now get by retrieving element
    	var cloned = $("#" + newId);
    	
    	//Add a new rank field element
    	var newrank = parseInt(addDataLinksForNewDataset.newDataRank) + parseInt(addDataLinksForNewDataset.dataFieldsCounter - 1);
    	var newRankInput = cloned.find("input[name='dataRank" + addDataLinksForNewDataset.dataFieldsCounter + "']");
    	newRankInput.val(newrank);
		
		
		addDataLinksForNewDataset.modifyConfiguration(addDataLinksForNewDataset.dataFieldsCounter);
	},
    
    //the data links being added
	modifyConfiguration:function(counter) {
		
		var urlString = addDataLinksForNewDataset.editAJAXUrl; //main AJAX url is always the same
		var editKey = addDataLinksForNewDataset.editKey;
		var generator = addDataLinksForNewDataset.editAJAXGenerator;
		var action = "DataLink";
        $.ajax({
            url: urlString,
            dataType: 'json',
            data: {
                editKey:editKey,
                counter:counter,
                generator:generator,
                action: action
            }, 
            complete: function(xhr, status) {
                // Not sure why, but we need an explicit json parse here. jQuery
                // should parse the response text and return a json object.
                var results = jQuery.parseJSON(xhr.responseText);
               
            }

        });
	},
	//***Methods for handling form view errors**/
	handleErrorViewWithAdditionalLinks:function() {
		var i = 2;
		for(i = 2; i <=addDataLinksForNewDataset.dataFieldsCounter; i++) {
			addDataLinksForNewDataset.displayDataLinkInFormWithErrors(i);
			addDataLinksForNewDataset.populateDataLinkInFormWithErrors(i);
		}
	},
	displayDataLinkInFormWithErrors:function(counter) {
		var newId =  "mainDataLinksForm" +counter;
    	var clonedFields = $("#mainDataLinksForm").clone();
    	clonedFields.attr("id", newId);
    	addAuthorForm.renameIds(clonedFields, counter);
    	addAuthorForm.clearInputs(clonedFields);
    	clonedFields.appendTo($("#mainDataLinksContainer"));
	},
	populateDataLinkInFormWithErrors:function(counter) {
		//populate this with the information that has been returned
		var newId =  "mainDataLinksForm" +counter;
		var cloned = $("#" + newId);
		//fields = lastname firstname middlename orgname personuri orguri rank
		var dataLinksHash = addDataLinksForNewDataset.additionalDataLinksHash;
		var dataLinkKey = "datalink" + counter;
		if(dataLinkKey in dataLinksHash ) {
			var dataFieldMap = dataLinksHash[dataLinkKey];
			for(var dataFieldName in dataFieldMap) {
				var dataFieldValue = dataFieldMap[dataFieldName];
				//if there is a value, then find that input with that name within the form and set the value
				if(dataFieldValue != "") {
					cloned.find("input[name='" + dataFieldName + "']").val(dataFieldValue);
				}
			}
			
		}			
	}
};

$(document).ready(function() {   
    addDataLinksForNewDataset.onLoad();

}); 
