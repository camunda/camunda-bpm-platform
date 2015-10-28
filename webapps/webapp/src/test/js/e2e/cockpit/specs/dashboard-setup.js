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

var dmnFragment1 = operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups.dmn').toString()
  }]
  },{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups-changed.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups-changed.dmn').toString()
  }]
}]);

var dmnFragment2 = operation('deployment', 'create', [{
  deploymentName: 'dmn-without-name',
  files: [{
    name: 'dmn-without-name.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/dmn-without-name.dmn').toString()
  }]
}]);

module.exports = {

  setup1: fragment1,
  setup2: fragment2,
  setup3: fragment3,
  setup4: dmnFragment1,
  setup5: dmnFragment2
};
