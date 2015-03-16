'use strict';

var fs = require('fs');

var ops = module.exports = {};

ops.deployment = {
  create: [{
    deploymentName:  'user-tasks',
    files:           [{
      name: 'user-tasks.bpmn',
      content: fs.readFileSync(__dirname + '/../../resources/user-tasks.bpmn').toString()
    }]
  }]
};
ops['process-definition'] = {
  start: [{
    key: 'user-tasks',
    businessKey: 'Instance1',
    variables: {
      test : {
        value: 1.5,
        type: 'Double'
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
      }
    }
  }]
};
