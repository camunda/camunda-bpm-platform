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
    },
    {
      id:         'mm',
      password:   'SweetDreams',
      firstName:  'uʎlᴉɹɐW',
      lastName:   'uosuɐW',
      email:      'm.m@rock.com'
    }]),

    operation('authorization', 'create', [{
      type : 1,
      permissions: ['ALL'],
      userId: 'mm',
      groupId: null,
      resourceType: 0,
      resourceId: 'admin'
    }])
  )
};
