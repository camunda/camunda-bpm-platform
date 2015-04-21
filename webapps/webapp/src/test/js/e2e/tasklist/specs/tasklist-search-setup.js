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
      id: '1',
      varId: 'testString',
      value: '4711',
      type: 'String'
    },
    {
      id: '2',
      varId: 'testString',
      value: 'asdfhans dampf',
      type: 'String'
    }
  ]
};
