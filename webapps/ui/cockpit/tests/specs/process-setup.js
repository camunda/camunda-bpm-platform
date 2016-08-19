'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;


module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'user-tasks',
        files: [{
          name: 'user-tasks.bpmn',
          content: readResource('user-tasks.bpmn')
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'user-tasks',
        businessKey: 'Instance1',
        variables: {
          test: {
            value: 1.5,
            type: 'Double'
          },
          myString : {
            value: '123 dfg',
            type: 'String'
          },
          extraLong : {
            value: '1234567890987654321',
            type: 'Long'
          }
        }
      },
      {
        key: 'user-tasks',
        businessKey: 'Instance2',
        variables: {
          test : {
            value: 1.5,
            type: 'Double'
          },
          myString : {
              value: 'abc dfg',
              type: 'String'
          }
        }
      },
      {
        key: 'user-tasks',
        businessKey: 'myBusinessKey',
        variables: {
          test : {
            value: 1.49,
            type: 'Double'
          },
          myString : {
              value: 'abc dfg',
              type: 'String'
          },
          myDate : {
              value: '2011-11-11T11:11:11',
              type: 'Date'
          }
        }
      }])
    ),


  setup2:
    combine(
      operation('deployment', 'create', [{
        deploymentName:  'mi-incident',
        files: [{
          name: 'mi-incident.bpmn',
          content: readResource('mi-incident.bpmn')
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'mi-incident',
        businessKey: 'MultiInstance'
      }])
    ),

  setup3:
    combine(
      operation('deployment', 'create', [{
        deploymentName:  'bulk-retry',
        files: [{
          name: 'mi-incident.bpmn',
          content: readResource('mi-incident.bpmn')
        }]
      }]),

      operation('deployment', 'create', [{
        deploymentName:  'four-fails',
        files: [{
          name: '4-failed-service-tasks.bpmn',
          content: readResource('4-failed-service-tasks.bpmn')
        }]
      }]),

      operation('deployment', 'create', [{
        deploymentName:  'seven-fails',
        files: [{
          name: '7-failed-service-tasks.bpmn',
          content: readResource('7-failed-service-tasks.bpmn')
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'mi-incident'
      },
      {
        key: 'fourFailingServiceTasks'
      },
      {
        key: 'sevenFailingServiceTasks'
      }])
    ),


  setup4: combine(
    operation('deployment', 'create', [{
      deploymentName: 'failed-external-task',
      files: [{
        name: 'failed-external-task.bpmn',
        content: readResource('failed-external-task.bpmn')
      }]
    }]),

    operation('process-definition', 'start', [{
      key: 'failed-external-task'
    }])
  ),

    multiTenancySetup:

        combine(
        		operation('deployment', 'create', [{
              deploymentName:  'user-tasks',
              tenantId: 'tenant1',
              files: [{
                name: 'user-tasks.bpmn',
                content: readResource('user-tasks.bpmn')
              }]
            }]),

            operation('deployment', 'create', [{
              deploymentName:  'user-tasks',
              files: [{
                name: 'user-tasks.bpmn',
                content: readResource('user-tasks.bpmn')
              }]
            }]),

            operation('process-definition', 'start', [{
              key: 'user-tasks',
              tenantId: 'tenant1',
              variables: {
                test: {
                  value: 1.5,
                  type: 'Double'
                },
                myString : {
                  value: '123 dfg',
                  type: 'String'
                },
                extraLong : {
                  value: '1234567890987654321',
                  type: 'Long'
                }
              }
            }]),

            operation('process-definition', 'start', [{
              key: 'user-tasks',
              variables: {
                test: {
                  value: 3.0,
                  type: 'Double'
                },
                myString : {
                  value: '50 dfg',
                  type: 'String'
                },
                extraLong : {
                  value: '42',
                  type: 'Long'
                }
              }
            }])
        ),

    versionTagSetup:
        combine(
          operation('deployment', 'create', [{
            deploymentName:  'user-tasks',
            files: [{
              name: 'user-tasks.bpmn',
              content: readResource('user-tasks.bpmn')
            }]
          }]),

          operation('deployment', 'create', [{
            deploymentName:  'invoice-deployment-binding',
            files: [{
              name: 'invoice-deployment-binding.bpmn',
              content: readResource('invoice-deployment-binding.bpmn')
            }]
          }])
        )
};
