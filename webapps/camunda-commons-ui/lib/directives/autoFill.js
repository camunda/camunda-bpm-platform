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

/**
 * This directive ensures that form fields
 * filled by browsers are properly recognized by angular.
 *
 * @example
 *
 * <input ng-model="password" auto-fill />
 */
module.exports = [
  '$interval',
  function($interval) {
    return {
      restrict: 'A',

      require: 'ngModel',

      link: function(scope, element, attrs, model) {
        // console.info('start watching for auto-filled field', attrs.name);

        var interval = $interval(function() {
          var value = element.val();
          if (value !== model.$viewValue) {
            model.$setViewValue(value);
            model.$setPristine();
          }

          if (
            typeof document.contains === 'function' &&
            !document.contains(element[0])
          ) {
            // console.info('stop watching for auto-filled field', attrs.name);
            $interval.cancel(interval);
          } else if (typeof document.contains !== 'function') {
            $interval.cancel(interval);
          }
        }, 500);
      }
    };
  }
];
