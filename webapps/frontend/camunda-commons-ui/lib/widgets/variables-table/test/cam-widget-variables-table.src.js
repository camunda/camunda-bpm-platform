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

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
    variablesTableDefinition = require('../cam-widget-variables-table'),
    renderVarTemplateDefinition = require('../cam-render-var-template'),
    fancyDialogTemplate = require('./fancy-modal.html'),
    variableDefinition = require('../../variable/cam-widget-variable'),
    clipboardWidget = require('../../clipboard/cam-widget-clipboard'),
    varUtils = require('../../variable/cam-variable-utils'),
    camCommonsUi = require('../../index');

require('angular-ui-bootstrap');
require('angular-translate');

var variableModule = angular.module('variableModule', [
  'ui.bootstrap'
]);

variableModule.directive('camWidgetVariablesTable', variablesTableDefinition);
variableModule.directive('camRenderVarTemplate', renderVarTemplateDefinition);
variableModule.directive('camWidgetVariable', variableDefinition);
variableModule.directive('camWidgetClipboard', clipboardWidget);

var testModule = angular.module('testModule', [camCommonsUi.name]);
var vars = [
  {
    name:   null,
    type:   null
  },
  {
    name:   'booleanVar',
    type:   'Boolean',
    value:  true
  },
  {
    name:   'dateVar',
    type:   'Date',
    value:  '2015-03-23T13:14:06.340'
  },
  {
    name:   'doubleVar',
    type:   'Double',
    value:  '12.34'
  },
  {
    name:   'integerVar',
    type:   'Integer',
    value:  '1000'
  },
  {
    name:   'longVar',
    type:   'Long',
    value:  '-100000000'
  },
  {
    name:   'nullVar',
    type:   'Null',
    value:  null
  },
  {
    name:   'objectVar',
    type:   'Object',
    value:  'rO0ABXNyACpvcmcuY2FtdW5kYS5icG0ucGEuc2VydmljZS5Db2NrcGl0VmFyaWFibGUAAAAAAAAAAQIAA0wABWRhdGVzdAAQTGphdmEvdXRpbC9MaXN0O0wABG5hbWV0ABJMamF2YS9sYW5nL1N0cmluZztMAAV2YWx1ZXEAfgACeHBzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAAAXcEAAAAAXNyAA5qYXZhLnV0aWwuRGF0ZWhqgQFLWXQZAwAAeHB3CAAAAUyieV6meHh0AAR0ZXN0dAAUY29ja3BpdFZhcmlhYmxlVmFsdWU',
    valueInfo: {
      objectTypeName: 'org.camunda.bpm.pa.service.CockpitVariable',
      serializationDataFormat: 'application/x-java-serialized-object'
    }
  },
  {
    name:   'shortVar',
    type:   'Short',
    value:  '-32768'
  },
  {
    name:   'stringVar',
    type:   'String',
    value:  'Some string value'
  },
  {
    name:   'bytesVar',
    type:   'Bytes',
    value:  null
  },
  {
    name:   'nonEditableVariable',
    type:   'String',
    value:  'hello world!'
  }
];







testModule.controller('example1Controller', [
  '$scope',
  '$q',
  '$timeout',
  '$uibModal',
  function(
    $scope,
    $q,
    $timeout,
    $modal
  ) {
    $scope.editFunction = function(info/*, i*/) {
      return $modal.open({
        template: fancyDialogTemplate,

        windowClass: 'doopi-doo',

        resolve: {
          variable: function() { return info.variable; },
          readonly: function() { return !info.editMode; }
        },

        controller: varUtils.modalCtrl
      }).result;
    };



    $scope.changes = [];
    $scope.saveFunction = function(info/*, i*/) {
    // copy otherwise it keeps the reference
      var copy = angular.copy(info.variable);
      copy.saved = 'Not saved';
      $scope.changes.push(copy);

      var deferred = $q.defer();

      $timeout(function() {
        copy.saved = 'Saved';
        deferred.resolve(copy);
      }, 500);

      return deferred.promise;
    };



    $scope.vars = angular.copy(vars).map(function(variable) {
      return {
        variable: variable
      };
    });

    $scope.isVariableEditable = function(info) {
      return info.variable.name !== 'nonEditableVariable';
    };

  }]);







testModule.controller('example2Controller', ['$scope', function($scope) {
  $scope.formatDownloadLink = function() {
    return 'http://i.ytimg.com/vi/2DzryjDrjCM/maxresdefault.jpg';
  };

  $scope.vars = angular.copy(vars).map(function(variable, v) {
    return {
      variable: variable,

      additions: {
        plain:      {html: 'variable #' + v},
        formatted:  {html: '<i>HTML</i>{{ variable.value }}'},
        missing:    {html: 'should not be printed'}
      }
    };
  });
}]);


angular.element(document).ready(function() {
  angular.bootstrap(document.body, [testModule.name, 'pascalprecht.translate']);
});
