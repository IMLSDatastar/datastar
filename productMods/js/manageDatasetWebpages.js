/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var manageDatasetWebpages = {
		datasetUrlKey:"http://purl.org/datastar/DatasetURLLink",
		metadataUrlKey:"http://purl.org/datastar/MetadataURLLink",
		onLoad:function() {
			//set up the objects
			this.initObjects();
			this.initDisplay();
			this.bindEventListeners();
			
		}
		,
		initObjects:function() {
			this.urlTypeSelect = $("select[name='urlType']");
			//input and related label for the inputs for data file format and metadata format
			this.dataFileFormatElements = $("[forproperty='dataFileFormat']");
			this.metadataFormatElements = $("[forproperty='associatedMetadataFormat']");
			this.dataFileFormatInput = $("input[name='dataFileFormat']");
			this.metadataFormatInput = $("input[name='associatedMetadataFormat']");

		},
		initDisplay:function() {
			this.dataFileFormatElements.addClass("hidden");
			this.metadataFormatElements.addClass("hidden");
		},
		bindEventListeners:function() {
			this.urlTypeSelect.change(function(){
				manageDatasetWebpages.handleUrlTypeChange();
			});
		},
		handleUrlTypeChange:function() {
			//Hide/show based on which url type is selected
			if(manageDatasetWebpages.urlTypeSelect.val() == manageDatasetWebpages.datasetUrlKey) {
				//hide data, show metadata
				manageDatasetWebpages.dataFileFormatElements.removeClass("hidden");
				manageDatasetWebpages.metadataFormatElements.addClass("hidden");
				manageDatasetWebpages.metadataFormatInput.val("");

			} else if(manageDatasetWebpages.urlTypeSelect.val() == manageDatasetWebpages.metadataUrlKey) {
				manageDatasetWebpages.dataFileFormatElements.addClass("hidden");
				manageDatasetWebpages.metadataFormatElements.removeClass("hidden");
				manageDatasetWebpages.dataFileFormatInput.val("");
			} else {
				//Make sure they are both hidden
				manageDatasetWebpages.dataFileFormatElements.addClass("hidden");
				manageDatasetWebpages.metadataFormatElements.addClass("hidden");
				manageDatasetWebpages.dataFileFormatInput.val("");
				manageDatasetWebpages.metadataFormatInput.val("");

			}
			
		}

};

$(document).ready(function() {
	   manageDatasetWebpages.onLoad();
	});

