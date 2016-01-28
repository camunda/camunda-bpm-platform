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

      operation('task', 'create', [{
        id: '1',
        name: 'Task 1'
      }])

)};
