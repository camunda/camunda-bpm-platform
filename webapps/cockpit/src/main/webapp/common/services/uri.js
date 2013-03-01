"use strict";

(function() {
  var module = angular.module("common.services");
  
  var Service = function () {
    function appRoot() {
      return $("base").attr("app-base");
    }
    
    function restRoot() {
      return $("base").attr("rest-base");
    }
    
    function buildUri (context, str) {
      return context + (str.indexOf("/") === 0 ? str.substring(1, str.length) : str);
    }
    
    return {
      appRoot: appRoot,
      restRoot: restRoot,
      appUri: function(str) {
        return buildUri(appRoot(), str);;
      },
      restUri: function (str) {
        return buildUri(restRoot(), str);
      }
    };
  };

  module
    .factory("Uri", Service);
  
})();
