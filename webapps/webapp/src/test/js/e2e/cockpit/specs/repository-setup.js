'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;


module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'first-deployment',
        files: [{
          name: 'user-tasks.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/user-tasks.bpmn').toString()
        }]
      },
      {
        deploymentName:  'second-deployment',
        files: [{
          name: 'suspension-process.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/suspension-process.bpmn').toString()
        }]
      },
      {
        deploymentName:  'third-deployment',
        files: [{
          name: 'failing-process.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/failing-process.bpmn').toString()
        }]
      }]),

    operation('process-definition', 'start', [{
      key: 'user-tasks'
    },
    {
      key: 'suspension-process'
    },
    {
      key: 'failing-process'
    }])
)};
