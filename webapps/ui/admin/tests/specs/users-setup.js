'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

var fragment1 = combine(
  operation('user', 'create', [{
    id:         'john',
    password:   'MobyDick',
    firstName:  'John',
    lastName:   'Bonham',
    email:      'john.bonham@led-zeppelin.com'
  },
  {
    id:         'keith',
    password:   'abcdefg',
    firstName:  'Keith',
    lastName:   'Moon',
    email:      'keith.moon@the-who.com'
  },
  {
    id:         'ringo',
    password:   'cam123',
    firstName:  'Ringo',
    lastName:   'Starr',
    email:      'ringo.starr@the-beatles.com'
  }]),

  operation('group', 'create', [{
    id:   "accounting",
    name: "Accounting",
    type: "WORKFLOW"
  },
  {
    id:   "sales",
    name: "Sales",
    type: "WORKFLOW"
  }]),

  operation('tenant', 'create', [{
    id: 'tenantOne',
    name: 'Tenant One'
  },
    {
      id: 'tenantTwo',
      name: 'Tenant Two'
    }]),

  operation('group', 'createMember', [{
    id:     "accounting",
    userId: "ringo"
  }])
)

var fragment2 = combine(
  operation('group', 'create', [{
    id: '/göäüp_name',
      name: '/üöäüöäü/',
      type: 'testgroup/üäö'
    },
    {
      id: '\\göäüp_name',
      name: '\\üöäüöäü\\',
      type: 'testgroup\\üäö'
    }])
)

var userBatch = [];
for (var i = 0; i < 45; i++) {
  userBatch.push({
    id:         'user' + i,
    password:   'cam123',
    firstName:  'abc',
    lastName:   'def'
  });
}

var fragment3 = combine(
  operation('user', 'create', userBatch)
)

module.exports = {
  setup1: fragment1,
  setup2: combine(fragment1, fragment2),
  setup3: fragment3
};
