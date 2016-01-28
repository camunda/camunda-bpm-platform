'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('filter', 'create', [{
        name:         'Empty Filter',
        query: {},
        properties: {
          priority: 5,
          description:  'Filter without variable definitions'
        },
        resourceType: 'Task'
      }]),

      operation('user', 'create', [{
        id: 'test',
        firstName: 'Montgomery',
        lastName: 'QA',
        password: 'test'
      },
      {
        id: 'juri',
        firstName: 'Juri',
        lastName: 'Gagarin',
        password: 'juri'
      }]),

      operation('group', 'create', [{
        id:   "marketing",
        name: "Marketing",
        type: "WORKFLOW"
      }]),

      operation('group', 'createMember', [{
        id:     "marketing",
        userId: "juri"
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
        userId: 'juri',
        groupId: null,
        resourceType: 0,
        resourceId: 'tasklist'
      }])

)};
