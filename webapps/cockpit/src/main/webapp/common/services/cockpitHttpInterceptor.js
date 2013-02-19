angular
.module('cockpit.service.cockpit.http.interceptor', [])
.factory('cockpitHttpInterceptor', function($q, Error, RequestStatus) {
    return function(promise) {
      
      RequestStatus.setBusy(true);  
      
      return promise.then(function (response, arg1, arg2)  {
        RequestStatus.setBusy(false);
        return promise;
        
      }, function (response) {        
        RequestStatus.setBusy(false);
      
        if (parseInt(response.status) == 500) {
          if (response.data && response.data.message) {
            Error.addError({ "status" : "Error" , "config" :  response.data.message , "type" : response.data.exceptionType });
          } else {
            Error.addError({ "status" : "Error" , "config" :  "A problem occurred: try logging out of cockpit and logging back in. If the problem persists, contact your administrator." });
          }
          
        } else if (parseInt(response.status) == 0) {
          Error.addError({ "status" : "Request Timeout" , "config" :  "Your request timed out. Try refreshing the page." });
          
        } else if (parseInt(response.status) == 401) {
          Error.addError({ "status" : "Unauthorized" , "config" :  "Your session has probably expired. Try refreshing the page and login again." });
          
        } else {
          Error.addError({ "status" : "Error" , "config" :  "A problem occurred: try logging out of cockpit and logging back in. If the problem persists, contact your administrator." });
        }         
        return $q.reject(response);
      });
    };
  });