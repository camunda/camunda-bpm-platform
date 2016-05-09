'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('filter', 'create', [{
        name: 'Empty Filter',
        query: {},
        properties: {
          priority: 5,
          description: 'Filter without variable definitions',
        },
        resourceType: 'Task'
      }]),

      operation('user', 'create', [{
        id: 'test',
        firstName: 'test',
        lastName: 'test',
        password: 'test'
      }]),

      operation('group', 'create', [{
        id:   "sales",
        name: "Sales",
        type: "WORKFLOW"
      }]),

      operation('group', 'createMember', [{
        id:     "sales",
        userId: "test"
      }]),

      operation('authorization', 'create', [{
        type : 1,
        permissions: ['ALL'],
        userId: 'test',
        groupId: null,
        resourceType: 0,
        resourceId: 'tasklist'
      },
      {
        type : 1,
        permissions: ['ALL'],
        userId: 'test',
        groupId: null,
        resourceType: 5,
        resourceId: '*'
      },
      {
        type : 1,
        permissions: ['READ'],
        userId: 'test',
        groupId: null,
        resourceType: 7,
        resourceId: '*'
      }]),

      operation('deployment', 'create', [{
        deploymentName:  'user-tasks',
        files:           [{
          name: 'user-tasks.bpmn',
          content: readResource('user-tasks.bpmn')
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'user-tasks',
        businessKey: 123
      }]),

      operation('task', 'create', [{
        id: '1',
        name: 'Task 1',
        owner: 'test',
        delegationState: 'PENDING',
        due: '2016-08-30T10:01:59',
        followUp: '2019-08-25T11:00:01'
      },
      {
        id: '2',
        name: 'Task 2',
        owner: 'test',
        delegationState: 'PENDING',
        due: '2014-08-30T10:01:59',
        followUp: '2014-08-25T11:00:01'
      }]),

      operation('task', 'assignee', [{
        taskId: '2',
        userId: 'test'
      }]),

      operation('task', 'identityLinksAdd', [{
        id: '1',
        groupId: 'sales',
        type: 'candidate'
      },
      {
        id: '2',
        groupId: 'sales',
        type: 'candidate'
      }])

    ),
    
    multiTenancySetup:

      combine(
        operation('filter', 'create', [{
          name: 'Empty Filter',
          query: {},
          properties: {
            priority: 5,
            description: 'Filter',
          },
          resourceType: 'Task'
        }]),

        operation('task', 'create', [{
          id: '1',
          name: 'Task 1',
          tenantId: 'tenant1',
          owner: 'test'
        },
        {
          id: '2',
          name: 'Task 2',
          tenantId: 'tenant2',
          owner: 'test'
        },
        {
          id: '3',
          name: 'Task 3',
          owner: 'test'
        }])
    )
    
};
