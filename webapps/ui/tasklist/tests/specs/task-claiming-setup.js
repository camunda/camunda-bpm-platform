'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

var fragment1 = combine(
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
  }])
)

var fragment2 = combine(
  operation('task', 'assignee', [{
    taskId: '1',
    userId: 'test'
  }])
)

module.exports = {

  setup1: fragment1,
  setup2: combine(fragment1, fragment2)
};
