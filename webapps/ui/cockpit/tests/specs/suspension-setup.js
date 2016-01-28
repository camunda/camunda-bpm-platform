'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;


module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'suspension-process',
        files:           [{
          name: 'suspension-process.bpmn',
          content: readResource('suspension-process.bpmn')
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
