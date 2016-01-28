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
        resourceType: 'Task',
        properties: {
          variables: [{
            name: 'testVar',
            label: 'Test Variable'
          }],
          showUndefinedVariable: true
        }
      }]),

      operation('user', 'create', [{
        id: 'test',
        firstName: 'test',
        lastName: 'test',
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
      }]),

      operation('task', 'localVariable', [{
        id: '1',
        varId: 'testVar',
        value: 42,
        type: 'Integer'
      }])

)};
