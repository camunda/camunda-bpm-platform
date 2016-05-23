'use strict';

var factory = require('../../../common/tests/setup-factory.js'),
    readResource = factory.readResource,
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1: combine(
        operation('filter', 'create', [{
          name: 'My Tasks',
          query: {
            assigneeExpression: '${ currentUser() }'
          },
          properties: {
            priority: 10
          },
          resourceType: 'Task'
        }]),

        operation('tenant', 'create',  [{
          id:   'tenantOne',
          name: 'Tenant One'
        }]),

        operation('tenant', 'createUserMember', [
          {
            id: 'tenantOne',
            userId: 'admin'
        }])
      ),

  setup2: combine(
    operation('filter', 'create', [{
      name: 'My Tasks',
      query: {
        assigneeExpression: '${ currentUser() }'
      },
      properties: {
        priority: 10
      },
      resourceType: 'Task'
    }]),

    operation('tenant', 'create',  [{
      id:   'tenantOne',
      name: 'Tenant One'
    },{
        id:   'tenantTwo',
        name: 'Tenant Two'
    }]),

    operation('tenant', 'createUserMember', [
      {
        id: 'tenantOne',
        userId: 'admin'
    },{
        id: 'tenantTwo',
        userId: 'admin'
    }])
  )
};
