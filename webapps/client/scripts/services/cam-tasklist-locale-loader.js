define([], function() {
  'use strict';
  return ['$q', '$http',
  function($q,   $http) {
  return function (options) {

    if (!options || (!angular.isString(options.prefix) || !angular.isString(options.suffix))) {
      throw new Error('Couldn\'t load static files, no prefix or suffix specified!');
    }

    var deferred = $q.defer();

    $http(angular.extend({
      url: [
        options.prefix,
        options.key,
        options.suffix
      ].join(''),
      method: 'GET',
      params: ''
    }, options.$http)).success(function (data) {
      if(typeof options.callback === "function") {
        options.callback(null, data);
      }
      deferred.resolve(data);
    }).error(function (data) {
      if(typeof options.callback === "function") {
        options.callback(data);
      }
      deferred.reject(options.key);
    });

    return deferred.promise;
  };
  }];
});
