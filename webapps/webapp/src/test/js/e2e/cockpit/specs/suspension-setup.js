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
    businessKey: 'Instance1'
  },
  {
    key: 'user-tasks',
    businessKey: 'Instance2'
  },
  {
    key: 'user-tasks',
    businessKey: 'myBusinessKey'
  }]
};
