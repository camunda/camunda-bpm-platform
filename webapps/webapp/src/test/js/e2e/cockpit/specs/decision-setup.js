'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

var deployFirst = combine(

operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups.dmn').toString()
  }]
},{
  deploymentName: 'invoice',
  files: [{
    name: 'invoice.bpmn',
    content: fs.readFileSync(__dirname + '/../../resources/invoice.bpmn').toString()
  }]
}
]),

operation('process-definition', 'start', [{
  key: 'invoice',
  businessKey: 'invoice1',
  variables: {
    amount: { value: 100 },
    invoiceCategory: { value: 'travelExpenses' }
  }
}])

);

var deploySecond = operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups-changed.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups-changed.dmn').toString()
  }]
}]);

var deployThird = combine(

operation('deployment', 'create', [{
  deploymentName: 'assign-approver-without-clause-name',
  files: [{
    name: 'assign-approver-groups-clauses-without-name.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups-clauses-without-name.dmn').toString()
  }]
},{
  deploymentName: 'invoice',
  files: [{
    name: 'invoice.bpmn',
    content: fs.readFileSync(__dirname + '/../../resources/invoice.bpmn').toString()
  }]
}
]),

operation('process-definition', 'start', [{
  key: 'invoice',
  businessKey: 'invoice1',
  variables: {
    amount: { value: 100 },
    invoiceCategory: { value: 'travelExpenses' }
  }
}])

);

module.exports = {

  setup1: deployFirst,
  setup2: combine(deployFirst, deploySecond),
  setup3: deployThird

};
