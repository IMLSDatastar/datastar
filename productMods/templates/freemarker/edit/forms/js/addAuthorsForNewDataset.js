/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var addAuthorForm = {
	//author fields counter will be passed in from custom data
	//This helps in the situation where page needs to be reloaded if there are validation errors
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
        this.showFormButtonWrapper = $('#showAddForm');
        this.showFormButton = $('#showAddFormButton');
        this.removeAuthorshipLinks = $('a.remove');
        //this.undoLinks = $('a.undo');
        this.submit = this.form.find(':submit');
        this.cancel = this.form.find('.cancel'); 
        this.acSelector = this.form.find('.acSelector');
        this.labelField = $('#label');
        this.firstNameField = $('#firstName');
        this.middleNameField = $('#middleName');
        this.lastNameField = $('#lastName');
        this.lastNameLabel = $('label[for=lastName]');
        this.personUriField = $('#personUri');
        this.firstNameWrapper = this.firstNameField.parent();
        this.middleNameWrapper = this.middleNameField.parent();
        this.lastNameWrapper = this.lastNameField.parent();
        this.selectedAuthor = $('#selectedAuthor');
        this.selectedAuthorName = $('#selectedAuthorName');
        this.acHelpTextClass = 'acSelectorWithHelpText';
        this.verifyMatch = this.form.find('.verifyMatch');  
        this.personRadio = $('input.person-radio');  
        this.orgRadio = $('input.org-radio');
        this.personSection = $('section#personFields');  
        this.orgSection = $('section#organizationFields');  
        this.orgName = $('input#orgName');
        this.orgNameWrapper = this.orgName.parent();
        this.orgUriField = $('input#orgUri');
        this.selectedOrg = $('div#selectedOrg');
        this.selectedOrgName = $('span#selectedOrgName');
        this.orgLink = $('a#orgLink');
        this.personLink = $('a#personLink');
        this.returnLink = $('a#returnLink');
        this.selectedAuthorLabel = $("input#selectedAuthorLabel");
        this.orgSection.hide();
    },
    
    // Initial page setup. Called only at page load.
    initPage: function() {

        this.initAuthorshipData();
            
        // Show elements hidden by CSS for the non-JavaScript-enabled version.
        // NB The non-JavaScript version of this form is currently not functional.
        this.removeAuthorshipLinks.show();
        
        //this.undoLinks.hide();
        
        this.bindEventListeners();
        
        this.initAutocomplete();
        
        this.initElementData();

        this.initAuthorDD();
        
        if (this.findValidationErrors()) {
            this.initFormAfterInvalidSubmission();
        } else {
            //this.initAuthorListOnlyView();
        	//Unlike the regular add authors form, we would like to show the first
        	this.initFormView();
            this.showFormButtonWrapper.show(); 

        }
    },
    
    
    /* *** Set up the various page views *** */
   
   // This initialization is done only on page load, not when returning to author list only view 
   // after hitting 'cancel.'
   initAuthorListOnlyView: function() {
       
        if ($('.authorship').length) {  // make sure we have at least one author
            // Reorder authors on page load so that previously unranked authors get a rank. Otherwise,
            // when we add a new author, it will get put ahead of any previously unranked authors, instead
            // of at the end of the list. (It is also helpful to normalize the data before we get started.)            
            this.reorderAuthors();
        }        
        this.showAuthorListOnlyView();       
   },
    
    // This view shows the list of existing authors and hides the form.
    // There is a button to show the form. We do this on page load, and after
    // hitting 'cancel' from full view.
    showAuthorListOnlyView: function() {
        this.hideForm();
        this.showFormButtonWrapper.show();
    },

    // View of form after returning from an invalid submission. On this form,
    // validation errors entail that we were entering a new person, so we show
    // all the fields straightaway.
    
    //this is the javascript for the case where one author was being input at a time
    //here we can have multiple authors - so we're not necessarily interested in 
    //showing the fields for a new person - if an author was selected, we will need to show that author
    
    initFormAfterInvalidSubmission: function() {
    	//if there are values within last name, first, middle name
    	//OR if there is a value for a selected person, then show that, otherwise
    	if(this.lastNameField.val() != "" || this.firstNameField.val() != "" || this.personUriField.val() != "" || this.orgUriField.val() != "" || this.orgName.val() != "") {
    		this.initFormWithExistingValues();
    	} else {
    		this.initForm();
    	}
        //this is redundant b/c already called within initForm
        //this.showFieldsForNewPerson();
        //also check for additional authors if they are there
        this.handleFormViewWithErrors();
    },
    
    initFormWithExistingValues: function() {
    	//if uris selected, then need to show selected autocomplete value
    	if(this.orgUriField.val() != "" || this.personUriField.val() != "") {
    		if(this.orgUriField.val() != "") {
    			//organization selected
    			var authorUri = this.orgUriField.val();
    			var authorLabel = this.selectedAuthorLabelValue;
    			this.showSelectedAuthorGivenValue(authorUri, authorLabel, "org", null, null);
    		} else {
    			//person selected
    			var authorUri = this.personUriField.val();
    			var authorLabel = this.selectedAuthorLabelValue;
    			this.showSelectedAuthorGivenValue(authorUri, authorLabel, "person", null, null);
    		}
    		
    	} else {
    		//See whether org or person fields filled out and select author type on that basis
    		var orgNameValue = this.orgName.val();
    		var personLastNameValue = this.lastNameField.val();
    		if(orgNameValue != "") {
    			//this is the org type
    			this.setAuthorType("org", null, null);
    			//this will take care of hiding autocomplete selected fields as well
    			//as showing the appropriate fields for org/person and selecting the appropriate 
    			//radio button value
    		} else {
    			//this is person type
    			this.setAuthorType("person", null, null);
    		}
    	}
	 this.cancel.unbind('click');
        this.cancel.bind('click', function() {
            addAuthorForm.showAuthorListOnlyView();
            addAuthorForm.setAuthorType("person");
            return false;
        });
        this.form.show(); //not sure why this would be hidden but included in original initForm
    },

    // Initial view of add author form. We get here by clicking the show form button,
    // or by cancelling out of an autocomplete selection.
    initFormView: function() {
        
        this.initForm();
        
        // There's a conflict bewteen the last name fields .blur event and the cancel
        // button's click. So display the middle and first names along with the last name tlw72
        //this.hideFieldsForNewPerson();

        // This shouldn't be needed, because calling this.hideFormFields(this.lastNameWrapper)
        // from showSelectedAuthor should do it. However, it doesn't work from there,
        // or in the cancel action, or if referring to this.lastNameField. None of those work,
        // however.
        $('#lastName').val(''); 
        // Set the initial autocomplete help text in the acSelector field.
        this.addAcHelpText(this.acSelector);
        
        return false; 
        
    },
    
    // Form initialization common to both a 'clean' form view and when
    // returning from an invalid submission.
    initForm: function() {
        
        // Hide the button that shows the form
        this.showFormButtonWrapper.hide(); 

        this.hideSelectedPerson();
        this.hideSelectedOrg();

        this.cancel.unbind('click');
        this.cancel.bind('click', function() {
            addAuthorForm.showAuthorListOnlyView();
            addAuthorForm.setAuthorType("person");
            return false;
        });
        
        // Reset the last name field. It had been hidden if we selected an author from
        // the autocomplete field.
        this.lastNameWrapper.show(); 
        this.showFieldsForNewPerson();        

        // Show the form
        this.form.show();                 
        //this.lastNameField.focus();
    },   
    
    hideSelectedPerson: function(counter) {
    	if(!counter) {
	        this.selectedAuthor.hide();
	        this.selectedAuthorName.html('');
	        this.personUriField.val('');
    	} else {
    		addAuthorForm.getElementByIdInClone(this.selectedAuthor.attr("id"), counter).hide();
    		addAuthorForm.getElementByIdInClone(this.selectedAuthorName.attr("id"), counter).html('');
    		addAuthorForm.getElementByIdInClone(this.personUriField.attr("id"), counter).val('');
    	}
    },

    hideSelectedOrg: function(counter) {
    	if(!counter) {
    		this.selectedOrg.hide();
    		this.selectedOrgName.html('');
    		this.orgUriField.val('');
    	} else {
    		var selectedOrg = addAuthorForm.getElementByIdInClone(this.selectedOrg.attr("id"), counter);
    		var selectedOrgName = addAuthorForm.getElementByIdInClone(this.selectedOrgName.attr("id"), counter);
    		var orgUriField = addAuthorForm.getElementByIdInClone(this.orgUriField.attr("id"), counter);
    		selectedOrg.hide();
    		selectedOrgName.html('');
    		orgUriField.val('');
    	}
    },

    showFieldsForNewPerson: function(counter) {    
    	if(!counter) {
    		this.firstNameWrapper.show();
    		this.middleNameWrapper.show();
    	} else {
    		addAuthorForm.getElementByIdInClone(this.firstNameField.attr("id"), counter).parent().show();
    		addAuthorForm.getElementByIdInClone(this.middleNameField.attr("id"), counter).parent().show();
    	}
    },

    hideFieldsForNewPerson: function(counter) {   
    	if(!counter) {
    		this.hideFields(this.firstNameWrapper); 
    		this.hideFields(this.middleNameWrapper); 
    	} else {
    		var firstNameWrapper = addAuthorForm.getElementByIdInClone(this.firstNameField.attr("id"), counter).parent();
    		var middleNameWrapper = addAuthorForm.getElementByIdInClone(this.middleNameField.attr("id"), counter).parent().show();
    		this.hideFields(firstNameWrapper);
    		this.hideFields(middleNameWrapper);
    	}
    },
        
    /* *** Ajax initializations *** */

    /* Autocomplete */
    initAutocomplete: function($el, counter) {

    	//Extending this to enable autocomplete on the cloned objects when user wishes to add additional authors
    	var personRadio = this.personRadio;
    	var lastNameField = this.lastNameField;
    	var orgName = this.orgName;
    	if(counter) {
    		personRadio = $el.find('input.person-radio');
    		lastNameField = addAuthorForm.getElementByIdInClone(this.lastNameField.attr("id"), counter);
    		orgName = addAuthorForm.getElementByIdInClone(this.orgName.attr("id"), counter);
    	}
    	
    	
        // Make cache a property of this so we can access it after removing 
        // an author.
        this.acCache = {};  
        this.setAcFilter();
        var $acField;
        var urlString;
        var authType;
        
        if  ( personRadio.attr("checked") ) {
            $acField = lastNameField;
            urlString = addAuthorForm.acUrl + addAuthorForm.personUrl + addAuthorForm.tokenize;
            authType = "person";
        }
        else {
            $acField = orgName;
            urlString = addAuthorForm.acUrl + addAuthorForm.orgUrl + addAuthorForm.tokenize;
            authType = "org";
        }  
        $acField.autocomplete({
            minLength: 2,
            source: function(request, response) {
                if (request.term in addAuthorForm.acCache) {
                    // console.log('found term in cache');
                    response(addAuthorForm.acCache[request.term]);
                    return;
                }
                // console.log('not getting term from cache');
                
                // If the url query params are too long, we could do a post
                // here instead of a get. Add the exclude uris to the data
                // rather than to the url.
                $.ajax({
                    url: urlString,
                    dataType: 'json',
                    data: {
                        term: request.term
                    }, 
                    complete: function(xhr, status) {
                        // Not sure why, but we need an explicit json parse here. jQuery
                        // should parse the response text and return a json object.
                        var results = jQuery.parseJSON(xhr.responseText),
                        filteredResults = addAuthorForm.filterAcResults(results);
                        addAuthorForm.acCache[request.term] = filteredResults;  
                        response(filteredResults);
                    }

                });
            },
            // Select event not triggered in IE6/7 when selecting with enter key rather
            // than mouse. Thus form is disabled in these browsers.
            // jQuery UI bug: when scrolling through the ac suggestions with up/down arrow
            // keys, the input element gets filled with the highlighted text, even though no
            // select event has been triggered. To trigger a select, the user must hit enter
            // or click on the selection with the mouse. This appears to confuse some users.
            select: function(event, ui) {
                addAuthorForm.showSelectedAuthor(ui,authType, $el, counter); 
            }
        });

    },

    initElementData: function($el) {   
    	if(!$el) {
    		this.verifyMatch.data('baseHref', this.verifyMatch.attr('href'));
    	} else {
    		var verifyMatch = $el.find('.verifyMatch');  
    		verifyMatch.data('baseHref', verifyMatch.attr('href'));
    	}
    },

    setAcFilter: function() {
        this.acFilter = [];
        
        $('.authorship').each(function() {
            var uri = $(this).data('authorUri');
            addAuthorForm.acFilter.push(uri);
         });
    },
    
    removeAuthorFromAcFilter: function(author) {
        var index = $.inArray(author, this.acFilter);
        if (index > -1) { // this should always be true
            this.acFilter.splice(index, 1);
        }   
    },
    
    filterAcResults: function(results) {
        var filteredResults = [];
        if (!this.acFilter.length) {
            return results;
        }
        $.each(results, function() {
            if ($.inArray(this.uri, addAuthorForm.acFilter) == -1) {
                // console.log("adding " + this.label + " to filtered results");
                filteredResults.push(this);
            }
            else {
                // console.log("filtering out " + this.label);
            }
        });
        return filteredResults;
    },
    
    // After removing an authorship, selectively clear matching autocomplete
    // cache entries, else the associated author will not be included in 
    // subsequent autocomplete suggestions.
    clearAcCacheEntries: function(name) {
        name = name.toLowerCase();
        $.each(this.acCache, function(key, value) {
            if (name.indexOf(key) == 0) {
                delete addAuthorForm.acCache[key];
            }
        });
    },
    
    // Action taken after selecting an author from the autocomplete list
    showSelectedAuthor: function(ui,authType, $el, counter) {
    	this.showSelectedAuthorGivenValue(ui.item.uri, ui.item.label, authType, $el, counter);
    },
        
    /* Drag-and-drop */
    initAuthorDD: function() {
        
        var authorshipList = $('#authorships'),
            authorships = authorshipList.children('li');
        
        if (authorships.length < 2) {
            return;
        }
        
        $('.authorNameWrapper').each(function() {
            $(this).attr('title', 'Drag and drop to reorder authors');
        });
        
        authorshipList.sortable({
            cursor: 'move',
            update: function(event, ui) {
                addAuthorForm.reorderAuthors(event, ui);
            }
        });     
    },
    
    // Reorder authors. Called on page load and after author drag-and-drop and remove.
    // Event and ui parameters are defined only in the case of drag-and-drop.
    reorderAuthors: function(event, ui) {
        var authorships = $('li.authorship').map(function(index, el) {
            return $(this).data('authorshipUri');
        }).get();

        $.ajax({
            url: addAuthorForm.reorderUrl,
            data: {
                predicate: addAuthorForm.rankPredicate,
                individuals: authorships
            },
            traditional: true, // serialize the array of individuals for the server
            dataType: 'json',
            type: 'POST',
            success: function(data, status, request) {
                var pos;
                $('.authorship').each(function(index){
                    pos = index + 1;
                    // Set the new position for this element. The only function of this value 
                    // is so we can reset an element to its original position in case reordering fails.
                    addAuthorForm.setPosition(this, pos);                
                });
                // Set the form rank field value.
                $('#rank').val(pos + 1);        
            },
            error: function(request, status, error) {
                // ui is undefined on page load and after an authorship removal.
                if (ui) {
                    // Put the moved item back to its original position.
                    // Seems we need to do this by hand. Can't see any way to do it with jQuery UI. ??
                    var pos = addAuthorForm.getPosition(ui.item),                       
                        nextpos = pos + 1, 
                        authorships = $('#authorships'), 
                        next = addAuthorForm.findAuthorship('position', nextpos);
                    
                    if (next.length) {
                        ui.item.insertBefore(next);
                    }
                    else {
                        ui.item.appendTo(authorships);
                    }
                    
                    alert('Reordering of authors failed.');                                 
                }      
            }
        });           
    },
    
    // On page load, associate data with each authorship element. Then we don't
    // have to keep retrieving data from or modifying the DOM as we manipulate the
    // authorships.
    initAuthorshipData: function() {
        $('.authorship').each(function(index) {
            $(this).data(authorshipData[index]);    
            
            // RY We might still need position to put back an element after reordering
            // failure. Rank might already have been reset? Check.
            // We also may need position to implement undo links: we want the removed authorship
            // to show up in the list, but it has no rank.
            $(this).data('position', index+1);      
        });
    },

    getPosition: function(authorship) {
        return $(authorship).data('position');
    },
    
    setPosition: function(authorship, pos) {
        $(authorship).data('position', pos);
    },
    
    findAuthorship: function(key, value) {
        var matchingAuthorship = $(); // if we don't find one, return an empty jQuery set
        
        $('.authorship').each(function() {
            var authorship = $(this);
            if ( authorship.data(key) === value ) {
                matchingAuthorship = authorship; 
                return false; // stop the loop
            }
        });
         
        return matchingAuthorship;       
    },
    
               
    /* *** Event listeners *** */ 
   
    bindEventListeners: function() {
        
        this.showFormButton.click(function() {
         //   addAuthorForm.initFormView();
         //   return false;
        	addAuthorForm.addAdditionalAuthors();
        	return false;
        });
        
        this.orgRadio.click(function() {
            addAuthorForm.setAuthorType("org");
        });

        this.personRadio.click(function() {
            addAuthorForm.setAuthorType("person");
        });

        this.form.submit(function() {
            // NB Important JavaScript scope issue: if we call it this way, this = addAuthorForm 
            // in prepareSubmit. If we do this.form.submit(this.prepareSubmit); then
            // this != addAuthorForm in prepareSubmit.
            $selectedObj = addAuthorForm.form.find('input.acSelector');
            addAuthorForm.deleteAcHelpText($selectedObj);
			addAuthorForm.prepareSubmit(); 
        });     

        this.lastNameField.blur(function() {
            // Cases where this event should be ignored:
            // 1. personUri field has a value: the autocomplete select event has already fired.
            // 2. The last name field is empty (especially since the field has focus when the form is displayed).
            // 3. Autocomplete suggestions are showing.
            if ( addAuthorForm.personUriField.val() || !$(this).val() || $('ul.ui-autocomplete li.ui-menu-item').length ) {
                return;
            }
            addAuthorForm.onLastNameChange();
        });

        this.personLink.click(function() {
            window.open($(this).attr('href'), 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
            return false;
        });   

        this.orgLink.click(function() {
            window.open($(this).attr('href'), 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
            return false;
        });   

    	this.acSelector.focus(function() {
        	addAuthorForm.deleteAcHelpText(this);
    	});   

    	this.acSelector.blur(function() {
        	addAuthorForm.addAcHelpText(this);
    	}); 
                
    	this.orgName.focus(function() {
        	addAuthorForm.deleteAcHelpText(this);
    	});   

    	this.orgName.blur(function() {
        	addAuthorForm.addAcHelpText(this);
    	}); 
                
        // When hitting enter in last name field, show first and middle name fields.
        // NB This event fires when selecting an autocomplete suggestion with the enter
        // key. Since it fires first, we undo its effects in the ac select event listener.
        this.lastNameField.keydown(function(event) {
            if (event.which === 13) {
                addAuthorForm.onLastNameChange();
                return false; // don't submit form
            }
        });
        
        //TODy
        this.removeAuthorshipLinks.click(function() {
            addAuthorForm.removeAuthorship(this);
            return false;
        });
        
        
        
//      this.undoLinks.click(function() {
//          $.ajax({
//              url: $(this).attr('href')
//          });
//          return false;           
//      });
        
    },

    prepareSubmit: function() {
        var firstName,
            middleName,
            lastName,
            name;
        
        
	        // If selecting an existing person, don't submit name fields
	        if (this.personUriField.val() != '' || this.orgUriField.val() != '' || this.orgName.val() != '' ) {
	            this.firstNameField.attr('disabled', 'disabled');
	            this.middleNameField.attr('disabled', 'disabled');
	            this.lastNameField.attr('disabled', 'disabled');
	        } 
	        else {
	            firstName = this.firstNameField.val();
	            middleName = this.middleNameField.val();
	            lastName = this.lastNameField.val();
	            
	            name = lastName;
	            if (firstName) {
	                name += ', ' + firstName;
	            }
	            if (middleName) {
	                name += ' ' + middleName;
	            }
	            
	            this.labelField.val(name);
	        }
	   //Also do the same for the other people if they are added     
	   if(this.authorFieldsCounter > 1) {
		   for(var counter = 2; counter <= this.authorFieldsCounter; counter++) {
        	// If selecting an existing person, don't submit name fields
				var personUriField = addAuthorForm.getElementByIdInClone(this.personUriField.attr("id"), counter);
				var orgUriField = addAuthorForm.getElementByIdInClone(this.orgUriField.attr("id"), counter);
				var orgName = addAuthorForm.getElementByIdInClone(this.orgName.attr("id"), counter);
				
				var firstNameField = addAuthorForm.getElementByIdInClone(this.firstNameField.attr("id"), counter);
	            var middleNameField = addAuthorForm.getElementByIdInClone(this.middleNameField.attr("id"), counter);
	        	var lastNameField = addAuthorForm.getElementByIdInClone(this.lastNameField.attr("id"), counter);
	        	var labelField = addAuthorForm.getElementByIdInClone(this.labelField.attr("id"), counter);
		        if (personUriField.val() != '' || orgUriField.val() != '' || orgName.val() != '' ) {
		        	
		            firstNameField.attr('disabled', 'disabled');
		            middleNameField.attr('disabled', 'disabled');
		            lastNameField.attr('disabled', 'disabled');
		        } 
		        else {
		            firstName = firstNameField.val();
		            middleName = middleNameField.val();
		            lastName = lastNameField.val();
		            
		            name = lastName;
		            if (firstName) {
		                name += ', ' + firstName;
		            }
		            if (middleName) {
		                name += ' ' + middleName;
		            }
		            
		            labelField.val(name);
		        }
		   }
	   }
    },    
    
    onLastNameChange: function($el, counter) {
    	if(!counter) {
    		this.showFieldsForNewPerson();
    		this.firstNameField.focus();
    	} else {
    		addAuthorForm.showFieldsForNewPerson(counter);
    		addAuthorForm.getElementByIdInClone("#firstNameField", counter).focus();
    	}
        // this.fixNames();
    },
    
    // User may have typed first name as well as last name into last name field.
    // If so, when showing first and middle name fields, move anything after a comma
    // or space into the first name field.
    // RY Space is problematic because they may be entering "<firstname> <lastname>", but
    // comma is a clear case. 
//    fixNames: function() {
//        var lastNameInput = this.lastNameField.val(),
//            names = lastNameInput.split(/[, ]+/), 
//            lastName = names[0];
// 
//        this.lastNameField.val(lastName);
//        
//        if (names.length > 1) {
//            //firstName = names[1].replace(/^[, ]+/, '');
//            this.firstNameField.val(names[1]);
//        } 
//    },
     
    removeAuthorship: function(link) {
        // RY Upgrade this to a modal window

        authorName = $(link).prev().children().text();

        var removeLast = false,
            message = 'Are you sure you want to remove this author:\n\n' + authorName + ' ?\n\n';
        if (!confirm(message)) {
            return false;
        }

        if ( addAuthorForm.showFormButtonWrapper.is(':visible') ) {
            addAuthorForm.returnLink.hide();
            $('img#indicatorOne').removeClass('hidden');
            addAuthorForm.showFormButton.addClass('disabledSubmit');
            addAuthorForm.showFormButton.attr('disabled','disabled');
        }
        else {
            addAuthorForm.cancel.hide();
            $('img#indicatorTwo').removeClass('hidden');            
            addAuthorForm.submit.addClass('disabledSubmit');
            addAuthorForm.submit.attr('disabled','disabled');
        }
              
        if ($(link)[0] === $('.remove:last')[0]) {
            removeLast = true;
        } 
        
        $.ajax({
            url: $(link).attr('href'),
            type: 'POST', 
            data: {
                deletion: $(link).parents('.authorship').data('authorshipUri')
            },
            dataType: 'json',
            context: link, // context for callback
            complete: function(request, status) {
                var authorship,
                    authorUri;
            
                if (status === 'success') {
                    
                    authorship = $(this).parents('.authorship');
                
                    // Clear autocomplete cache entries matching this author's name, else
                    // autocomplete will be retrieved from the cache, which excludes the removed author.
                    addAuthorForm.clearAcCacheEntries(authorship.data('authorName'));
                    
                    // Remove this author from the acFilter so it is included in autocomplete
                    // results again.
                    addAuthorForm.removeAuthorFromAcFilter(authorship.data('authorUri'));
                    
                    authorship.fadeOut(400, function() {
                        var numAuthors;
 
                        // For undo link: add to a deletedAuthorships array
                        
                        // Remove from the DOM                       
                        $(this).remove();
                        
                        // Actions that depend on the author having been removed from the DOM:
                        numAuthors = $('.authorship').length; // retrieve the length after removing authorship from the DOM
                        
                        // If removed item not last, reorder to remove any gaps
                        if (numAuthors > 0 && ! removeLast) {
                            addAuthorForm.reorderAuthors();
                        }
                            
                        // If fewer than two authors remaining, disable drag-drop
                        if (numAuthors < 2) {
                            addAuthorForm.disableAuthorDD();
                        }                           

                        if ( $('img#indicatorOne').is(':visible') ) {
                            $('img#indicatorOne').fadeOut(100, function() {
                                $(this).addClass('hidden');
                            });

                            addAuthorForm.returnLink.fadeIn(100, function() {
                                $(this).show();
                            });
                            addAuthorForm.showFormButton.removeClass('disabledSubmit');
                            addAuthorForm.showFormButton.attr('disabled','');
                        }
                        else {
                            $('img#indicatorTwo').fadeOut(100, function() {
                                 $(this).addClass('hidden');
                             });

                             addAuthorForm.cancel.fadeIn(100, function() {
                                 $(this).show();
                             });
                             addAuthorForm.submit.removeClass('disabledSubmit');
                             addAuthorForm.submit.attr('disabled','');
                        }
                    });

                } else {
                    alert('Error processing request: author not removed');
                    
                }
            }
        });        
    },
    
    // Disable DD and associated cues if only one author remains
    disableAuthorDD: function() {
        var authorships = $('#authorships'),
            authorNameWrapper = $('.authorNameWrapper');
            
        authorships.sortable({ disable: true } );
        
        // Use class dd rather than jQuery UI's class ui-sortable, so that we can remove
        // the class if there's fewer than one author. We don't want to remove the ui-sortable
        // class, in case we want to re-enable DD without a page reload (e.g., if implementing
        // adding an author via Ajax request). 
        authorships.removeClass('dd');
              
        authorNameWrapper.removeAttr('title');
    },

    // RY To be implemented later.
    toggleRemoveLink: function() {
        // when clicking remove: remove the author, and change link text to 'undo'
        // when clicking undo: add the author back, and change link text to 'remove'
    },

	// Set the initial help text in the lastName field and change the class name.
    //updated to enable working with multiple elements returned
	addAcHelpText: function(selectedObj) {
		if(!selectedObj.each) {
			
			var typeText;
	        if ( $(selectedObj).attr('id') == "lastName" ) {
	            typeText = "Author";
	        }
	        else {
	            typeText = "Organization";
	        }
	        
	        if (!$(selectedObj).val()) {
				$(selectedObj).val("Select an existing " + typeText + " or add a new one.")
							   .addClass(addAuthorForm.acHelpTextClass);
			}
			
		} else {
			selectedObj.each(function() {
				var typeText;
		        if ( $(this).attr('id') == "lastName" || $(this).attr("id").indexOf("lastName") == 0 ) {
		            typeText = "Author";
		        }
		        else {
		            typeText = "Organization";
		        }
		        
		        if (!$(this).val()) {
					$(this).val("Select an existing " + typeText + " or add a new one.")
								   .addClass(addAuthorForm.acHelpTextClass);
				}
			});
		}
        
	},
	//updated to support multiple elements being sent to function
	//selectedObj is sometimes result of find but other times appears to be a direct reference to the object
	//there didn't seem to be a type-based way to distinguish (both appear to be objects)
	//so instead checking if the method each is recognized
	deleteAcHelpText: function(selectedObj) {
		if(!selectedObj.each) {
			if ($(selectedObj).hasClass(addAuthorForm.acHelpTextClass)) {
	            $(selectedObj).val('')
	                          .removeClass(addAuthorForm.acHelpTextClass);
			}
		} else {
			selectedObj.each(function() {
				 if ($(this).hasClass(addAuthorForm.acHelpTextClass)) {
			            $(this).val('')
			                          .removeClass(addAuthorForm.acHelpTextClass);
			    }
			});
		};
	},

    // Depending on whether the author is a person or an organization,
    // we need to set the correct class names for fields like the acSelector, acSelection, etc.
    // as well as clear and disable fields, call other functions ...
	setAuthorType: function(authType, $el, counter) {
		if(!$el && !counter) {
		    if ( authType == "org" ) {
		        this.personSection.hide();
		        //Also, if autocomplete had already been done AND then the user selected a different type
		        //then need to show the wrapper for the elements as those will have been hidden
		        this.orgNameWrapper.show();
		        this.orgSection.show();
		        // person fields
	            this.personRadio.attr('checked', false);  // needed for reset when cancel button is clicked
		        this.acSelector.removeClass("acSelector");
		        this.acSelector.removeClass(this.acHelpTextClass);
		        this.selectedAuthor.removeClass("acSelection");
		        this.selectedAuthorName.removeClass("acSelectionInfo");
		        this.personLink.removeClass("verifyMatch");
		        this.acSelector.attr('disabled', 'disabled');
		        this.firstNameField.attr('disabled', 'disabled');
		        this.middleNameField.attr('disabled', 'disabled');
		        this.lastNameField.attr('disabled', 'disabled');
		        this.acSelector.val('');
		        this.firstNameField.val('');
		        this.middleNameField.val('');
		        this.lastNameField.val('');
		        // org fields
		        this.orgRadio.attr('checked', true); // needed for reset when cancel button is clicked
		        this.orgName.addClass("acSelector");
		        this.selectedOrg.addClass("acSelection");
		        this.selectedOrgName.addClass("acSelectionInfo");
		        this.orgLink.addClass("verifyMatch");
		        this.orgName.attr('disabled', '');
		        this.orgUriField.attr('disabled', '');
	
		        addAuthorForm.addAcHelpText(this.orgName);
		        addAuthorForm.initAutocomplete();
		        addAuthorForm.hideSelectedPerson();
		    }
		    else if ( authType == "person" ) {
		        this.orgSection.hide();
		        this.firstNameWrapper.show();
		        this.middleNameWrapper.show();
		        this.lastNameWrapper.show();
		        this.personSection.show();

		        // org fields
		        this.orgRadio.attr('checked', false);  // needed for reset when cancel button is clicked
		        this.orgName.removeClass("acSelector");
		        this.orgName.removeClass(this.acHelpTextClass);
		        this.selectedOrg.removeClass("acSelection");
		        this.selectedOrgName.removeClass("acSelectionInfo");
		        this.orgLink.removeClass("verifyMatch");
		        this.orgName.attr('disabled', 'disabled');
		        this.orgUriField.attr('disabled', 'disabled');
		        this.orgName.val('');
		        this.orgUriField.val('');
	            // person fields
	            this.acSelector.addClass("acSelector");
	            this.personRadio.attr('checked', true);  // needed for reset when cancel button is clicked
		        this.selectedAuthor.addClass("acSelection");
		        this.selectedAuthorName.addClass("acSelectionInfo");
		        this.personLink.addClass("verifyMatch");
		        this.acSelector.attr('disabled', '');
		        this.firstNameField.attr('disabled', '');
		        this.middleNameField.attr('disabled', '');
		        this.lastNameField.attr('disabled', '');
	
		        addAuthorForm.addAcHelpText(this.acSelector);
		        addAuthorForm.initAutocomplete();
		        addAuthorForm.hideSelectedOrg();
		        
		    }
		} else {
		  var personSection = $('section#personFields' + counter);  
	      var orgSection = $('section#organizationFields' + counter);  
	      var personRadio = $el.find('input.person-radio');  
	      var selectedAuthor = $('#selectedAuthor' + counter);
	      var selectedAuthorName = $('#selectedAuthorName' + counter);
	      var personLink = $('a#personLink' + counter);
	      var firstNameField = $('#firstName' + counter);
	      var middleNameField = $('#middleName' + counter);
	      var lastNameField = $('#lastName' + counter);
	      var orgRadio = $el.find('input.org-radio');
	      var orgName = $('input#orgName' + counter);
	      var selectedOrg = $('div#selectedOrg' + counter);
	      var selectedOrgName = $('span#selectedOrgName' + counter);
	      var orgLink = $('a#orgLink' + counter);
	      var orgUriField = $('input#orgUri' + counter);
	      //	      var acSelector = $el.find('.acSelector');
	      
	      //parallel to the original code, althought that has redundant portions
	      //we are setting acSelector always be the lastNameField
		 	acSelector = lastNameField;


		 if ( authType == "org" ) {
			 //setting ac selector to actually be the field
		       	personSection.hide();
		       	orgName.parent().show();
		        orgSection.show();
		        // person fields
	            personRadio.attr('checked', false);  // needed for reset when cancel button is clicked
		        acSelector.removeClass("acSelector");
		        acSelector.removeClass(this.acHelpTextClass);
		        selectedAuthor.removeClass("acSelection");
		        selectedAuthorName.removeClass("acSelectionInfo");
		        personLink.removeClass("verifyMatch");
		        acSelector.attr('disabled', 'disabled');
		        firstNameField.attr('disabled', 'disabled');
		        middleNameField.attr('disabled', 'disabled');
		        lastNameField.attr('disabled', 'disabled');
		        acSelector.val('');
		        firstNameField.val('');
		        middleNameField.val('');
		        lastNameField.val('');
		        // org fields
		        orgRadio.attr('checked', true); // needed for reset when cancel button is clicked
		        orgName.addClass("acSelector");
		        selectedOrg.addClass("acSelection");
		        selectedOrgName.addClass("acSelectionInfo");
		        orgLink.addClass("verifyMatch");
		        orgName.attr('disabled', '');
		        orgUriField.attr('disabled', '');

		        addAuthorForm.addAcHelpText(orgName);
		        addAuthorForm.initAutocomplete($el, counter);
		        addAuthorForm.hideSelectedPerson(counter);
		    }
		    else if ( authType == "person" ) {
		        orgSection.hide();
		        firstNameField.parent().show();
		       	middleNameField.parent().show();
		       	lastNameField.parent().show();
		        personSection.show();
		        // org fields
		        orgRadio.attr('checked', false);  // needed for reset when cancel button is clicked
		        orgName.removeClass("acSelector");
		        orgName.removeClass(this.acHelpTextClass);
		        selectedOrg.removeClass("acSelection");
		        selectedOrgName.removeClass("acSelectionInfo");
		        orgLink.removeClass("verifyMatch");
		        orgName.attr('disabled', 'disabled');
		        orgUriField.attr('disabled', 'disabled');
		        orgName.val('');
		        orgUriField.val('');
	            // person fields
	            acSelector.addClass("acSelector");
	            personRadio.attr('checked', true);  // needed for reset when cancel button is clicked
		        selectedAuthor.addClass("acSelection");
		        selectedAuthorName.addClass("acSelectionInfo");
		        personLink.addClass("verifyMatch");
		        acSelector.attr('disabled', '');
		        firstNameField.attr('disabled', '');
		        middleNameField.attr('disabled', '');
		        lastNameField.attr('disabled', '');

		        addAuthorForm.addAcHelpText(acSelector);
		        addAuthorForm.initAutocomplete($el, counter);
		        addAuthorForm.hideSelectedOrg(counter);
		        
		    }	
		}
    },
    /***Methods for AJAX call to modify edit configuration***/
    //first, clicking on the 'add author' button should create
    //additional section with the author info
    //except all the fields should be cleared
    //AND we should make sure that person is set (as that is the default for the original selection
    addAdditionalAuthors:function() {
    	addAuthorForm.authorFieldsCounter++;
    	var newId =  "mainAuthorFields" + addAuthorForm.authorFieldsCounter;
    	var clonedFields = $("#mainAuthorFields").clone();
    	clonedFields.attr("id", newId);
    	addAuthorForm.renameIds(clonedFields, addAuthorForm.authorFieldsCounter);
    	addAuthorForm.clearInputs(clonedFields);
    	//bind event listeners
    	addAuthorForm.bindEventListenersForClone(clonedFields, addAuthorForm.authorFieldsCounter);
    	//append
    	clonedFields.appendTo($("#authorsContainer"));
    	//Now get by retrieving element
    	var cloned = $("#" + newId);
    	//if the author type is not a person for the original form that is copied
    	//then set author type to person - or if the original person shows a selected author
    	//that method also sets ac selector text and calls init autocomplete
    	if(!this.personRadio.is(":checked"))  {
    		addAuthorForm.setAuthorType("person", cloned, addAuthorForm.authorFieldsCounter);
    	} else if(this.personRadio.is(":checked") && !this.selectedAuthor.is(":hidden")) {
    		//get change link and undo autocomplete for cloned object
    		var changeLink = this.getElementByIdInClone("changePersonSelection", addAuthorForm.authorFieldsCounter);
    		addAuthorForm.undoAutocompleteSelection(changeLink, "person", addAuthorForm.authorFieldsCounter);
    		addAuthorForm.initAutocomplete(cloned, addAuthorForm.authorFieldsCounter);
    	} 
    	else {
    		addAuthorForm.initAutocomplete(cloned, addAuthorForm.authorFieldsCounter);
    		addAuthorForm.addAcHelpText(cloned.find(".acSelector"));
    	}
    	//init data for clone
    	addAuthorForm.initElementData(cloned);
    	//Add a new rank field element
    	var newrank = parseInt(addAuthorForm.newRank) + parseInt(addAuthorForm.authorFieldsCounter - 1);
    	var newRankInput = cloned.find("input[name='rank" + addAuthorForm.authorFieldsCounter + "']");
    	newRankInput.val(newrank);
       	//submit ajax request to modify edit configuration 
    	addAuthorForm.modifyConfiguration(addAuthorForm.authorFieldsCounter);
    	
    },
    //rename ids/names in cloned fields
    renameIds:function(clonedFields, counter) {
    	clonedFields.find("[id]").each(function(index, el) { 
    		var originalId = $(this).attr("id");
    		var newId = originalId + counter;
    		$(this).attr("id", newId);
    	});
    	//renaming names as well because we want to submit this directly
    	//to edit configuration
    	clonedFields.find("[name]").each(function(index, el) { 
    		var originalId = $(this).attr("name");
    		var newId = originalId + counter;
    		$(this).attr("name", newId);
    	});
    },
    clearInputs:function($el) {
		// jquery selector :input selects all input text area select and button elements
	    $el.find("input").each( function() {
	       
	            $(this).val("");
	     
        });
		$el.find("textarea").val("");
		//resetting class group section as well so selection is reset if type changes
		$el.find("select option:eq(0)").attr("selected", "selected");
	},
	bindEventListenersForClone:function($el, counter) {
        var removeAuthorshipLinks = $el.find('a.remove');
         var acSelector = $el.find('.acSelector');

		
		var showFormButton = $();
		var orgRadio = $el.find('input.org-radio[name="authorType' + counter + '"]');
		var personRadio = $el.find('input.person-radio[name="authorType' + counter + '"]');
		var lastNameField = $el.find('#lastName' + counter);
		var personLink = $el.find('a#personLink' + counter);
		var orgLink = $el.find('a#orgLink' + counter);
		//will have to change all these to select the right one
		var acSelector = $el.find('.acSelector');
		var orgName = $el.find('input#orgName' + counter);
		var personUriField = $el.find('#personUri' + counter);

		
        
        orgRadio.click(function() {
            addAuthorForm.setAuthorType("org", $el, counter);
        });

        personRadio.click(function() {
            addAuthorForm.setAuthorType("person", $el, counter);
        });

        
        lastNameField.blur(function() {
            // Cases where this event should be ignored:
            // 1. personUri field has a value: the autocomplete select event has already fired.
            // 2. The last name field is empty (especially since the field has focus when the form is displayed).
            // 3. Autocomplete suggestions are showing.
            if (personUriField.val() || !$(this).val() || $el.find('ul.ui-autocomplete li.ui-menu-item').length ) {
                return;
            }
            addAuthorForm.onLastNameChange($el, counter);
        });

        personLink.click(function() {
            window.open($(this).attr('href'), 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
            return false;
        });   

        orgLink.click(function() {
            window.open($(this).attr('href'), 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
            return false;
        });   

    	acSelector.focus(function() {
        	addAuthorForm.deleteAcHelpText(this);
    	});   

    	acSelector.blur(function() {
        	addAuthorForm.addAcHelpText(this);
    	}); 
                
    	orgName.focus(function() {
        	addAuthorForm.deleteAcHelpText(this);
    	});   

    	orgName.blur(function() {
        	addAuthorForm.addAcHelpText(this);
    	}); 
                
        // When hitting enter in last name field, show first and middle name fields.
        // NB This event fires when selecting an autocomplete suggestion with the enter
        // key. Since it fires first, we undo its effects in the ac select event listener.
        lastNameField.keydown(function(event) {
            if (event.which === 13) {
                addAuthorForm.onLastNameChange($el, counter);
                return false; // don't submit form
            }
        });
        
        //this should be changed to update configuration instead of
        //sending request to actually edit the objects
        /*
        removeAuthorshipLinks.click(function() {
            addAuthorForm.removeAuthorship(this);
            return false;
        });*/
		        
	},
	
	getElementByIdInClone:function(elementId, counter) {
		return $("#" + elementId + counter);
	},
	undoAutocompleteSelection: function($changeLink, authType, counter) {
		//Get the div that is the closest ancestor the chagne link
		var $acDiv = $changeLink.closest("div.acSelection");
		
		 if ( authType == "person" ) {
			 	//the section container
			 	var sectionContainer = $acDiv.closest("section[role='personContainer']");
	    	 	var personUriField = $acDiv.find("input[name^='personUri']");
	    	 	//div is the selected author div
	    	 	var selectedAuthor = $acDiv;
	            var selectedAuthorName = $acDiv.find(".acSelectionInfo");
	            var lastNameField = sectionContainer.find("input[name^='lastName']");
	    	 	var lastNameWrapper = lastNameField.parent();
	    	 	var personLink = $acDiv.find("a[id^='personLink']");
	    	 	
	    	 	//Clear out the fields/url within the selected author div and hide selected author
	    	 	//show fields for new person and last name again
	    	 	personUriField.val("");
	            selectedAuthor.hide();
	            selectedAuthorName.html("");	 
	            //clear fields for the first, middle, and last names
	            sectionContainer.find("input[name^='firstName']").val("");
	            sectionContainer.find("input[name^='middleName']").val("");
	            lastNameField.val("");
	            //set help text for last name
	            addAuthorForm.addAcHelpText(lastNameField);
	            //show the fields for first, middle and last names
	            lastNameWrapper.show();
	            addAuthorForm.showFieldsForNewPerson(counter); 
	            //resetting to what was originally within the freemarker form
	            personLink.attr('href', addAuthorForm.baseUrl + '/individual?uri');
	        }
	        else {
	        	var sectionContainer = $acDiv.closest("section[role='organization']");
	    	 	var orgUriField = $acDiv.find("input[name^='orgUri']");
	    	 	//div is the selected author div
	    	 	var selectedOrg = $acDiv;
	            var selectedOrgName = $acDiv.find("span[id^='selectedOrgName']");
	    	 	var orgLink = $acDiv.find("a[name^='orgLink']");
	    	 	var orgName = sectionContainer.find("input[name^='orgName']");
	    	 	var orgNameWrapper = orgName.parent();
	        	
	        	// clear out fields/inputs/urls within the selected org div and hide it
	            orgUriField.val(""); 
	            selectedOrg.hide;
	            selectedOrgName.html("");
	            //set help text for for org name
	            addAuthorForm.addAcHelpText(orgName);
	            orgNameWrapper.show(); 

	            orgLink.attr('href', addAuthorForm.baseUr + '/individual?uri');
	        }

	},
	//Method that will send request to modify the edit configuration in accordance with
	//the authors being added
	modifyConfiguration:function(counter) {
		
		var urlString = addAuthorForm.editAJAXUrl;
		var editKey = addAuthorForm.editKey;
		var generator = addAuthorForm.editAJAXGenerator;
        $.ajax({
            url: urlString,
            dataType: 'json',
            data: {
                editKey:editKey,
                counter:counter,
                generator:generator
            }, 
            complete: function(xhr, status) {
                // Not sure why, but we need an explicit json parse here. jQuery
                // should parse the response text and return a json object.
                var results = jQuery.parseJSON(xhr.responseText);
               
            }

        });
	},
	/***Methods for handling error conditions where multiple authors have been added and page must be reloaded with error message****/
	handleFormViewWithErrors:function() {
		if(addAuthorForm.authorFieldsCounter > 1) {
			//more than one author, so need to get data for additional authors and display the authors
			addAuthorForm.displayAndPopulateAdditionalAuthors();
		}
	},
	displayAndPopulateAdditionalAuthors:function() {
		
		var i = 2;
		for(i = 2; i <=addAuthorForm.authorFieldsCounter; i++) {
			addAuthorForm.displayAuthorInFormWithErrors(i);
			addAuthorForm.populateAuthorInFormWithErrors(i);
		}
	},
	displayAuthorInFormWithErrors:function(counter) {
		var newId =  "mainAuthorFields" + counter;
    	var clonedFields = $("#mainAuthorFields").clone();
    	clonedFields.attr("id", newId);
    	addAuthorForm.renameIds(clonedFields, counter);
    	addAuthorForm.clearInputs(clonedFields);
    	//bind event listeners
    	addAuthorForm.bindEventListenersForClone(clonedFields, counter);
    	//append
    	clonedFields.appendTo($("#authorsContainer"));
    	//Now get by retrieving element
    	var cloned = $("#" + newId);
    	//if the author type is not a person for the original form that is copied
    	//then set author type to person - or if the original person shows a selected author
    	//that method also sets ac selector text and calls init autocomplete
    	if(!this.personRadio.is(":checked"))  {
    		addAuthorForm.setAuthorType("person", cloned, counter);
    	} else if(this.personRadio.is(":checked") && !this.selectedAuthor.is(":hidden")) {
    		//get change link and undo autocomplete for cloned object
    		var changeLink = this.getElementByIdInClone("changePersonSelection", counter);
    		addAuthorForm.undoAutocompleteSelection(changeLink, "person", counter);
    		addAuthorForm.initAutocomplete(cloned, counter);
    	} 
    	else {
    		addAuthorForm.initAutocomplete(cloned, counter);
    		addAuthorForm.addAcHelpText(cloned.find(".acSelector"));
    	}
    	//init data for clone
    	addAuthorForm.initElementData(cloned);
    	//Add a new rank field element
    	var newRankInput = cloned.find("input[name='rank" + counter + "']");
    	//Assuming rank here is same as counter but we could populate this value in populate method
    	//newRankInput.val(newrank);
	},
	populateAuthorInFormWithErrors:function(counter) {
		//populate this with the information that has been returned
		var newId =  "mainAuthorFields" + counter;
		var cloned = $("#" + newId);
		//fields = lastname firstname middlename orgname personuri orguri rank
		var authorsHash = addAuthorForm.additionalAuthorsHash;
		var authorKey = "author" + counter;
		if(authorKey in authorsHash ) {
			var authorFieldMap = authorsHash[authorKey];
			for(var authorFieldName in authorFieldMap) {
				var authorFieldValue = authorFieldMap[authorFieldName];
				//if there is a value, then find that input with that name within the form and set the value
				if(authorFieldValue != "") {
					cloned.find("input[name='" + authorFieldName + "']").val(authorFieldValue);
				}
			}
			
			
			//Check whether orgUri or personUri have values, if so then autocomplete already selected
			//Will also decide whether person or organization selected
			//If not selected, then check if orgname selected - then org otherwise person
			if(authorFieldMap["orgUri" + counter] != "" || authorFieldMap["orgName" + counter] != "") {
				//selected type is organization
				addAuthorForm.setAuthorType("org", cloned, counter);
				if(authorFieldMap["orgUri" + counter] != "") {
					var orgUriValue = authorFieldMap["orgUri" + counter];
					var selectedOrgLabelValue = authorFieldMap["selectedAuthorLabel" + counter];
					//show autocomplete selection
					addAuthorForm.showSelectedAuthorGivenValue(orgUriValue, selectedOrgLabelValue, "org", cloned, counter);
				}
			} else {
				//the fields are set to the default person type in cloning already
				//so the only thing to check here if autocomplete done
				if(authorFieldMap["personUri" + counter] != "") {
					//show autocomplete selection
					//ui is something that happens when you select the person
					//we're going to have to set the label manually and store the label as a field as well
					//or as form specific data
					var personUriValue = authorFieldMap["personUri" + counter];
					var selectedPersonLabelValue = authorFieldMap["selectedAuthorLabel" + counter];

					addAuthorForm.showSelectedAuthorGivenValue(personUriValue, selectedPersonLabelValue, "person", cloned, counter);
				}
				
			}
			
		}			
	},
	//factoring out code that will help this method be called in general as well as validation error context
	showSelectedAuthorGivenValue: function(authorUri, authorLabel,authType, $el, counter) {
			var changeLinkId = "";

	    	if(!$el && !counter) {
	    	 	//This is used to keep track of the selected label from autocomplete in case the form has to reload b/c of validation error
	    		this.selectedAuthorLabel.val(authorLabel);
		        if ( authType == "person" ) {
		        	changeLinkId = "changePersonSelection";
		            this.personUriField.val(authorUri);
		            this.selectedAuthor.show();
		
		            // Transfer the name from the autocomplete to the selected author
		            // name display, and hide the last name field.
		            this.selectedAuthorName.html(authorLabel);
		            // NB For some reason this doesn't delete the value from the last name
		            // field when the form is redisplayed. Thus it's done explicitly in initFormView.
		            this.hideFields(this.lastNameWrapper);
		            // These get displayed if the selection was made through an enter keystroke,
		            // since the keydown event on the last name field is also triggered (and
		            // executes first). So re-hide them here.
		            this.hideFieldsForNewPerson(); 
		            this.personLink.attr('href', this.verifyMatch.data('baseHref') + authorUri);
		        }
		        else {
		            // do the same as above but for the organization fields
		        	changeLinkId = "changeOrgSelection";
		            this.orgUriField.val(authorUri); 
		            this.selectedOrg.show();
		
		            this.selectedOrgName.html(authorLabel);
		
		            this.hideFields(this.orgNameWrapper); 
		
		            this.orgLink.attr('href', this.verifyMatch.data('baseHref') + authorUri);
		            
		        }
	        
	    	} else {
	    	 	var verifyMatch = $el.find('.verifyMatch');  
	    	 	//This is used to keep track of the selected label from autocomplete in case the form has to reload b/c of validation error
	    	 	var selectedAuthorLabelField = addAuthorForm.getElementByIdInClone("selectedAuthorLabel", counter);
	    	    selectedAuthorLabelField.val(authorLabel);
	    	 	if ( authType == "person" ) {
	    	    	 	var personUriField = addAuthorForm.getElementByIdInClone(this.personUriField.attr("id"), counter);
	    	    	 	var selectedAuthor = addAuthorForm.getElementByIdInClone(this.selectedAuthor.attr("id"), counter);
	    	            var selectedAuthorName = addAuthorForm.getElementByIdInClone(this.selectedAuthorName.attr("id"), counter);
	    	    	 	var lastNameWrapper = addAuthorForm.getElementByIdInClone(this.lastNameField.attr("id"), counter).parent();
	    	    	 	var personLink = addAuthorForm.getElementByIdInClone(this.personLink.attr("id"), counter);
	    	    	 	changeLinkId = "changePersonSelection" + counter;

	    	    	 	personUriField.val(authorUri);
	    	            selectedAuthor.show();

	    	            // Transfer the name from the autocomplete to the selected author
	    	            // name display, and hide the last name field.
	    	            selectedAuthorName.html(authorLabel);
	    	            // NB For some reason this doesn't delete the value from the last name
	    	            // field when the form is redisplayed. Thus it's done explicitly in initFormView.
	    	            this.hideFields(lastNameWrapper);
	    	            // These get displayed if the selection was made through an enter keystroke,
	    	            // since the keydown event on the last name field is also triggered (and
	    	            // executes first). So re-hide them here.
	    	            this.hideFieldsForNewPerson(counter); 
	    	            personLink.attr('href', verifyMatch.data('baseHref') + authorUri);
	    	    }
		        else {
		        	var orgUriField = addAuthorForm.getElementByIdInClone(this.orgUriField.attr("id"), counter);
		            var selectedOrg = addAuthorForm.getElementByIdInClone(this.selectedOrg.attr("id"), counter);
		        	var selectedOrgName = addAuthorForm.getElementByIdInClone(this.selectedOrgName.attr("id"), counter);
		        	var orgNameWrapper = addAuthorForm.getElementByIdInClone(this.orgName.attr("id"), counter).parent();
		        	var orgLink = addAuthorForm.getElementByIdInClone(this.orgLink.attr("id"), counter);
		        	changeLinkId = "changeOrgSelection" + counter;

		        	// do the same as above but for the organization fields
		            orgUriField.val(authorUri); 
		            selectedOrg.show();

		            selectedOrgName.html(authorLabel);

		            this.hideFields(orgNameWrapper); 

		            orgLink.attr('href', verifyMatch.data('baseHref') + authorUri);
		        }
	    	}
	    	
	    	 // Cancel restores initial form view
	        this.cancel.unbind('click');
	        this.cancel.bind('click', function() {
	            addAuthorForm.initFormView();
	            addAuthorForm.setAuthorType(authType, $el, counter);
	            return false;
	        });
	        
	    	
	    	 //Attaching event to change link
	        $changeLink = $("a#" + changeLinkId);
	        $changeLink.click(function() {
	            addAuthorForm.undoAutocompleteSelection($(this), authType, counter);
	        });
	    }		
};

$(document).ready(function() {   
    addAuthorForm.onLoad();
 
}); 
