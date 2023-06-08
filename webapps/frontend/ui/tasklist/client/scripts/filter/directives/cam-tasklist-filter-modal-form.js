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

var template = require('./cam-tasklist-filter-modal-form.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');

var isArray = angular.isArray;

var noop = function() {};

var GENERAL_ACCORDION = 'general',
  PERMISSION_ACCORDION = 'permission',
  CRITERIA_ACCORDION = 'criteria',
  VARIABLE_ACCORDION = 'variable';

module.exports = [
  function() {
    return {
      restrict: 'A',
      scope: {
        filter: '=',
        filterModalData: '=',
        registerIsValidProvider: '&',
        registerPostFilterSavedProvider: '&'
      },

      template: template,

      controller: [
        '$scope',
        function($scope) {
          // init ////////////////////////////////////////////////////////////////////////

          var filterModalFormData = ($scope.filterModalFormData = $scope.filterModalData.newChild(
            $scope
          ));

          $scope.registerIsValidProvider =
            $scope.registerIsValidProvider() || noop;
          $scope.registerPostFilterSavedProvider =
            $scope.registerPostFilterSavedProvider() || noop;

          var opened = GENERAL_ACCORDION;
          $scope.group = {
            general: opened === GENERAL_ACCORDION,
            permission: opened === PERMISSION_ACCORDION,
            criteria: opened === CRITERIA_ACCORDION,
            variable: opened === VARIABLE_ACCORDION
          };

          // observe //////////////////////////////////////////////////////////////////////

          filterModalFormData.observe('accesses', function(accesses) {
            $scope.accesses = accesses;
          });

          // init isValidProvider ////////////////////////////////////////////////////////

          var isValidProvider = function() {
            return $scope.filterForm.$valid;
          };

          $scope.registerIsValidProvider(isValidProvider);

          // handle hints ////////////////////////////////////////////////////////////////

          var hintProvider = {};
          this.registerHintProvider = function(formName, fn) {
            fn = fn || noop;
            hintProvider[formName] = fn;
          };

          $scope.showHint = function(formName) {
            var provider = hintProvider[formName];
            return provider && provider();
          };

          // handle submit after filter has been saved succesfully //////////////////////

          var postFilterSavedProviders = [];
          this.registerPostFilterSavedProvider = function(provider) {
            postFilterSavedProviders.push(
              provider ||
                function(filter, callback) {
                  return callback();
                }
            );
          };

          var postFilterSavedProvider = function(filter, callback) {
            var count = postFilterSavedProviders.length;

            if (count === 0) {
              return callback();
            }

            var errors = [];
            var localCallback = function(err) {
              count = count - 1;

              if (err) {
                if (isArray(err)) {
                  if (err.length) {
                    errors = errors.concat(err);
                  }
                } else {
                  errors.push(err);
                }
              }

              if (count === 0) {
                if (errors.length === 1) {
                  return callback(errors[0]);
                } else if (errors.length) {
                  return callback(errors);
                } else {
                  callback();
                }
              }
            };

            for (
              var i = 0, provider;
              (provider = postFilterSavedProviders[i]);
              i++
            ) {
              provider(filter, localCallback);
            }
          };

          $scope.registerPostFilterSavedProvider(postFilterSavedProvider);

          // helper ///////////////////////////////////////////////////////////////////////

          this.removeArrayItem = function(arr, delta) {
            var newArr = [];
            for (var key in arr) {
              if (key != delta) {
                newArr.push(arr[key]);
              }
            }
            return newArr;
          };
        }
      ]
    };
  }
];
