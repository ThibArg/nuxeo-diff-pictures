/* nuxeo-diff-pictures.js

 */
jQuery(document).ready(function() {
	jQuery(".select-language").dropdown();
});

var NxDiffPictures;
(function scope_NxDiffPictures() {

	var leftDocId, rightDocId,
		resultImgObj,
		resultImgSizeClass = "large"; // WARNING: Must mach the original declaration in nuxeo-diff-pictures.xhtml

	NxDiffPictures = this;

	init = function(inLeftDocId, inRightDocId, inResultImgId) {

		leftDocId = inLeftDocId;
		rightDocId = inRightDocId;

		// Please, do not ask why I have to do that, I have no idea. But if I don't, we have some messy-missing background in the UI
		jQuery("#fancybox-content").first().css("background-color", "white");

		// The code is called twice: When the fancybox is initialized but not
		// yet displayed, and when it is displayed
		// This leads to problem with the cropping tool, which duplicates
		// the picture and losts itself.
		// Also, a quick reminder about using getElementById() with jQuery: we can't
		// use jQuery(@-"#" + inNxDocId) because nuxeo (well, JSF), sometimes adds
		// colons inside the id, so jQuery is lost.
		//debugger;
		var fancybox = jQuery("#fancybox-content");
		if (fancybox && fancybox.is(":visible")) {

			resultImgObj = jQuery(document.getElementById(inResultImgId));
			updateResultImage();
  		}
	}

	function buildUrl() {
		var url = "/nuxeo/diffPictures?leftDocId=" + leftDocId + "&rightDocId=" + rightDocId;

		// . . . add parameters: fuzz, colors, . . .

		return url;
	}

	function updateResultImage() {

		var url = buildUrl();

		resultImgObj.attr("alt", "Comparison result not found");
		resultImgObj.attr("src", url);

	}

	changeResultSize = function(inSelect) {

		resultImgObj.removeClass(resultImgSizeClass);
		resultImgSizeClass = inSelect.value;
		resultImgObj.addClass(resultImgSizeClass);
		inSelect.value = "display size";

	}

}());
