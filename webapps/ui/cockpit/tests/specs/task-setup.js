'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

var singleTaskSetup = combine(
  combine(
    operation('deployment', 'create', [{
      deploymentName:  'parallel-user-tasks',
      files: [{
        name: 'parallel-user-tasks.bpmn',
        content: readResource('parallel-user-tasks.bpmn')
      }]
    }]),

    operation('process-definition', 'start', [{
      key : 'parallel-user-tasks',
      businessKey : 'Instance1',
      variables : {
        test : {
          value : 1.5,
          type : 'Double'
        },
        myString : {
          value : '123 dfg',
          type : 'String'
        },
        extraLong : {
          value : '1234567890987654321',
          type : 'Long'
        }
      }
    }])
  )
);

module.exports = {

  setup1: singleTaskSetup
  
};
