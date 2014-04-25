var addDynamicFieldsUtils = {
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
	    //for ids with numbers at the end, this replaces the old number with the new one
	    replaceIds: function(clonedFields, oldCount, newCount) {
	    	clonedFields.find("[id]").each(function(index, el) { 
	    		var originalId = $(this).attr("id");
	    		var strOldCount = "" + oldCount;
	    		var mainIdPattern = originalId.substr(0, originalId.length - strOldCount.length);
	    		var newId = mainIdPattern + newCount;
	    		$(this).attr("id", newId);
	    	});
	    	//renaming names as well because we want to submit this directly
	    	//to edit configuration
	    	clonedFields.find("[name]").each(function(index, el) { 
	    		var originalName = $(this).attr("name");
	    		var strOldCount = "" + oldCount;
	    		var mainNamePattern = originalName.substr(0, originalName.length - strOldCount.length);
	    		var newName = mainNamePattern + newCount;
	    		$(this).attr("name", newName);
	    	});
	    },
	    //this applies in the case where you have a new first element and the names/ids shouldn't have any numbers at all
	    replaceIdsForFirstElement: function(clonedFields, oldCount) {
	    	clonedFields.find("[id]").each(function(index, el) { 
	    		var originalId = $(this).attr("id");
	    		var strOldCount = "" + oldCount;
	    		var mainIdPattern = originalId.substr(0, originalId.length - strOldCount.length);
	    		$(this).attr("id", mainIdPattern);
	    	});
	    	//renaming names as well because we want to submit this directly
	    	//to edit configuration
	    	clonedFields.find("[name]").each(function(index, el) { 
	    		var originalName = $(this).attr("name");
	    		var strOldCount = "" + oldCount;
	    		var mainNamePattern = originalName.substr(0, originalName.length - strOldCount.length);
	    		$(this).attr("name", mainNamePattern);
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
		}
		
};