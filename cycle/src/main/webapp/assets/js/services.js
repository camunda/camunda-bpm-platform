'use strict';

/* Services */

angular
  .module('cycle.services', ['ngResource'])
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
  .factory('App', function() {
    function root() {
      return $("base").attr("app-base");
    }
    
    return {
      root: root, 
      uri: function(str) {
        return root() + (str.indexOf("/") == 0 ? str.substring(1, str.length) : str);
      }
    };
  })
  .factory('Roundtrip', function($resource, App) {
    return $resource(App.uri('secured/resource/roundtrip/:id'), {id: "@id"}, {});
  })
  .factory('RoundtripDetails', function($resource, App) {
    return $resource(App.uri('secured/resource/roundtrip/:id/details'), {id: "@id"}, {});
  })
  .factory('Commons', function($http, HttpUtils, App) {
    return {
      getModelerNames: function() {
        return HttpUtils.makePromise($http.get(App.uri('secured/resource/diagram/modelerNames')));
      },
      getConnectors: function() {
        return HttpUtils.makePromise($http.get(App.uri("secured/resource/connector/list")));
      },
      isImageAvailable : function (node) {
        var uri = "secured/resource/connector/" + node.connectorId + "/contents/info?type=PNG_FILE&nodeId=" + escape(node.id);
        return $http.get(App.uri(uri));
      },
      isContentAvailable: function (node) {
        var uri = "secured/resource/connector/" + node.connectorId + "/contents/info?nodeId=" + encodeURI(node.id);
        return $http.get(App.uri(uri));
      },
      getDiagramStatus: function(diagram) {
        var uri = "secured/resource/diagram/" + diagram.id + "/syncStatus";
        return $http.get(App.uri(uri));
      }, 
      getImageUrl: function (node, update) {
        var uri = App.uri("secured/resource/connector/" + node.connectorId + "/contents?type=PNG_FILE&nodeId=" + encodeURI(node.id));
        if (update) {
          uri +="&ts=" + new Date().getTime();
        }
        return uri;
      }
    };
  })
  .service('Error', function () {
    return {
      errors : [],
      addError: function (error) {
        this.errors.push(error);
        $('.errorPanel').removeClass("hide");
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
  .factory('cycleHttpInterceptor', function($q, Error) {
    return function(promise) {
      
      var blockTime = setTimeout(function() {
        $.blockUI({ message: '<h1>...</h1>'});
      }, 1300);
      
      return promise.then(function (response, arg1, arg2)  {
        clearTimeout(blockTime);
        $.unblockUI();
        return promise;
      }, function (response)  {
        clearTimeout(blockTime);
        $.unblockUI();
        console.log("error", response);
        Error.addError({ "status" : response.status , "config" :  response.config });
        return $q.reject(response);
      });
    };
  })
  /**
   * Lists all available events used in cycle for emit and on
   */
  .factory('Event', function() {
    return {
      userChanged : "user-changed",
      navigationChanged : "navigation-changed",
      roundtripAdded : "roundtrip-added",
      roundtripChanged : "roundtrip-changed",
      modelImageClicked : "model-image-clicked",
      componentError : "component-error",
      selectedConnectorChanged : "selected-connector-changed" ,
      destroy : "$destroy", // angular own event which is fired when current scope is destroyed
      ngChange : "change", // jquery + angularjs event
      imageAvailable :  "image-available"
    };
  });