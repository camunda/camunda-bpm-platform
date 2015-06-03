'use strict';

var fs = require('fs');

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'All',
    query: {},
    resourceType: 'Task',
    properties: {
      variables: [
        {name: 'testVar', label: 'Test Variable'},
        {name: 'testString', label:'String Variable'}
      ]
    }
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
    }
  ]

};
