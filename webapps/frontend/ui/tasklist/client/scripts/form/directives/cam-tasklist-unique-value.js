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

module.exports = [
  function() {
    return {
      require: 'ngModel',

      link: function($scope, $element, $attrs, ctrl) {
        var validate = function(viewValue) {
          var names = JSON.parse($attrs.camUniqueValue);

          ctrl.$setValidity('camUniqueValue', true);

          if (viewValue) {
            if (ctrl.$pristine) {
              ctrl.$pristine = false;
              ctrl.$dirty = true;
              $element.addClass('ng-dirty');
              $element.removeClass('ng-pristine');
            }

            var nameFound = false;
            for (var i = 0; i < names.length; i++) {
              if (names[i] === viewValue) {
                if (nameFound) {
                  ctrl.$setValidity('camUniqueValue', false);
                  break;
                }
                nameFound = true;
              }
            }
          }
          return viewValue;
        };

        ctrl.$parsers.unshift(validate);
        ctrl.$formatters.push(validate);

        $attrs.$observe('camUniqueValue', function() {
          return validate(ctrl.$viewValue);
        });
      }
    };
  }
];
