'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('filter', 'create', [{
        name:         'All',
        query: {},
        properties: {
          priority: 100,
          description:  'Show all Tasks'
        },
        resourceType: 'Task'
      },
      {
        name:         'My Tasks',
        query: {
          assigneeExpression: '${ currentUser() }'
        },
        properties: {
          priority: 10
        },
        resourceType: 'Task'
      },
      {
        name:         'Test Filter',
        query: {},
        properties: {
          priority: 110
        },
        resourceType: 'Task'
      }]),

      operation('user', 'create', [{
        id: 'test',
        firstName: 'Montgomery',
        lastName: 'QA',
        password: 'test'
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

      operation('task', 'create', [{
        id: '1',
        name: 'Task 1'
      },
      {
        id: '2',
        name: 'Task 2'
      },
      {
        id: '3',
        name: 'Task 3'
      }]),

      operation('task', 'assignee', [{
        taskId: '1',
        userId: 'test'
      },
      {
        taskId: '2',
        userId: 'test'
      }])

)};
