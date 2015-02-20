'use strict';

var ops = module.exports = {};
ops.user = {
  create: [{
    id:         'john',
    password:   'MobyDick',
    firstName:  'John',
    lastName:   'Bonham',
    email:      'john.bonham@led-zeppelin.com'
  },
  {
    id:         'ringo',
    password:   'cam123',
    firstName:  'Ringo',
    lastName:   'Starr',
    email:      'ringo.starr@the-beatles.com'
  }]
};
ops.group = {
  create: [{
    id:   "accounting",
    name: "Accounting",
    type: "WORKFLOW"
  },
  {
    id:   "sales",
    name: "Sales",
    type: "WORKFLOW"    
  },
  {
    id:   "marketing",
    name: "Marketing",
    type: "WORKFLOW"    
  }],
  createMember: [{
    id:     "marketing",
    userId: "john"
  },
  {
    id:     "accounting",
    userId: "john"
  },  
  {
    id:     "sales",
    userId: "john"
  }]
};