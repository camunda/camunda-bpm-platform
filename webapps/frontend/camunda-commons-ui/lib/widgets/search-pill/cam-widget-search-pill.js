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

var $ = require('jquery'),
  template = require('./cam-widget-search-pill.html')();

module.exports = [
  '$timeout',
  function($timeout) {
    return {
      restrict: 'A',

      scope: {
        valid: '=',
        extended: '=',
        basic: '=',
        allowDates: '=',
        enforceDates: '=',
        enforceString: '=',
        options: '=',
        invalidText: '@',
        deleteText: '@',

        type: '=',
        name: '=',
        potentialNames: '=?',
        operator: '=',
        value: '=',

        onChange: '&',
        onDelete: '&',

        disableTypeaheadAutoselect: '=?',
        allowNonOptions: '@?'
      },

      link: function($scope, element) {
        $scope.valueType = getValueType();
        $scope.potentialNames = $scope.potentialNames || [];

        $scope.changeSearch = function(field, value, evt) {
          var before = $scope[field].value;
          $scope[field].value = value;
          $scope[field].inEdit = false;
          if (typeof $scope.onChange === 'function') {
            $scope.onChange({
              field: field,
              before: before,
              value: value,
              $event: evt
            });
          }
        };

        $scope.clearEditTrigger = function(field) {
          $scope[field].inEdit = false;
        };

        $scope.onKeydown = function(evt, field) {
          if (evt.keyCode === 13 && evt.target === evt.currentTarget) {
            $scope[field].inEdit = true;
          }
        };

        $scope.$watch('allowDates', function(newValue) {
          if (!newValue) {
            $scope.valueType = getValueType();
          }
        });

        $scope.$watch('enforceDates', function(newValue) {
          if (newValue) {
            $scope.valueType = getValueType();
          }
        });

        $scope.$watch('enforceString', function(newValue) {
          if (newValue) {
            $scope.valueType = getValueType();
          }
        });

        var focusField = function(fieldName) {
          $timeout(function() {
            $(
              element[0].querySelectorAll(
                "[cam-widget-inline-field][value='" + fieldName + ".value']"
              )
            )
              .find('.view-value')
              .click();
          });
        };
        $scope.$watch(
          'value',
          function(newValue) {
            return newValue && newValue.inEdit && focusField('value');
          },
          true
        );
        $scope.$watch(
          'name',
          function(newValue) {
            return newValue && newValue.inEdit && focusField('name');
          },
          true
        );
        $scope.$watch(
          'type',
          function(newValue) {
            return newValue && newValue.inEdit && focusField('type');
          },
          true
        );
        $scope.$watch(
          'operator',
          function(newValue) {
            if (newValue && !newValue.value && newValue.values.length === 1) {
              newValue.value = newValue.values[0];
            }
            return newValue && newValue.inEdit && focusField('operator');
          },
          true
        );

        function getValueType() {
          if ($scope.options) {
            return 'option';
          }

          return $scope.enforceDates ? 'datetime' : 'text';
        }
      },

      template: template
    };
  }
];
