/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'All',
    query: {},
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

ops.task = {
  create: [
    {
      id: '1',
      name: 'Task 1'
    },
    {
      id: '2',
      name: 'Task 2'
    }
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
