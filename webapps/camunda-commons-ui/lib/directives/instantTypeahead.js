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

var secretEmptyKey = '[$empty$]';
/**
    this directive is used in combination with typeahead and opens the typeahead field on focus
  **/
module.exports = [
  function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, model) {
        // this parser run before typeahead's parser
        model.$parsers.unshift(function(inputValue) {
          var value = inputValue ? inputValue : secretEmptyKey; // replace empty string with secretEmptyKey to bypass typeahead-min-length check
          model.$viewValue = value; // this $viewValue must match the inputValue pass to typehead directive
          return value;
        });

        // this parser run after typeahead's parser
        model.$parsers.push(function(inputValue) {
          return inputValue === secretEmptyKey ? '' : inputValue; // set the secretEmptyKey back to empty string
        });

        scope.instantTypeahead = function(element, viewValue) {
          return (
            viewValue === secretEmptyKey ||
            ('' + element)
              .toLowerCase()
              .indexOf(('' + viewValue).toLowerCase()) > -1
          );
        };

        element.bind('click', function() {
          model.$setViewValue(model.$viewValue === ' ' ? '' : ' ');
          element.triggerHandler('input');
        });

        element.bind('input', function() {
          // update the view value to trigger re-evaluation of the model parsers
          model.$setViewValue(model.$viewValue);
        });
      }
    };
  }
];
