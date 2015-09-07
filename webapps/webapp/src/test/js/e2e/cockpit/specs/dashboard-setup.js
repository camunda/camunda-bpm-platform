'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

var fragment1 = combine(
  operation('deployment', 'create', [{
    deploymentName: 'failing-process',
    files: [{
      name: 'failing-process.bpmn',
      content: fs.readFileSync(__dirname + '/../../resources/failing-process.bpmn').toString()
    }]
  }])
);

var fragment2 = combine(
  operation('process-definition', 'start', [{
    key: 'failing-process',
    businessKey: 'Instance1',
    variables: {
      test : {
        value: 1,
        type: 'Integer'
      }
    }
  }])
);

var fragment3 = combine(
  operation('deployment', 'create', [{
    deploymentName:  'process-with-subprocess',
    files:           [{
      name: 'process-with-sub-process.bpmn',
      content: fs.readFileSync(__dirname + '/../../resources/process-with-sub-process.bpmn').toString()
    }]
  }]),

  operation('process-definition', 'start', [{
    key: 'processWithSubProcess',
    businessKey: 'Instance1',
    variables: {
      test : {
        value: 1,
        type: 'Integer'
      }
    }
  }])
);

var dmnFragment = operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups.dmn').toString()
  }]
}]);

module.exports = {

  setup1: fragment1,
  setup2: fragment2,
  setup3: fragment3,
  setup4: dmnFragment
};
