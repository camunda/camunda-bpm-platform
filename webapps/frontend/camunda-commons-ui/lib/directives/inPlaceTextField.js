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

var fs = require('fs');

var angular = require('../../../camunda-bpm-sdk-js/vendor/angular');
var template = require('./inPlaceTextField.html')();

/**
 * @name inPlaceTextField
 * @memberof cam.common.directives.inPlaceTextField
 * @type angular.directive
 * @description Provides a widget for in place editing of a simple text variable.
 *
 * @example
 * <div cam-in-place-text-field context="someObject" property="keyOfTheObject" />
 */
module.exports = [
  '$translate',
  function($translate) {
    /**
     * Callback used when initialization of the directive completes.
     * @param {angular.Scope} scope - the directive scope
     */
    function initialized(scope) {
      scope.value = scope.context[scope.property] || scope.defaultValue || null;

      scope.enter = function() {
        scope.editing = true;
        scope.value = scope.context[scope.property];
      };

      scope.submit = function() {
        var editForm = this;

        // it seems that, because this method
        // is called from the <form ng-submit="...">
        // "this" is having the "uncommited `value`"
        if (scope.context[scope.property] === editForm.value) {
          scope.leave();
          return;
        }

        // the value has change, so we do update the scope.context property
        scope.context[scope.property] = editForm.value;

        if (angular.isFunction(scope.$parent[scope.submitCallback])) {
          scope.$parent[scope.submitCallback](editForm);
        }
        scope.leave();
      };

      scope.leave = function() {
        scope.editing = false;
      };
    }

    return {
      restrict: 'E',

      scope: {
        // from context to form value
        unserializeCallback: '@unserialize',
        // from form to context value
        serializeCallback: '@serialize',

        initializeCallback: '@initialize',
        enterCallback: '@enter',
        validateCallback: '@validate',
        submitCallback: '@submit',
        successCallback: '@success',
        errorCallback: '@error',
        leaveCallback: '@leave',
        context: '=',
        property: '@',
        defaultValue: '@default'
      },

      template: template,

      link: function(scope, element, attrs) {
        // if the attribute 'required' is present, then
        // then input field is required.
        scope.isRequired =
          attrs.required !== null && attrs.required !== undefined;

        if (!scope.property) {
          throw new Error(
            $translate.instant('DIRECTIVE_INPLACE_TEXTFIELD_ERROR_MSG')
          );
        }

        var initialize = scope.initializeCallback
          ? scope.$parent[scope.initializeCallback]
          : function(scope, cb) {
              cb();
            };

        initialize(scope, function() {
          initialized(scope);
        });
      }
    };
  }
];
