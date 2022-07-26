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

var angular = require('../../../../camunda-bpm-sdk-js/vendor/angular'),
  template = fs.readFileSync(
    __dirname + '/cam-widget-selection-type.template.html',
    'utf8'
  );

module.exports = function() {
  return {
    template: template,
    scope: {
      selectedInstancesCount: '@',
      totalInstancesCount: '@',
      toggleState: '@'
    },
    link: function(scope) {
      scope.selectionType = 'INSTANCE';

      scope.updateSelectionType = function(selectionType) {
        if (scope.selectionType !== selectionType) {
          scope.selectionType = selectionType;
        }

        scope.$emit('selection.type.updated', selectionType);
      };
    }
  };
};
