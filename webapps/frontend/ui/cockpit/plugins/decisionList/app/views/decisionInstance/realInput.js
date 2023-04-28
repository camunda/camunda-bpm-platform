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

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realInput',
      initialize: function(data) {
        var realInput, dataEl;
        var inputHeaders = angular.element('th[data-col-id]');

        inputHeaders &&
          inputHeaders.each(function(idx, inputHeader) {
            dataEl = data.decisionInstance.inputs.filter(function(inputObject) {
              return (
                inputObject.clauseId === inputHeader.getAttribute('data-col-id')
              );
            })[0];

            if (dataEl) {
              realInput = document.createElement('span');
              if (
                dataEl.type !== 'Object' &&
                dataEl.type !== 'Bytes' &&
                dataEl.type !== 'File'
              ) {
                realInput.className = 'dmn-input';
                realInput.textContent = ' = ' + dataEl.value;
              } else {
                realInput.className = 'dmn-input-object';
                realInput.setAttribute(
                  'title',
                  'Variable value of type ' + dataEl.type + ' is not shown'
                );
                realInput.textContent = ' = [' + dataEl.type + ']';
              }
              inputHeader.firstChild.appendChild(realInput);
            }
          });
      }
    });
  }
];
