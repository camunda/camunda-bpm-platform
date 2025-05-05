/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var moment = require('camunda-commons-ui/vendor/moment'),
  angular = require('camunda-commons-ui/vendor/angular');

module.exports = function(ngModule, appRoot, appName) {
  ngModule.factory('sanitizeMissingTranslationKey', [
    '$translateSanitization',
    function($sanitize) {
      return function(translationKey) {
        return $sanitize.sanitize(translationKey, 'text', 'escape');
      };
    }
  ]);

  ngModule.factory('localeLoader', [
    '$q',
    '$http',
    'Notifications',
    'configuration',
    function($q, $http, Notifications, configuration) {
      return function(options) {
        if (
          !options ||
          !angular.isString(options.prefix) ||
          !angular.isString(options.suffix)
        ) {
          throw new Error(
            "Couldn't load static files, no prefix or suffix specified!"
          );
        }

        var deferred = $q.defer();
        var cacheKey =
          options.prefix + '_locales_data_' + options.key + '_' + window.bust;

        var cachedLocalesData = configuration.get(cacheKey);

        if (cachedLocalesData) {
          cachedLocalesData = JSON.parse(cachedLocalesData);
          if (typeof options.callback === 'function') {
            options.callback(null, cachedLocalesData, options.key);
          }
          deferred.resolve(cachedLocalesData.labels);
        }

        $http(
          angular.extend(
            {
              url: [options.prefix, options.key, options.suffix].join(''),
              method: 'GET',
              // Use `now` instead of `window.bust` to update translations without rebuilding the app
              params: {bust: CAMUNDA_VERSION} // eslint-disable-line
            },
            options.$http
          )
        )
          .then(function(response) {
            configuration.clearTranslationData();
            configuration.set(cacheKey, JSON.stringify(response.data));
            if (!cachedLocalesData) {
              if (typeof options.callback === 'function') {
                options.callback(null, response.data, options.key);
              }

              deferred.resolve(response.data.labels);
            }
          })
          .catch(function(response) {
            // error notification
            Notifications.addError({
              status: 'Error in localization configuration',
              message:
                '"' +
                options.key +
                '" is declared as available locale, but no such locale file exists.'
            });

            if (!cachedLocalesData) {
              if (typeof options.callback === 'function') {
                options.callback(response.data, null, options.key);
              }

              deferred.reject(options.key);
            }
          });

        return deferred.promise;
      };
    }
  ]);

  ngModule.config([
    '$translateProvider',
    'configurationProvider',
    function($translateProvider, configurationProvider) {
      $translateProvider.useMissingTranslationHandler(
        'sanitizeMissingTranslationKey'
      );
      var avail = configurationProvider.getAvailableLocales();
      var fallback = configurationProvider.getFallbackLocale();

      $translateProvider.useLoader('localeLoader', {
        prefix: appRoot + '/app/' + appName + '/locales/',
        suffix: '.json',
        callback: function(err, data, locale) {
          if (!err && data && data.dateLocales) {
            var abbreviation = locale || fallback;
            if (moment.locales().indexOf(abbreviation) > -1) {
              moment.updateLocale(abbreviation, data.dateLocales);
            } else {
              // Define new locale if it does not exist yet
              moment.defineLocale(abbreviation, data.dateLocales);
            }
          }
        }
      });

      $translateProvider.registerAvailableLanguageKeys(avail);
      $translateProvider.fallbackLanguage(fallback);
      $translateProvider.useSanitizeValueStrategy('escapeParameters');

      $translateProvider.determinePreferredLanguage(function() {
        var nav = window.navigator;
        var browserLang = (
          (angular.isArray(nav.languages)
            ? nav.languages[0]
            : nav.language ||
              nav.browserLanguage ||
              nav.systemLanguage ||
              nav.userLanguage) || ''
        ).split('-');
        var idx = avail.indexOf(browserLang[0].toLowerCase());
        if (idx > -1) {
          return avail[idx];
        } else {
          return fallback;
        }
      });
    }
  ]);
};
