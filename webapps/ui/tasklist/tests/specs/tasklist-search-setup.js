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
          },
          {
            name: 'testString',
            label:'String Variable'
          }]
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
        name: 'Task 1'
      },
      {
        id: '2',
        name: 'Task 2'
      },
      {
        id: '3',
        name: 'Task 3',
        assignee: 'test'
      }]),

      operation('task', 'localVariable', [{
        id: '1',
        varId: 'testVar',
        value: 42,
        type: 'Integer'
      },
      {
        id: '2',
        varId: 'testVar',
        value: 48,
        type: 'Integer'
      },
      {
        id: '3',
        varId: 'testVar',
        value: 1000,
        type: 'Integer'
      },
      {
        id: '1',
        varId: 'testDate',
        value: '2013-11-30T10:03:01',
        type: 'Date'
      },
      {
        id: '2',
        varId: 'testDate',
        value: '2013-11-30T10:03:00',
        type: 'Date'
      },
      {
        id: '1',
        varId: 'testString',
        value: '4711',
        type: 'String'
      },
      {
        id: '2',
        varId: 'testString',
        value: 'asdfhans dampf',
        type: 'String'
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
