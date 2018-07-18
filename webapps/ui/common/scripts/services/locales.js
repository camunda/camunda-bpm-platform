'use strict';
var moment = require('camunda-commons-ui/vendor/moment'),
    angular = require('camunda-commons-ui/vendor/angular');

var now = (new Date()).getTime();

module.exports = function(ngModule, appRoot, appName) {
  ngModule.factory('localeLoader', ['$q', '$http', 'Notifications', 'configuration',
      function($q, $http, Notifications, configuration) {
        return function(options) {

          if (!options || (!angular.isString(options.prefix) || !angular.isString(options.suffix))) {
            throw new Error('Couldn\'t load static files, no prefix or suffix specified!');
          }

          var deferred = $q.defer();
          var cacheKey = options.prefix+'_locales_data_'+options.key;
          var cachedLocalesData = configuration.get(cacheKey);

          if(cachedLocalesData) {
            cachedLocalesData = JSON.parse((cachedLocalesData));
            if(typeof options.callback === 'function') {
              options.callback(null, cachedLocalesData, options.key);
            }
            deferred.resolve(cachedLocalesData.labels);
          }

          $http(angular.extend({
            url: [
              options.prefix,
              options.key,
              options.suffix
            ].join(''),
            method: 'GET',
            params: { '_' : now }
          }, options.$http))
            .success(function(data) {
              configuration.set(cacheKey, JSON.stringify(data));
              if (!cachedLocalesData) {

                if(typeof options.callback === 'function') {
                  options.callback(null, data, options.key);
                }

                deferred.resolve(data.labels);
              }
            })
            .error(function(data) {
              // error notification
              Notifications.addError({
                status: 'Error in localization configuration',
                message: '"' + options.key + '" is declared as available locale, but no such locale file exists.'
              });

              if (!cachedLocalesData) {

                if(typeof options.callback === 'function') {
                  options.callback(data, null, options.key);
                }

                deferred.reject(options.key);
              }
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
      var avail = configurationProvider.getAvailableLocales();
      var fallback = configurationProvider.getFallbackLocale();

      $translateProvider.useLoader('localeLoader', {
        prefix: appRoot + '/app/'+ appName +'/locales/',
        suffix: '.json',
        callback: function(err, data, locale) {
          if(!err && data && data.dateLocales) {
              // Deprecation warning: moment.lang is deprecated. Use moment.locale instead.
            moment.locale(locale || fallback, data.dateLocales);
          }
        }
      });

      $translateProvider.registerAvailableLanguageKeys(avail);
      $translateProvider.fallbackLanguage(fallback);

      $translateProvider.determinePreferredLanguage(function() {
        var nav = window.navigator;
        var browserLang = ((angular.isArray(nav.languages) ? nav.languages[0] :
                              nav.language ||
                              nav.browserLanguage ||
                              nav.systemLanguage ||
                              nav.userLanguage
                            ) || '').split('-');
        var idx = avail.indexOf(angular.lowercase(browserLang[0]));
        if (idx > -1) {
          return avail[idx];
        } else {
          return fallback;
        }
      });
    }]);
};
