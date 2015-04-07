'use strict';

var fs = require('fs');

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'All',
    query: {},
    properties: {
      variables: [{
        name: 'testVar',
        label: 'varTest'
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
      permissions: ['ALL'],
      userId: 'test',
      groupId: null,
      resourceType: 0,
      resourceId: 'tasklist'
    },
    {
      type : 1,
      permissions: ['ALL'],
      userId: 'test',
      groupId: null,
      resourceType: 5,
      resourceId: '*'
    },
    {
      type : 1,
      permissions: ['READ'],
      userId: 'test',
      groupId: null,
      resourceType: 7,
      resourceId: '*'
    }]
};

ops.task = {
  create: [
    {
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
    },
  ],

  localVariable: [
    {
      id: '1',
      varId: 'testVar',
      value: 42,
      type: 'Integer'
    }
  ]
};

/*ops.deployment = {
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
};*/

