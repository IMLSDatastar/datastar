/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var temporalCoverage = {	
	
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
    // Initial page setup. Called only at page load.
    initPage: function() {
       this.initDisplay();
        this.bindEventListeners();
    },
    initObjects:function(){
    	this.temporalForm = $("#temporalForm");
    	this.intervalForm = $("#intervalForm");
    	this.singleDateForm = $("#singleDateForm");
    	this.temporalTypeRadio = $("input[name='temporalCoverageType']");
    }, 
    initDisplay:function() {
    	//check what is selected on the form and toggle accordingly
    	this.toggleFormDisplay();
    },
    bindEventListeners: function() {
    	this.temporalTypeRadio.change(function() {
    		temporalCoverage.toggleFormDisplay();
    		//modifies edit configuration on this basis
    		temporalCoverage.handleType();
    	});
    },
    toggleFormDisplay:function() {
    	//this needs to be executed each time b/c saving it will only save what was checked
    	//at the time the expression was evaluted
    	var selectedTypeValue =  $("input[name='temporalCoverageType']:checked").val();
    	if(selectedTypeValue == "dateTimeInterval") {
    		temporalCoverage.intervalForm.removeClass("hidden");
    		temporalCoverage.singleDateForm.addClass("hidden");
    		//clear out the single date value
    		$("input[name^='dateTimeField'], select[name^='dateTimeField']").val("");
    	} else if(selectedTypeValue == "dateTimeValue") {
    		temporalCoverage.intervalForm.addClass("hidden");
    		temporalCoverage.singleDateForm.removeClass("hidden");
    		//clear out the interval values
    		$("input[name^='startField'], select[name^='startField']").val("");
    		$("input[name^='endField'], select[name^='endField']").val("");

    	} else {
    		//nothing
    	}
    },
    //handle interval or value
    handleType:function() {
    	//If date time interval selected, fire off ajax request to enable validator to be added
    	var selectedTypeValue =  $("input[name='temporalCoverageType']:checked").val();
    	var urlString = temporalCoverage.editAJAXUrl;
    	var editKey = temporalCoverage.editKey;
    	var generator = temporalCoverage.editAJAXGenerator;
    	$.ajax({
    		url: urlString,
    		dataType: 'json',
    		data: {
    			editKey: editKey,
    			temporalCoverageType:selectedTypeValue,
    			generator: generator
    		}, 
    		complete: function(xhr, status) {
    			var results = jQuery.parseJSON(xhr.responseText);
    		},
    		error:function(xhr, status, error) {
    			var results = jQuery.parseJSON(xhr.responseText);
    			alert("an error occurred with modifying the edit configuration:" + results);
    		}
    	});
    	
    }
};

$(document).ready(function() {   
   temporalCoverage.onLoad();
}); 
