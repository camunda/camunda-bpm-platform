'use strict';

var fs = require('fs');

var ops = module.exports = {};

ops.deployment = {
  create: [{
    deploymentName:  'suspension-process',
    files:           [{
      name: 'suspension-process.bpmn',
      content: fs.readFileSync(__dirname + '/../../resources/suspension-process.bpmn').toString()
    }]
  }]
};
ops['process-definition'] = {
  start: [{
    key: 'suspension-process',
    businessKey: 'Instance1'
  },
  {
    key: 'suspension-process',
    businessKey: 'Instance2'
  },
  {
    key: 'suspension-process',
    businessKey: 'myBusinessKey'
  }]
};
