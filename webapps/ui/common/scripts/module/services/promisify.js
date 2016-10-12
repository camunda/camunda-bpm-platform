'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var includes = require('../../util/includes');
var getKeys = require('../../util/get-keys');

/**
 * Service that provides possibility to promisify node callback based function and APIs.
 * It uses angular $q service for promise functionality
 */
module.exports = [
  '$q',
  function($q) {
    return {
      promisifyFunction: promisifyFunction,
      promisifyObject: promisifyObject
    };

    /**
     * It take object and return it's promisfied version. Returned object has original object as it's prototype.
     *
     * @param obj - object that will be promisifed
     * @param methods - list of methods that should be promisified by default all methods are promisified
     * @param onlyOwnProperties - if true only own properties of obj are promisified
     */
    function promisifyObject(obj, methods, onlyOwnProperties) {
      methods = angular.isArray(methods) ? methods : getKeys(obj, !onlyOwnProperties);
      methods = filterMethods(obj, methods);

      var newObj = Object.create(obj);

      return getKeys(obj, !onlyOwnProperties)
        .reduce(function(newObj, key) {
          var value = obj[key];

          if (includes(methods, key)) {
            newObj[key] = promisifyFunction(value, obj);
          }

          return newObj;
        }, newObj);
    }

    function filterMethods(obj, methods) {
      return methods
        .filter(function(key) {
          return angular.isFunction(obj[key]);
        });
    }

    /**
     * Promisifies callback function. It assumes that callback is last argument to function and it is node callback.
     * That is first argument of callback is error if any and second is the desired result.
     *
     * @param func
     * @param thisArg - optional this argument, original function will be called with it as context
     * @returns {Function}
     */
    function promisifyFunction(func, thisArg) {
      return function() {
        var deferred = $q.defer();
        var args = Array.prototype.slice.call(arguments)
          .concat(callback);

        func.apply(thisArg, args);

        return deferred.promise;

        function callback(err, response) {
          if (err) {
            deferred.reject(err);
          }

          deferred.resolve(response);
        }
      };
    }
  }
];
