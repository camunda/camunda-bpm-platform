/**
 * AngularUI - The companion suite for AngularJS
 * @version v0.2.0 - 2012-08-06
 * @link http://angular-ui.github.com
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */

// READ: http://docs-next.angularjs.org/guide/ie
(function(exports){
  
  var debug = window.ieShivDebug || false;
  
  var getIE = function() {
      // Returns the version of Internet Explorer or a -1
      // (indicating the use of another browser).
     var rv = -1; // Return value assumes failure.
     if (navigator.appName == 'Microsoft Internet Explorer') {
        var ua = navigator.userAgent;
        var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
        if (re.exec(ua) != null) {
        	rv = parseFloat( RegExp.$1 );
        }
     }
     return rv;
  };

  var toCustomElements = function(str,delim) {
	  var result = [];
	  var dashed = str.replace(/([A-Z])/g, function($1) { return " "+$1.toLowerCase();} );
      var tokens = dashed.split(' ');
      var ns = tokens[0];
      var dirname = tokens.slice(1).join('-');
      
      // this is finite list and it seemed senseless to create a custom method
      result.push(ns + ":" + dirname);
      result.push(ns + "-" + dirname);
      result.push("x-" + ns + "-" + dirname);
      result.push("data-" + ns + "-" + dirname);
      return result;
  };

  var shiv = function() {
	// TODO: unfortunately, angular is not exposing these in 'ng' module
	var tags = [ 'ngInclude', 'ngPluralize', 'ngView' ]; // angular specific, 
	
	// TODO: unfortunately, angular does not expose module names, it is a simple change to angular's loader.js
	// however, not sure if something happens when referencing them, so maybe an OK thing.
	
	var moduleNames = window.myAngularModules || []; // allow user to inject their own directives
	moduleNames.push('ui.directives');
	
	if(debug) console.log('moduleNames', moduleNames);
	for(var k = 0, mlen = moduleNames.length; k < mlen; k++) {
		var modules = angular.module(moduleNames[k]); // will throw runtime exception
		angular.forEach(modules._invokeQueue, function(item) {
	      // only allow directives
		  if(item[1] === "directive") {
	        var dirname = item[2][0];
	        tags.push(dirname);
		  } else {
			  if(debug) console.log("skipping",item[1], item[2][0]);
		  }
		});
	}
	
	if(debug) console.log("tags found", tags);
    for(var i = 0, tlen = tags.length; i < tlen; i++) {
    	if(debug) console.log("tag",tags[i]);
    	var customElements = toCustomElements(tags[i],':');
    	for(var j = 0, clen = customElements.length; j < clen; j++) {
    		var customElement = customElements[j];
    		if(debug) console.log("shivving",customElement);
    		document.createElement(customElement);
    	}
    }
  };
	
  var ieVersion = getIE();
  
  if ((ieVersion > -1 && ieVersion < 9) || debug) {
    shiv();
  }
  
})(window);