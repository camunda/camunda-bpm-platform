'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('user', 'create', [{
        id:         'ringo',
        password:   'cam123',
        firstName:  'Ringo',
        lastName:   'Starr',
        email:      'ringo.starr@the-beatles.com'
      }]),

      operation('task', 'create', [{
        id: '1',
        name: 'Task 1',
        assignee: 'test',
        due: '2016-09-15T15:45:48'
      },
      {
        id: '2',
        name: 'Task 2',
        assignee: 'test',
        due: '2016-09-16T15:45:48'
      },
      {
        id: 'abc123',
        name: 'My Task',
        assignee: 'test',
        due: '2016-09-15T15:46:48'
      }]),

      operation('task', 'localVariable', [{
        id: '1',
        varId: 'testVar',
        value: 42,
        type: 'Integer'
      }]),

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
      }]),

      operation('process-definition', 'start', [{
        key: 'invoice',
        businessKey: 'invoice1',
        variables: {
          amount: { value: 100 },
          invoiceCategory: { value: 'travelExpenses' }
        }
      }])

)};
