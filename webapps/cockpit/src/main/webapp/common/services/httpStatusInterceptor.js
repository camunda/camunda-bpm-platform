"use strict";

(function() {

  var module = angular.module("common.services");
  
  var Service = function ($rootScope, $q, Error, RequestStatus) {
    return function(promise) {

      RequestStatus.setBusy(true);

      var successCallback = function(response)  {
        RequestStatus.setBusy(false);
        return promise;
      };

      var errorCallback = function(response) {
        RequestStatus.setBusy(false);

        var status = parseInt(response.status);

        if (status === 500) {
          if (response.data && response.data.message) {
            Error.addError({ "status" : "Error" , "config" :  response.data.message , "type" : response.data.exceptionType });
          } else {
            Error.addError({ "status" : "Error" , "config" :  "A problem occurred: try logging out of cockpit and logging back in. If the problem persists, contact your administrator." });
          }
        } else
        if (status === 0) {
          Error.addError({ "status" : "Request Timeout" , "config" :  "Your request timed out. Try refreshing the page." });
        } else
        if (status === 401) {
          Error.addError({ "status" : "Unauthorized" , "config" :  "Your session has probably expired. Try refreshing the page and login again." });
          $rootScope.$broadcast("response-error", { status: status, data: response.data });
        } else {
          Error.addError({ "status" : "Error" , "config" :  "A problem occurred: try logging out of cockpit and logging back in. If the problem persists, contact your administrator." });
        }

        return $q.reject(response);
      };

      return promise.then(successCallback, errorCallback);
    };    
  };

  Service.$inject = ["$rootScope", "$q", "Error", "RequestStatus"];
  
  module
    .factory("httpStatusInterceptor", Service);
  
})();
