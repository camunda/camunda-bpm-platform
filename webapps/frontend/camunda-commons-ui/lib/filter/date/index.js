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

var angular = require('camunda-bpm-sdk-js/vendor/angular');
var moment = require('camunda-bpm-sdk-js/vendor/moment');
require('angular-translate');

var filtersModule = angular.module('cam.commons.filter.date', [
  'pascalprecht.translate'
]);

filtersModule.provider('camDateFormat', function() {
  var variants = {
    normal: 'LLL',
    short: 'LL',
    long: 'LLLL'
  };

  this.setDateFormat = function(newFormat, variant) {
    variant = variant || 'normal';
    variants[variant] = newFormat;
  };

  this.$get = function() {
    return function(variant) {
      variant = variant || 'normal';
      return variants[variant];
    };
  };
});

filtersModule.config([
  '$filterProvider',
  function($filterProvider) {
    $filterProvider.register('camDate', [
      '$translate',
      'camDateFormat',
      function($translate, camDateFormat) {
        return function(date, variant) {
          if (!date) {
            return '';
          }

          if (typeof date === 'number') {
            date = new Date(date);
          }

          return moment(date, moment.ISO_8601).format(camDateFormat(variant));
        };
      }
    ]);
  }
]);

module.exports = filtersModule;
