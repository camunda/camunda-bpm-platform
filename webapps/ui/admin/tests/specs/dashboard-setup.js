'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {
  setup1: combine(
    operation('user', 'create', [{
      id:         'john',
      password:   'MobyDick',
      firstName:  'John',
      lastName:   'Bonham',
      email:      'john.bonham@led-zeppelin.com'
    }])
  )
};
