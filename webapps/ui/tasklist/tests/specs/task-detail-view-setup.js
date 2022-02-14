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

var fragment1 = combine(
  operation('filter', 'create', [
    {
      name: 'All',
      query: {},
      resourceType: 'Task',
      properties: {
        variables: [
          {
            name: 'testVar',
            label: 'Test Variable'
          },
          {
            name: 'testString',
            label: 'String Variable'
          }
        ]
      }
    }
  ])
);

var fragment2 = combine(
  operation('task', 'create', [
    {
      id: '1',
      name: 'Task 1',
      description: 'This task is for testing purpose only!!!'
    }
  ])
);

var fragment3 = combine(
  operation('task', 'assignee', [
    {
      taskId: '1',
      userId: 'admin'
    }
  ])
);

var fragment4 = combine(
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
      businessKey: 'Instance1'
    }
  ])
);

var fragment5 = combine(
  operation('deployment', 'create', [
    {
      deploymentName: 'user-tasks',
      files: [
        {
          name: 'case-task.cmmn',
          content: readResource('case-task.cmmn')
        },
        {
          name: 'case-task2.cmmn',
          content: readResource('case-task2.cmmn')
        }
      ]
    }
  ]),
  operation('case-definition', 'create', [
    {
      key: 'Case_1',
      businessKey: 'Instance1'
    }
  ]),
  operation('case-definition', 'create', [
    {
      key: 'Case_2',
      businessKey: 'Instance1'
    }
  ])
);

var fragment6 = combine(
  operation('deployment', 'create', [
    {
      deploymentName: 'invoice',
      files: [
        {
          name: 'invoice-prevent.bpmn',
          content: readResource('invoice-prevent.bpmn')
        },
        {
          name: 'invoice-prevent.html',
          content: readResource('invoice-prevent.html')
        }
      ]
    }
  ]),
  operation('process-definition', 'start', [
    {
      key: 'invoice',
      businessKey: 'invoice'
    }
  ])
);

var multiTenancyFragment = combine(
  operation('task', 'create', [
    {
      id: '1',
      name: 'Task 1',
      tenantId: 'tenant1'
    },
    {
      id: '2',
      name: 'Task 2'
    }
  ])
);

module.exports = {
  setup1: combine(fragment1, fragment2),
  setup2: combine(fragment3),
  setup3: combine(fragment1, fragment4),
  setup4: combine(fragment1, fragment5),
  setup5: combine(fragment1, fragment6),
  multiTenancySetup: combine(fragment1, multiTenancyFragment)
};
