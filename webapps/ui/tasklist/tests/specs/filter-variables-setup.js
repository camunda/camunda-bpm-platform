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

var factory = require('../../../common/tests/setup-factory.js'),
  readResource = factory.readResource,
  combine = factory.combine,
  operation = factory.operation;

module.exports = {
  setup1: combine(
    operation('filter', 'create', [
      {
        name: 'Empty Filter',
        query: {},
        properties: {
          priority: 5,
          description: 'Filter without variable definitions'
        },
        resourceType: 'Task'
      },
      {
        name: 'Variable Filter',
        query: {},
        properties: {
          priority: 10,
          description: 'Filter with variable definitions',
          variables: [
            {
              name: 'myTestVar',
              label: 'my test variable'
            }
          ]
        },
        resourceType: 'Task'
      }
    ]),

    operation('deployment', 'create', [
      {
        deploymentName: 'user-tasks',
        files: [
          {
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }
        ]
      }
    ]),

    operation('process-definition', 'start', [
      {
        key: 'user-tasks',
        businessKey: 'Instance1',
        variables: {
          myTestVar: {
            value: 1.5,
            type: 'Double'
          },
          myString: {
            value: '123 dfg',
            type: 'String'
          },
          extraLong: {
            value: '1234567890987654321',
            type: 'Long'
          }
        }
      }
    ])
  )
};
