var addStudyMethods = {
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
	        //Mix in methods for adding /clearing inputs
	        $.extend(this, addDynamicFieldsUtils);

	        // Get the custom form data from the page
	        $.extend(this, customFormData);
	    },
	    
	    // On page load, create references for easy access to form elements.
	    initObjects: function() {
	        
	        this.form = $('#addStudyMethodsForm');
	        this.showFormButtonWrapper = $('#showAddForm');
	        this.showFormButton = $('#showAddFormButton');
	        this.removeMethodStepLinks = $('a.remove');
	    },
	    
	    // Initial page setup. Called only at page load.
	    initPage: function() {            
	        
	        this.bindEventListeners();
	        //Hide delete link if there is only one method step on the form
	        if(addStudyMethods.methodStepsCounter == 1) {
	        	$("#removeLink").hide();
	        }
	        
	    },
	    
	    
	    /* *** Event listeners *** */
	    
	    bindEventListeners: function() {
	        
	        this.showFormButton.click(function() {
	         //   addAuthorForm.initFormView();
	         //   return false;
	        	addStudyMethods.addAdditionalMethodSteps();
	        	return false;
	        });    
	        
	        this.removeMethodStepLinks.click(function() {
	            addStudyMethods.removeMethodStep(this);
	            return false;
	        });

	        $("form").submit(function (event) { 
	            return addStudyMethods.processSubmission(event);
	          });
	    },
	  
		addAdditionalMethodSteps: function() {
			addStudyMethods.methodStepsCounter++;
			//Show the remove link for the first div as well
			$("#removeLink").show();
			var newId =  "methodFields" + addStudyMethods.methodStepsCounter;
	    	var clonedFields = $("#methodFields").clone();
	    	clonedFields.attr("id", newId);
	    	clonedFields.attr("name", newId);
	    	addStudyMethods.renameIds(clonedFields, addStudyMethods.methodStepsCounter);
	    	addStudyMethods.clearInputs(clonedFields);
	    	//Also need to clear the uri attribute of the remove links and show the remove link in case it was hidden
	    	clonedFields.find("a.remove").attr("uri", "");
	    	//Add number of step to method step title
	    	clonedFields.find("span[name='stepnumber" + addStudyMethods.methodStepsCounter + "']").html(addStudyMethods.methodStepsCounter);
	    	clonedFields.find("label").attr("for", "methodStepValue" + addStudyMethods.methodStepsCounter);
	    
	    	//append
	    	clonedFields.appendTo($("#methodContainer"));
	    	//Now get by retrieving element
	    	var cloned = $("#" + newId);
	    	
	    	//Add a new rank field element
	    	var neworder = addStudyMethods.methodStepsCounter;
	    	var newOrderInput = cloned.find("input[name='methodStepOrder" + addStudyMethods.methodStepsCounter + "']");
	    	newOrderInput.val(neworder);
			
			
			addStudyMethods.modifyConfiguration(addStudyMethods.methodStepsCounter);
			//add event listeners
			addStudyMethods.bindClonedEventListeners(cloned);
		},
	    //bind event listener for cloned object
		bindClonedEventListeners: function($el) {
			var removeMethodStepLinks = $el.find('a.remove');
			removeMethodStepLinks.click(function() {
	            addStudyMethods.removeMethodStep(this);
	            return false;
	        });
		}, 
	    //the data links being added
		modifyConfiguration:function(counter) {
			var urlString = addStudyMethods.editAJAXUrl; //main AJAX url is always the same
			var editKey = addStudyMethods.editKey;
			var generator = addStudyMethods.editAJAXGenerator;
			var action = "add";
			var originalCount = addStudyMethods.originalMethodStepsCount;
	        $.ajax({
	            url: urlString,
	            dataType: 'json',
	            data: {
	                editKey:editKey,
	                counter:counter,
	                generator:generator,
	                action: action,
	                originalCount: originalCount
	            }, 
	            complete: function(xhr, status) {
	                // Not sure why, but we need an explicit json parse here. jQuery
	                // should parse the response text and return a json object.
	                var results = jQuery.parseJSON(xhr.responseText);
	               
	            }

	        });
		},
		
		//Deletion
		removeMethodStep:function(link) {
			 var removeLast = false,
	            message = 'Are you sure you want to remove this method step ?\n\n';
	        if (!confirm(message)) {
	            return false;
	        }
	        
	        var uri = $(link).attr("uri");
	        //if uri isn't empty, then delete individual
	        if(uri != "") {
	        	addStudyMethods.deleteMethodStepIndividual(link, uri);
	        } else {
	        	//in this case, we just need to remove? 
	        	//or also modify configuration
	        	addStudyMethods.removeMethodStepFromDisplay(link);
	        }
	      
	  
		}, 
		//delete individual method step
		deleteMethodStepIndividual:function(link, uri) {
			
	       $.ajax({
	            url: $(link).attr('href'),
	            type: 'POST', 
	            data: {
	                deletion: uri
	            },
	            dataType: 'json',
	            context: link, // context for callback
	            complete: function(request, status) {
	                if (status === 'success') {
	                	addStudyMethods.removeMethodStepFromDisplay(this);
	                	
	                } else {
	                    alert('Error processing request: author not removed');
	                    
	                }
	            }
	        });   
		},
		removeMethodStepFromDisplay:function(link) {
			 var methodStepDiv = $(link).parents("div[name^='methodFields']");
             methodStepDiv.fadeOut(400, function() {
                 // Remove from the DOM                       
                 $(this).remove();
                 //Will need to reorder and rename the rest accordingly
                 addStudyMethods.reorderStudyMethods();
               //update method steps counter and subtract one
     	        addStudyMethods.methodStepsCounter--;
     	        //if now only one method step left on the form, hide remove link
    	        if(addStudyMethods.methodStepsCounter == 1) {
    	        	$("#removeLink").hide();
    	        }
    
             });	
             //addStudyMethods.reorderStudyMethods();
		},
		//Basing this on the process seen in reordering authors - appears jquery's method will return elements in the order they appear in the dom
		//based on this, we can then assign the correct order to the different elements
		reorderStudyMethods:function() {
			var methodFieldDivs = $("div[name^='methodFields']");
			methodFieldDivs.each(function(index) {
				//current position in dom
				var currentPos = index + 1;
				//Get the old position
				var divId = $(this).attr("id");
				var patternString = "methodFields";
				var oldPos = 1;
				if(divId.length > patternString.length) {
					//the very first method fields div doesn't have a number at the end, the rest do
					oldPos = divId.substr(patternString.length);
				}
				if(oldPos != currentPos) {
					//Will have to assign new position to this method field
					if(currentPos > 1) {
						addStudyMethods.replaceIds($(this), oldPos, currentPos);
						$(this).attr("id", patternString + currentPos);
						$(this).attr("name", patternString + currentPos);
					} else {
						addStudyMethods.replaceIdsForFirstElement($(this), oldPos);
						$(this).attr("id", patternString);
						$(this).attr("name", patternString);
					}
					addStudyMethods.updateOrder($(this), currentPos);
				}
			});
		},
		updateOrder:function(fields, currentPos) {
			//The actual value of the order also needs to be updated
			fields.find("input[name^='methodStepOrder']").val(currentPos);
			//this is the number that is displayed
			var stepNumber = fields.find("span[name^='stepnumber']");
			stepNumber.html(currentPos);
			//Change text for label for methodStepValue
			var labelField = fields.find("label");
			if(currentPos > 1) {
				stepNumber.attr("name", "stepnumber" + currentPos);
				labelField.attr("for", "methodStepValue" + currentPos);
			} else {
				stepNumber.attr("name", "stepnumber");
				labelField.attr("for", "methodStepValue");
			}
			
		},
		//submission requires some steps
		processSubmission: function(event) {
			//validate
			var validationError = addStudyMethods.validate();
			if(validationError != "") {
				$("#error-alert").show();
				$("#error-alert p").html(validationError);
				event.preventDefault();
				return false;
			}
			
			//If no validation errors and edit mode, check the original number of method steps against 
			//the current, if current is less, then include blank value sentinels for other method steps
			//this ensures that uri in scope values do not make into the assertions for the n3
			if(addStudyMethods.editMode == "edit" && addStudyMethods.methodStepsCounter < addStudyMethods.originalMethodStepsCount) {
				//var numberToReplace = addStudyMethods.originalMethodStepsCount - addStudyMethods.methodStepsCounter;
				for(i = addStudyMethods.methodStepsCounter + 1; i <= addStudyMethods.originalMethodStepsCount; i++) {
					var blankValueInput = $("<input>").attr({
						type:"hidden",
						id:"methodStep" + i,
						name:"methodStep" + i
					});
					blankValueInput.val(addStudyMethods.blankSentinel);
					blankValueInput.appendTo(addStudyMethods.form);
				}
			}
			return true;
			
		},
		validate: function() {
			//if number of method steps less than 1, then need to remind user to add at least one method step
			var numberMethodSteps =  $("div[name^='methodFields']").length;
			if(numberMethodSteps == 0) {
				return "You must have at least one method step defined.";
			}
			//if any of the method steps do not have text entered, need to reminde ruser to add text
			var methodStepError = "";
			var methodStepValues = $("textarea[name^='methodStepValue']");
			methodStepValues.each(function() {
				var textValue = $(this).val();
				if(textValue == "") {
					methodStepError = "You must enter a value for each method step.";
					return false;
				}
			});
			return methodStepError;
		}
		//also need method to ensure that at least one method step is identifier
		//and/or separate delete button for deleting the entire thing
};

$(document).ready(function() {   
    addStudyMethods.onLoad();

}); 