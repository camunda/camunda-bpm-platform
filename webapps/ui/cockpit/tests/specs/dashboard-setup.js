'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

var fragment1 = combine(
  operation('deployment', 'create', [{
    deploymentName: 'failing-process',
    files: [{
      name: 'failing-process.bpmn',
      content: readResource('failing-process.bpmn')
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
      content: readResource('process-with-sub-process.bpmn')
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
    content: readResource('assign-approver-groups.dmn')
  }]
  },{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups-changed.dmn',
    content: readResource('assign-approver-groups-changed.dmn')
  }]
}]);

var dmnFragment2 = operation('deployment', 'create', [{
  deploymentName: 'dmn-without-name',
  files: [{
    name: 'dmn-without-name.dmn',
    content: readResource('dmn-without-name.dmn')
  }]
}]);

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

module.exports = {

  setup1: fragment1,
  setup2: fragment2,
  setup3: fragment3,
  setup4: dmnFragment1,
  setup5: dmnFragment2,
  multiTenancySetup: multiTenancyDeployment
  
};
