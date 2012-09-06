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
      }
    }
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
  });