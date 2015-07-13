'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'user-tasks',
        files:           [{
          name: 'user-tasks.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/user-tasks.bpmn').toString()
        }]
      },
      {
        deploymentName:  'suspension-process',
        files:           [{
          name: 'suspension-process.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/suspension-process.bpmn').toString()
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

)};
