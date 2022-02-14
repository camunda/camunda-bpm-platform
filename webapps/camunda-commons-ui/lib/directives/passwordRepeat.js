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
    this directive is placed on the Password (repeat) input field.
    it is configured with the name of the property which holds the password we must repeat.
  **/
module.exports = function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attrs, model) {
      // this is the name of the scope property
      // holding the value of the password we are trying to match.
      var repeatedPasswordName = attrs.passwordRepeat;

      // check match if we are changed
      model.$parsers.unshift(function(viewValue) {
        var repeatedPasswordValue = scope.$eval(repeatedPasswordName);
        var isValid = viewValue == repeatedPasswordValue;
        model.$setValidity('passwordRepeat', isValid);
        return viewValue;
      });

      // check match if password to repeat is changed
      scope.$watch(repeatedPasswordName, function(newValue) {
        var isValid = newValue == model.$viewValue;
        model.$setValidity('passwordRepeat', isValid);
        if (!isValid) {
          // make sure '$pristine' value is cleared even if the user
          // hasn't typed anything into the field yet.
          // if we do not clear '$pristine', the 'invalid' CSS rule does not match
          // and model will ne invalid but without visual feedback.
          model.$setViewValue(model.$viewValue);
        }
      });
    }
  };
};
