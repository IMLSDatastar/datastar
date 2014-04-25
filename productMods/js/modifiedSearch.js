/* $This file is distributed under the terms of the license in /doc/license.txt$ */

var modifiedSearch = {
		onLoad:function() {
			this.mixIn();
			//set up the objects
			this.initObjects();
			this.initDisplay();
			this.bindEventListeners();
			
		},
	mixIn: function() {
	    // Mix in the custom form utility methods
	    $.extend(this, vitro.customFormUtils);
	    
	    // Get the custom form data from the page
	    $.extend(this, customFormData);
	},
		initObjects:function() {
			this.searchFacetDivs = $("div.searchFacet");
			this.navImg = $("div.searchFacet nav img");

		},
		initDisplay:function() {
			//Hide all the uls and a tags within search facet divs except type
			this.searchFacetDivs.each(function(index) {
				var facet = $(this).attr("facet");
				//check if geographic coverage facet is selected
				var doHide = true;
				if(facet == "type" || (facet == "geographicCoverage" && modifiedSearch.geographicCoverageFacet != "")) {
					doHide = false;
				}
				if(doHide) {
					//Hide ul and a within this div
					$(this).find("ul[facet='" + facet + "']").hide();
					$(this).find("a[facet='" + facet + "']").hide();
				}
			});
		},
		bindEventListeners:function() {
			//Clicking on the h4 
			$("div.searchFacet h4").click(function() {
				var parentDiv = $(this).parent();
				var facet = parentDiv.attr("facet");
				var img = parentDiv.find("nav.scroll-up img");
				var imgSrc = img.attr("src");
				var doExpand = false;
				//expand image means collapsed and you can expand
				if(imgSrc.indexOf("expand-prop-group.png") != -1) {
					doExpand = true;
				}
				
				if(doExpand) {
					parentDiv.find("ul[facet='" + facet + "']").show();
					parentDiv.find("a[facet='" + facet + "']").show();
					//Set img src
					var newImgSrc = imgSrc.replace("expand-prop-group.png", "collapse-prop-group.png");
					img.attr("src", newImgSrc);
					
				} else {
					parentDiv.find("ul[facet='" + facet + "']").hide();
					parentDiv.find("a[facet='" + facet + "']").hide();
					//Set img src
					var newImgSrc = imgSrc.replace("collapse-prop-group.png", "expand-prop-group.png");
					img.attr("src", newImgSrc);
				}
			});
			
			//Clicking on the arrow
			$("div.searchFacet nav.scroll-up").click(function() {
				var parentDiv = $(this).parent();
				var facet = parentDiv.attr("facet");
				var img = $(this).find("img");
				var imgSrc = img.attr("src");
				var doExpand = false;
				if(imgSrc.indexOf("expand-prop-group.png") != -1) {
					doExpand = true;
				}
				
				if(doExpand) {
					parentDiv.find("ul[facet='" + facet + "']").show();
					parentDiv.find("a[facet='" + facet + "']").show();
					//Set img src
					var newImgSrc = imgSrc.replace("expand-prop-group.png", "collapse-prop-group.png");
					img.attr("src", newImgSrc);
					
				} else {
					parentDiv.find("ul[facet='" + facet + "']").hide();
					parentDiv.find("a[facet='" + facet + "']").hide();
					//Set img src
					var newImgSrc = imgSrc.replace("collapse-prop-group.png", "expand-prop-group.png");
					img.attr("src", newImgSrc);
				}
			});
			
			
			
			
		}
	

};

$(document).ready(function() {
	   modifiedSearch.onLoad();
	});

