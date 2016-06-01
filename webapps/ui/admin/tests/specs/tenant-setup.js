'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

var defaultTenantSetup = combine(
  operation('tenant', 'create', [{
    id:     'tenantOne',
    name:   'Tenant One'
  },
  {
    id:     'tenantTwo',
    name:   'Tenant Two'
  }])
);

function createEntities(factory) {
  var batch = [];
  for (var i = 0; i < 45; i++) {
    batch.push(factory(i));
  }
  return batch;
}

var tenantPagerSetup = combine(
  operation('tenant', 'create', createEntities(function(idx) {
    return {
      id:   'tenant' + idx,
      name: 'Tenant ' + idx
    };
  }))
);

var userPagerSetup = combine(

  operation('tenant', 'create',  [{
    id:   'tenantOne',
    name: 'Tenant One'
  }]),

  operation('user', 'create', createEntities(function(idx) {
    return {
      id:         'user' + idx,
      password:   'cam123',
      firstName:  'abc',
      lastName:   'def'
    };
  })),

  operation('tenant', 'createUserMember', createEntities(function(idx) {
    return {
      id:     'tenantOne',
      userId: 'user' + idx
    };
  }))
);

var groupPagerSetup = combine(

  operation('tenant', 'create', [{
    id:   'tenantOne',
    name: 'Tenant One'
  }]),

  operation('group', 'create', createEntities(function(idx) {
    return {
      id:   'group' + idx,
      name: 'group' + idx,
      type: 'GROUP' + idx
    };
  })),

  operation('tenant', 'createGroupMember', createEntities(function(idx) {
    return {
      id:       'tenantOne',
      groupId:  'group' + idx
    };
  }))
);

var pagingSetup = combine(

  operation('tenant', 'create', createEntities(function(idx) {
    return {
      id:   'tenant' + idx,
      name: 'Tenant ' + idx
    };
  })),

  operation('group', 'create', [{
    id : 'group',
    name : 'group',
    type : 'GROUP'
  }]),

  operation('user', 'create', [{
      id:         'user',
      password:   'cam123',
      firstName:  'abc',
      lastName:   'def'
  }])
);

module.exports = {

  setup1: defaultTenantSetup,

  setup2: tenantPagerSetup,

  setup3: groupPagerSetup,

  setup4: userPagerSetup,

  setup5: pagingSetup

};
