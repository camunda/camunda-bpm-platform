'use strict';

var fs = require('fs');

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'All',
    query: {},
    properties: {
      priority: '100',
      variables: [{
        name: 'testVar',
        label: 'varTest'
      }],
      showUndefinedVariable: true
    },
    resourceType: 'Task'
  },
  {
    name:         'My Tasks',
    query: {
      assigneeExpression: '${ currentUser() }'
    },
    properties: {
      priority: '10',
      variables: [{
        name: 'testVar',
        label: 'test Variable'
      },
      {
        name: 'testDate',
        label: 'test Date'
      },
      {
        name: 'testString',
        label: 'test String'
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
    }]
};

ops.task = {
  create: [
    {
      id: '1',
      name: 'Task 1'
    },
    {
      id: '2',
      name: 'Task 2'
    },
    {
      id: '3',
      name: 'Task 3'
    }
  ],

  assignee: [
    {
      taskId: '1',
      userId: 'test'
    },
    {
      taskId: '2',
      userId: 'test'
    }
  ],

  localVariable: [
    {
      id: '1',
      varId: 'testVar',
      value: 42,
      type: 'Integer'
    },
    {
      id: '2',
      varId: 'testVar',
      value: 48,
      type: 'Integer'
    },
    {
      id: '3',
      varId: 'testVar',
      value: 1000,
      type: 'Integer'
    },
    {
      id: '1',
      varId: 'testDate',
      value: '2013-11-30T10:03:01',
      type: 'Date'
    },
    {
      id: '2',
      varId: 'testDate',
      value: '2013-11-30T10:03:00',
      type: 'Date'
    },
    {
      id: '2',
      varId: 'testString',
      value: 'asdfhans dampf',
      type: 'String'
    }
  ]
};
