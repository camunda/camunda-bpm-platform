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
    operation('user', 'create', [
      {
        id: 'ringo',
        password: 'cam123',
        firstName: 'Ringo',
        lastName: 'Starr',
        email: 'ringo.starr@the-beatles.com'
      }
    ]),

    operation('task', 'create', [
      {
        id: '1',
        name: 'Task 1',
        assignee: 'test',
        due: '2016-09-15T15:45:48'
      },
      {
        id: '2',
        name: 'Task 2',
        assignee: 'test',
        due: '2016-09-16T15:45:48'
      },
      {
        id: 'abc123',
        name: 'My Task',
        assignee: 'test',
        due: '2016-09-15T15:46:48'
      }
    ]),

    operation('task', 'localVariable', [
      {
        id: '1',
        varId: 'testVar',
        value: 42,
        type: 'Integer'
      }
    ]),

    operation('deployment', 'create', [
      {
        deploymentName: 'assign-approver',
        files: [
          {
            name: 'assign-approver-groups.dmn',
            content: readResource('assign-approver-groups.dmn')
          }
        ]
      },
      {
        deploymentName: 'invoice',
        files: [
          {
            name: 'invoice.bpmn',
            content: readResource('invoice.bpmn')
          }
        ]
      }
    ]),

    operation('process-definition', 'start', [
      {
        key: 'invoice',
        businessKey: 'invoice1',
        variables: {
          amount: {value: 100},
          invoiceCategory: {value: 'travelExpenses'}
        }
      }
    ])
  )
};
