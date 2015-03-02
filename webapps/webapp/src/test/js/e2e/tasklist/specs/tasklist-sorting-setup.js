'use strict';

var fs = require('fs');

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'All',
    query: {},
    properties: {
      variables: [{
        name: "testVar",
        label: "varTest"
      }],
      showUndefinedVariable: true
    },
    resourceType: 'Task'
  }]
};

ops.user = {
  create: [{
    'id': 'test',
    'firstName': 'test',
    'lastName': 'test',
    'password': 'test'
  }]
};

ops.authorization = {
  create: [
    {
      type : 1,
      permissions: ["ALL"],
      userId: 'test',
      groupId: null,
      resourceType: 0,
      resourceId: 'tasklist'
    },
    {
      type : 1,
      permissions: ["ALL"],
      userId: 'test',
      groupId: null,
      resourceType: 5,
      resourceId: '*'
    }]
};

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
      testVar : {
        value: 1,
        type: 'Integer'
      }
    }
  },
  {
    key: 'user-tasks',
    businessKey: 'Instance1',
    variables: {
      testVar : {
        value: 2,
        type: 'Integer'
      }
    }
  },
  {
    key: 'user-tasks',
    businessKey: 'Instance1',
    variables: {
      testVar : {
        value: 3,
        type: 'Integer'
      }
    }
  }]
};

