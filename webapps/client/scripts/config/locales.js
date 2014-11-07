define(['moment'], function(moment) {
  'use strict';

  return function(ngModule, appRoot) {

    ngModule.factory('localeLoader', ['$q', '$http',
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
            deferred.resolve(data.labels);
          }).error(function (data) {
            if(typeof options.callback === "function") {
              options.callback(data);
            }
            deferred.reject(options.key);
          });

          return deferred.promise;
        };
      }]);

    ngModule.config([
      '$translateProvider',
      'configurationProvider',
      function(
      $translateProvider,
       configurationProvider
      ) {

        $translateProvider.useLoader('localeLoader', {
          prefix: appRoot + '/app/tasklist/locales/',
          suffix: '.json',
          callback: function(err, data) {
            if(!err && data && data.dateLocales) {
              moment.lang(configurationProvider.getPreferredLocale() || 'en', data.dateLocales);
            }
          }
        });
        $translateProvider.preferredLanguage(configurationProvider.getPreferredLocale());
      }]);
  };
});
