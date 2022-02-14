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
    operation('deployment', 'create', [
      {
        deploymentName: 'first-deployment',
        deploymentSource: 'process application',
        files: [
          {
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }
        ]
      },
      {
        deploymentName: 'second-deployment',
        files: [
          {
            name: 'suspension-process.bpmn',
            content: readResource('suspension-process.bpmn')
          }
        ]
      },
      {
        deploymentName: 'third-deployment',
        deploymentSource: 'cockpit',
        files: [
          {
            name: 'failing-process.bpmn',
            content: readResource('failing-process.bpmn')
          }
        ]
      }
    ]),

    operation('process-definition', 'start', [
      {
        key: 'user-tasks'
      },
      {
        key: 'suspension-process'
      },
      {
        key: 'failing-process'
      }
    ])
  ),

  setup2: combine(
    operation('deployment', 'create', [
      {
        deploymentName: 'bpmn',
        files: [
          {
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }
        ]
      },
      {
        deploymentName: 'cmmn',
        files: [
          {
            name: 'loan-application.cmmn',
            content: readResource('loan-application.cmmn')
          }
        ]
      },
      {
        deploymentName: 'dmn',
        files: [
          {
            name: 'assign-approver-groups.dmn',
            content: readResource('assign-approver-groups.dmn')
          },
          {
            name: 'drd.dmn',
            content: readResource('drd.dmn')
          }
        ]
      },
      {
        deploymentName: 'image',
        files: [
          {
            name: 'diagram.svg',
            content: readResource('diagram.svg')
          }
        ]
      },
      {
        deploymentName: 'script',
        files: [
          {
            name: 'my-script.groovy',
            content: readResource('my-script.groovy')
          }
        ]
      }
    ]),

    operation('process-definition', 'start', [
      {
        key: 'user-tasks'
      },
      {
        key: 'user-tasks'
      }
    ]),

    operation('case-definition', 'create', [
      {
        key: 'loanApplicationCase'
      },
      {
        key: 'loanApplicationCase'
      },
      {
        key: 'loanApplicationCase'
      }
    ])
  ),

  multiTenancySetup: combine(
    operation('deployment', 'create', [
      {
        deploymentName: 'processTenantOne',
        tenantId: 'tenant1',
        files: [
          {
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }
        ]
      }
    ]),

    operation('deployment', 'create', [
      {
        deploymentName: 'processNoTenant',
        files: [
          {
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }
        ]
      }
    ])
  )
};
