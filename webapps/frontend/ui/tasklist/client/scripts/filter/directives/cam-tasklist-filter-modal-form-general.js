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

var template = require('./cam-tasklist-filter-modal-form-general.html?raw');

module.exports = [
  function() {
    return {
      restrict: 'A',
      require: '^camTasklistFilterModalForm',
      scope: {
        filter: '=',
        accesses: '='
      },

      template: template,

      link: function($scope, $element, attrs, parentCtrl) {
        // init //////////////////////////////////////////////////////////

        var _form = $scope.filterGeneralForm;

        var controls = [];
        controls.push(_form.filterColor);
        controls.push(_form.filterName);
        controls.push(_form.filterPriority);
        controls.push(_form.filterDescription);
        controls.push(_form.filterRefresh);

        // register hint provider ////////////////////////////////////////

        var showHintProvider = function() {
          for (var i = 0, control; (control = controls[i]); i++) {
            if (control.$dirty && control.$invalid) {
              return true;
            }
          }
          return false;
        };

        parentCtrl.registerHintProvider('filterGeneralForm', showHintProvider);
      }
    };
  }
];
