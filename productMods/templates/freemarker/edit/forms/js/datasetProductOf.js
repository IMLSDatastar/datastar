var datasetProductOf = {
		 /* *** Initial page setup *** */
		   
	    onLoad: function() {    
	    	this.mixIn();
	        this.initPage();       
	    },
	    mixIn: function() {
	        // Mix in the custom form utility methods
	        $.extend(this, vitro.customFormUtils);

	        // Get the custom form data from the page
	        $.extend(this, customFormData);
	    },
	    // Initial page setup. Called only at page load.
	    initPage: function() {
	    	this.initObjects();
	    	this.initDisplay();
	        this.bindEventListeners();
	    },
	    initObjects:function(){
	    	this.typeSelector = $("#typeSelector");
	    	this.investigationFields = $("#investigationFields");
	    	this.projectFields = $("#projectFields");
	    	this.changeSelection = $("a.changeSelection");
	    }, 
	    initDisplay:function() {
	    	//If  investigation selected, show investigation fields otherwise hide investigation fields
	    	this.toggleFieldsDisplay();
	    },
	    bindEventListeners: function() {
	    	this.typeSelector.change(function() {
	    		datasetProductOf.toggleFieldsDisplay();
	    		
	    		//Check if edit mode, in this case need to handle some autocomplete issues ourselves
	    		if(datasetProductOf.editMode != null && datasetProductOf.editMode == "edit") {
	    			datasetProductOf.undoAcSelection();
	    		}
	    	});
	    	//Get rid of description fields, because the autocomplete code won't do that on its own
	    	this.changeSelection.click(function() {
	    		//Clear out investigation and project fields
	    		datasetProductOf.clearInputs(datasetProductOf.projectFields);
	    		datasetProductOf.clearInputs(datasetProductOf.investigationFields);

	    	});
	    },
	    toggleFieldsDisplay:function() {
	    	var typeSelected = datasetProductOf.typeSelector.val();
	    	if(typeSelected == "http://purl.org/datastar/Investigation") {
	    		datasetProductOf.investigationFields.show();
	    		datasetProductOf.projectFields.hide();
	    		datasetProductOf.clearInputs(datasetProductOf.projectFields);
	    	} else {
	    		datasetProductOf.investigationFields.hide();
	    		datasetProductOf.projectFields.show();
	    		datasetProductOf.clearInputs(datasetProductOf.investigationFields);
	    	}
	    },
	    clearInputs:function($el) {
			// jquery selector :input selects all input text area select and button elements
		    $el.find("input").each( function() {
		       
		            $(this).val("");
		     
	        });
			$el.find("textarea").val("");
			//dont' need select here but could utilize that later
			//$el.find("select option:eq(0)").attr("selected", "selected");
		},
		//specifically for the autocomplete input in this case
		clearAutocompleteInput:function() {
			var inputAutocomplete = $("input.acSelector");
			//Add ac text value
			inputAutocomplete.val("");
			datasetProductOf.addAcHelpText(inputAutocomplete);
		},
		undoAcSelection:function() {
			//Get group name
			var groupName = datasetProductOf.typeSelector.attr("acGroupName");
			var acSelector = $("input.acSelector");
			//Get ac selector with that group name - technically don't need to do this
			var acSelectionDiv = $("div.acSelection[acGroupName='" + groupName + "']");
			var pElement = acSelector.parent("p");
			datasetProductOf.resetAcSelection(acSelectionDiv);
			pElement.show();
			//display input
			 $("input.display[acGroupName='" + groupName + "']").val("");
			//clear autocomplete input 
 			datasetProductOf.clearAutocompleteInput();
		},
		resetAcSelection: function(selectedObj) {
	        this.hideFields($(selectedObj));
	        $(selectedObj).removeClass('userSelected');
	        $(selectedObj).find("input.acUriReceiver").val(this.blankSentinel);
	        $(selectedObj).find("span").text('');
	        $(selectedObj).find("a.verifyMatch").attr('href', this.baseHref);
	    },
	    addAcHelpText: function(selectedObj) {
	        var typeText;
	        var selectedType = datasetProductOf.typeSelector.find(':selected');
	        if(selectedType.val().length) {
	        	typeText = selectedType.html(); 
	        	var helpText = "Select an existing " + typeText + " or create a new one.";
	        	$(selectedObj).val(helpText)
    	               .addClass('acSelectorWithHelpText');    
	    	} 
	    	
	    }
		
};
$(document).ready(function() {   
	   datasetProductOf.onLoad();
	}); 
