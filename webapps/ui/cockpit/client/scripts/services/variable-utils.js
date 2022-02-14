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

const angular = require('angular');

module.exports = [
  'fixDate',
  fixDate => {
    const generatePayload = ({payload = {variables: {}}, variables}) => {
      if (variables) {
        if (!payload.variables) {
          payload.variables = {};
        }

        variables.forEach(variable => {
          const name = variable.variable.name;
          payload.variables[name] = angular.copy(variable.variable);
          delete payload.variables[name].name;

          if (variable.variable.type === 'Date') {
            payload.variables[name].value = fixDate(
              payload.variables[name].value
            );
          }

          if (variable.variable.type === 'Object') {
            payload.variables[name].valueInfo = payload.variables[name]
              .valueInfo || {
              objectTypeName: '',
              serializationDataFormat: ''
            };
          } else {
            delete payload.variables[name].valueInfo;
          }
        });
      }

      return payload;
    };

    return {generatePayload: generatePayload};
  }
];
