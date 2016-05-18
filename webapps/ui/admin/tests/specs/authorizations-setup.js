'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
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
    id:         'ringo',
    password:   'cam123',
    firstName:  'Ringo',
    lastName:   'Starr',
    email:      'ringo.starr@the-beatles.com'
  }])
);


var authBatch2 = [];
for (var i = 0; i < 10; i++) {
  authBatch2.push({
    type: 1, resourceType: 7, resourceId: '1', permissions: ['CREATE'], userId: 'a' + i
  });
}

var fragment2 = combine(
  operation('authorization', 'create', authBatch2)
);


var authBatch3 = [];
for (var i = 0; i < 45; i++) {
  authBatch3.push({
    type: 1, resourceType: 7, resourceId: '1', permissions: ['CREATE'], userId: 'xxxxxxxxxxx' + i
  });
}

var fragment3 = combine(
  operation('authorization', 'create', authBatch3)
);


module.exports = {

  setup1: fragment1,
  setup2: fragment2,
  setup3: fragment3
};
