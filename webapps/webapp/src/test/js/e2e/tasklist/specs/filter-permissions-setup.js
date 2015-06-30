'use strict';

var fs = require('fs');

var ops = module.exports = {};
ops.filter = {
  create: [{
    name:         'Empty Filter',
    query: {},
    properties: {
      priority: 5,
      description:  'Filter without variable definitions',

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
  },
  {
    id: 'juri',
    firstName: 'Juri',
    lastName: 'Gagarin',
    password: 'juri'
  }]
};

ops.group = {
  create: [{
    id:   "marketing",
    name: "Marketing",
    type: "WORKFLOW"
  }],
  createMember: [{
    id:     "marketing",
    userId: "juri"
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
      userId: 'juri',
      groupId: null,
      resourceType: 0,
      resourceId: 'tasklist'
    }]
};
