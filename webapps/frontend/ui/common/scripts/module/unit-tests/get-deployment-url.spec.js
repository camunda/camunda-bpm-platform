/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('../../../../../camunda-commons-ui/vendor/angular');
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

  beforeEach(
    module(function($provide) {
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
    })
  );

  beforeEach(inject(function($injector) {
    getDeploymentUrl = $injector.get('getDeploymentUrl');
  }));

  it('should return url', function() {
    expect(getDeploymentUrl(deployment, resource)).to.eql(url);
  });

  it('should create url with correct searches', () => {
    getDeploymentUrl(deployment, resource);

    expect(
      routeUtil.redirectTo.calledWith('#/repository', {
        deployment: deployment.id,
        deploymentsQuery: JSON.stringify([
          {
            type: 'id',
            operator: 'eq',
            value: deployment.id
          }
        ]),
        deploymentsSortBy: searchParams.deploymentsSortBy,
        deploymentsSortOrder: searchParams.deploymentsSortOrder,
        resourceName: resource.name
      })
    ).to.eql(true);
  });
});
