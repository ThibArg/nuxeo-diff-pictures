/* nuxeo-diff-pictures.js

*/

NxDiffPictures = {

	init : function (inLeftDocId, inRightDocId, inResultImgId) {
		
		var url;
		
		// The code is called twice: When the fancybox is initialized but not
		// yet displayed, and when it is displayed
		// This leads to problem with the cropping tool, which duplicates
		// the picture and losts itself.
		// Also, a quick reminder about using getElementById() with jQuery: we can't
		// use jQuery(@-"#" + inNxDocId) because nuxeo (well, JSF), sometimes adds
		// colons inside the id, so jQuery is lost.
		debugger;
		var fancybox = jQuery("#fancybox-content");
		if(fancybox && fancybox.is(":visible")) {
			
			url = "/nuxeo/diffPictures?leftDocId=" + inLeftDocId + "&rightDocId=" + inRightDocId;
			console.log("ZE URL: " + url);
			resultImgObj = jQuery(document.getElementById(inResultImgId));
			resultImgObj.attr("alt", "coucoucoucou");
			resultImgObj.attr("src", url);
		}
	}
	
	
};
