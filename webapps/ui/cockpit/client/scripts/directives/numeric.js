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

var Directive = function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attrs, model) {
      var pattern = attrs.integer
        ? /^-?[\d]+$/
        : /^(0|(-?(((0|[1-9]\d*)\.\d+)|([1-9]\d*))))([eE][-+]?[0-9]+)?$/;

      var numberParser = function(value) {
        var isValid = pattern.test(value);
        model.$setValidity('numeric', isValid);

        return isValid ? parseFloat(value, 10) : value;
      };

      model.$parsers.push(numberParser);

      var numberFormatter = function(value) {
        // if the value is not set,
        // then ignore it!
        if (value === undefined || value === null) {
          return;
        }

        // test the pattern
        var isValid = pattern.test(value);
        model.$setValidity('numeric', isValid);

        if (isValid) {
          // if the value is valid, then return the
          // value as a number
          return parseFloat(value, 10);
        } else {
          // if the value is invalid, then
          // set $pristine to false and set $dirty to true,
          // that means the user has interacted with the controller.
          model.$pristine = false;
          model.$dirty = true;

          // add 'ng-dirty' as class to the element
          element.addClass('ng-dirty');

          return value;
        }
      };

      model.$formatters.push(numberFormatter);
    }
  };
};

module.exports = Directive;
