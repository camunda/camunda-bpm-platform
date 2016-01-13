'use strict';

var factory = require('../../setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

var deployFirst = combine(

operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: readResource('assign-approver-groups.dmn')
  }]
},{
  deploymentName: 'invoice',
  files: [{
    name: 'invoice.bpmn',
    content: readResource('invoice.bpmn')
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
    content: readResource('assign-approver-groups-changed.dmn')
  }]
}]);

var deployThird = combine(

operation('deployment', 'create', [{
  deploymentName: 'assign-approver-without-clause-name',
  files: [{
    name: 'assign-approver-groups-clauses-without-name.dmn',
    content: readResource('assign-approver-groups-clauses-without-name.dmn')
  }]
},{
  deploymentName: 'invoice',
  files: [{
    name: 'invoice.bpmn',
    content: readResource('invoice.bpmn')
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

var deploy4 = combine(

operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: readResource('assign-approver-groups.dmn')
  }]
}]),

operation('decision-definition', 'evaluate', [{
  key: 'invoice-assign-approver',
  variables: {
    amount: { value: 100 },
    invoiceCategory: { value: 'travelExpenses' }
  }
}])

);

module.exports = {

  setup1: deployFirst,
  setup2: combine(deployFirst, deploySecond),
  setup3: deployThird,
  setup4: deploy4

};
