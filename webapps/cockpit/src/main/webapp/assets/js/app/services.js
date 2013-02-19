'use strict';

/* Services */

angular.module('cockpit.services', ['ngResource'])
  .value('Debouncer', {
    /**
     * Debounce a function call, making it callable an arbitrary number of times before it is actually executed once. 
     * 
     * @param {function()} func The function to debounce.
     * @param {number} wait The debounce timeout.
     * @return {function()} A function that can be called an arbitrary number
     *         of times within the given time.
     */
    debounce: function(func, wait) {
      var timer;
      return function() {
        var context = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function() {
          timer = null;
          func.apply(context, args);
        }, wait);
      };
    }
  })
  /**
   * Provides the app service with the functions 
   * 
   *  root() -> returns the application root
   *  uri(str) -> returns a application root relative uri from the given argument
   */
  .provider('App', function() {
    this.root = function () {
      return $("base").attr("app-base");
    }

    this.uri = function (str) {
      return root() + (str.indexOf("/") == 0 ? str.substring(1, str.length) : str);
    }

    this.$get = angular.noop;
  })
  .factory('ProcessDefinition', function($resource, App) {
    return $resource(App.uri('secured/resource/roundtrip/:id'), {id: "@id"}, {});
  })
  .service('Error', function () {
    return {
      errors : [],
      errorConsumers : [],  
      addError: function (error) {
        this.errors.push(error); 
        this.errorConsumers[this.errorConsumers.length-1](error);
      },
      removeError: function(error) {
    	var idx = this.errors.indexOf(error);
    	  this.errors.splice(idx,1);  
      },
      removeAllErrors: function() {
        // not assigning a new [], because it still can be referenced somewhere => memory leak
        this.errors.length = 0;
      },
      registerErrorConsumer: function(callback) {
        this.errorConsumers.push(callback);  
      },
      unregisterErrorConsumer: function(callback) {
        this.errorConsumers.splice(this.errorConsumers.indexOf(callback),1);
      }      
    };
  })
  .service('HttpUtils', function($q) {
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
  })
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
  })
  /**
   * RequestStatus isBusy=true -> cockpit is processing an AJAX request
   */
  .factory('RequestStatus', function() {

    function RequestStatus() {
      
      var self = this;
      
      // bind watchCurrent to credentials to make it directly accessible
      // for scope.$watch(RequestStatus.watchBusy)
      self.watchBusy = function() {
        return self.busy;
      };      
    }

    RequestStatus.prototype = {
      isBusy: function() {
        return busy;
      },
      setBusy: function(busy) {
    	this.busy = busy; 
      }    
    };

    return new RequestStatus();
  });