'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

var deployFirst = combine(

operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: readResource('assign-approver-groups.dmn')
  },
  {
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
  },
  {
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

var multiTenancyDeployment = combine(

    operation('deployment', 'create', [{
      deploymentName:  'processTenantOne',
      tenantId: 'tenant1',
      files: [{
        name: 'invoice.bpmn',
        filename: 'invoice-deployment-binding.bpmn',
        content: readResource('invoice-deployment-binding.bpmn')
      },
      {
        name: 'assign-approver-groups.dmn',
        content: readResource('assign-approver-groups.dmn')
      }]
    }]),

    operation('deployment', 'create', [{
      deploymentName:  'processNoTenant',
      files: [{
        name: 'invoice.bpmn',
        filename: 'invoice-deployment-binding.bpmn',
        content: readResource('invoice-deployment-binding.bpmn')
      },
      {
        name: 'assign-approver-groups.dmn',
        content: readResource('assign-approver-groups.dmn')
      }]
    }]),

    operation('process-definition', 'start', [{
      key: 'invoice',
      tenantId: 'tenant1',
      variables: {
        creditor: {
          value: 'test',
          type: 'String'
        },
        amount : {
          value: 20.5,
          type: 'Double'
        },
        invoiceCategory : {
          value: 'Travel Expenses',
          type: 'String'
        }
      }
    }]),

    operation('process-definition', 'start', [{
      key: 'invoice',
      variables: {
        creditor: {
          value: 'test',
          type: 'String'
        },
        amount : {
          value: 15.0,
          type: 'Double'
        },
        invoiceCategory : {
          value: 'Travel Expenses',
          type: 'String'
        }
      }
    }])
);

var deployMultipleInstances = combine(

  operation('deployment', 'create', [{
    deploymentName: 'assign-approver',
    files: [
      {
        name: 'assign-approver-groups.dmn',
        content: readResource('assign-approver-groups.dmn')
      },
      {
        name: 'invoice.bpmn',
        content: readResource('invoice.bpmn')
      }
    ]
  }
  ]),

  operation('process-definition', 'start', [{
    key: 'invoice',
    businessKey: 'invoice1',
    variables: {
      amount: { value: 100 },
      invoiceCategory: { value: 'travelExpenses' }
    }
  }]),
  operation('process-definition', 'start', [{
    key: 'invoice',
    businessKey: 'invoice2',
    variables: {
      amount: { value: 200 },
      invoiceCategory: { value: 'travelExpenses2' }
    }
  }])

);

module.exports = {

  setup1: deployFirst,
  setup2: combine(deployFirst, deploySecond),
  setup3: deployThird,
  setup4: deploy4,
  setup5: deployMultipleInstances,
  multiTenancySetup: multiTenancyDeployment

};
