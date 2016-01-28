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
      },
      {
        name:         'Variable Filter',
        query: {},
        properties: {
          priority: 10,
          description:  'Filter with variable definitions',
          variables: [
          {
            name: 'myTestVar',
            label: 'my test variable'
          }],
        },
        resourceType: 'Task'
      }]),

      operation('deployment', 'create', [{
        deploymentName:  'user-tasks',
        files: [{
          name: 'user-tasks.bpmn',
          content: readResource('user-tasks.bpmn')
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'user-tasks',
        businessKey: 'Instance1',
        variables: {
          myTestVar: {
            value: 1.5,
            type: 'Double'
          },
          myString : {
            value: '123 dfg',
            type: 'String'
          },
          extraLong : {
            value: '1234567890987654321',
            type: 'Long'
          }
        }
      }])

)};
