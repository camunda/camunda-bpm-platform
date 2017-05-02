'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('camunda-commons-ui/vendor/angular');
var camCommon = require('../index');
require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common getDeploymentUrl', function() {
  var searchParams;
  var $location;
  var url;
  var routeUtil;
  var getDeploymentUrl;
  var deployment;
  var resource;

  beforeEach(module(camCommon.name));

  beforeEach(module(function($provide) {
    searchParams = {
      deploymentsSortBy: 'b',
      deploymentsSortOrder: 'asc'
    };
    $location = {
      search: sinon.stub().returns(searchParams)
    };
    $provide.value('$location', $location);

    url = 'some-url';
    routeUtil = {
      redirectTo: sinon.stub().returns(url)
    };
    $provide.value('routeUtil', routeUtil);

    deployment = {
      id: 'dep-id'
    };
    resource = {
      name: 'resource-name'
    };
  }));

  beforeEach(inject(function($injector) {
    getDeploymentUrl = $injector.get('getDeploymentUrl');
  }));

  it('should return url', function() {
    expect(
      getDeploymentUrl(deployment, resource)
    ).to.eql(url);
  });

  it('should create url with correct searches', () => {
    getDeploymentUrl(deployment, resource);

    expect(routeUtil.redirectTo.calledWith(
      '#/repository',
      {
        deployment: deployment.id,
        deploymentsQuery: JSON.stringify([{
          type     : 'id',
          operator : 'eq',
          value    : deployment.id
        }]),
        deploymentsSortBy: searchParams.deploymentsSortBy,
        deploymentsSortOrder: searchParams.deploymentsSortOrder,
        resourceName: resource.name
      }
    )).to.eql(true);
  });
});
