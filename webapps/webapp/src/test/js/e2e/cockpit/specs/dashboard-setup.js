'use strict';

var fs = require('fs');

var ops = module.exports = {};

ops.deployment = {
  create: [{
    deploymentName:  'failing-process',
    files:           [{
      name: 'failing-process.bpmn',
      content: fs.readFileSync(__dirname + '/../../resources/failing-process.bpmn').toString()
    }]
  }]
};
/*ops['process-definition'] = {
  start: [{
    key: 'failing-process',
    businessKey: 'Instance1',
    variables: {
      test : {
        value: 1,
        type: 'Integer'
      }
    }
  }]
};*/
