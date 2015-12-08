'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;


module.exports = {

  setup1:

    combine(
      operation('deployment', 'create', [{
        deploymentName:  'user-tasks',
        files: [{
          name: 'user-tasks.bpmn',
          content: fs.readFileSync(__dirname + '/../../resources/user-tasks.bpmn').toString()
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
          content: fs.readFileSync(__dirname + '/../../resources/mi-incident.bpmn').toString()
        }]
      }]),

      operation('process-definition', 'start', [{
        key: 'mi-incident',
        businessKey: 'MultiInstance'
      }])

)

};
