"use strict";

(function() {

  var module = angular.module("common.services");
  
  var Service = function ($q) {
    return {
      makePromise: function(http) {
        var deferred = $q.defer();
        http.success(function() {
          deferred.resolve.apply(this, arguments);
        });
        http.error(function() {
          deferred.reject.apply(this, arguments);
        });
        return deferred.promise;
      }
    };
  };
  
  Service.$inject = ["$q"];
  
  module
    .service('HttpUtils', Service);
  
})();
