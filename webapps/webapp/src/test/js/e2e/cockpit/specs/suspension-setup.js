'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;


module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'suspension-process',
        files:           [{
          name: 'suspension-process.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/suspension-process.bpmn').toString()
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'suspension-process',
        businessKey: 'Instance1'
      },
      {
        key: 'suspension-process',
        businessKey: 'Instance2'
      },
      {
        key: 'suspension-process',
        businessKey: 'myBusinessKey'
      }])

)};
