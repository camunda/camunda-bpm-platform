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
  ),

  setup2:

    combine(
      operation('deployment', 'create', [{
        deploymentName: 'bpmn',
        files: [{
          name: 'user-tasks.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/user-tasks.bpmn').toString()
        }]
      },
      {
        deploymentName: 'cmmn',
        files: [{
          name: 'loan-application.cmmn',
          content: fs.readFileSync(__dirname + '/../../resources/loan-application.cmmn').toString()
        }]
      },
      {
        deploymentName: 'dmn',
        files: [{
          name: 'assign-approver-groups.dmn',
          content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups.dmn').toString()
        }]
      },
      {
        deploymentName: 'image',
        files: [{
          name: 'diagram.svg',
          content: fs.readFileSync(__dirname + '/../../resources/diagram.svg').toString()
        }]
      },
      {
        deploymentName: 'script',
        files: [{
          name: 'my-script.groovy',
          content: fs.readFileSync(__dirname + '/../../resources/my-script.groovy').toString()
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'user-tasks'
      },
      {
        key: 'user-tasks'
      }]),

      operation('case-definition', 'create', [{
        key: 'loanApplicationCase'
      },
      {
        key: 'loanApplicationCase'
      },
      {
        key: 'loanApplicationCase'
      }])

    )
};
