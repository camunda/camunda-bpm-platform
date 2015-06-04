'use strict';

var fs = require('fs');

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'All',
    query: {},
    properties: {
      priority: 100,
      description:  'Show all Tasks'
    },
    resourceType: 'Task'
  },
  {
    name:         'My Tasks',
    query: {
      assigneeExpression: '${ currentUser() }'
    },
    properties: {
      priority: 10
    },
    resourceType: 'Task'
  },
  {
    name:         'Test Filter',
    query: {},
    properties: {
      priority: 110
    },
    resourceType: 'Task'
  }]
};

ops.user = {
  create: [{
    id: 'test',
    firstName: 'Montgomery',
    lastName: 'QA',
    password: 'test'
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
  ]
};
