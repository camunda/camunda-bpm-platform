'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'user-tasks',
        files:           [{
          name: 'user-tasks.bpmn',
          content: readResource('user-tasks.bpmn')
        }]
      },
      {
        deploymentName:  'suspension-process',
        files:           [{
          name: 'suspension-process.bpmn',
          content: readResource('suspension-process.bpmn')
        }]
      }]),

      operation('filter', 'create', [{
        name:         'All',
        query: {},
        properties: {
          variables: [{
            name: 'var_1',
            label: 'var_1'
          },
          {
            name: 'var_2',
            label: 'var_2'
          },
          {
            name: 'var_3',
            label: 'var_3'
          },
          {
            name: 'var_4',
            label: 'var_4'
          },
          {
            name: 'var_5',
            label: 'var_5'
          },
          {
            name: 'var_6',
            label: 'var_6'
          },
          {
            name: 'var_7',
            label: 'var_7'
          }],
          priority: 10,
          description:  'Show all Tasks'
        },
        resourceType: 'Task'
      }])
    ),
    
    multiTenancySetup:

      combine(
        operation('deployment', 'create', [{
          deploymentName:  'user-tasks',
          tenantId: 'tenant1',
          files:           [{
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }]
        }]),
        
        operation('deployment', 'create', [{
          deploymentName:  'user-tasks',
          files:           [{
            name: 'user-tasks.bpmn',
            content: readResource('user-tasks.bpmn')
          }]
        }]),

        operation('filter', 'create', [{
          name:         'All',
          query: {},
          properties: {
            priority: 10,
            description:  'Show all Tasks'
          },
          resourceType: 'Task'
        }])
  )
    
};
