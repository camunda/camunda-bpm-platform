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

var expect = require('chai').expect;
var sinon = require('sinon');
var angular = require('angular');
var testModule = require('./module');
var deleteDeploymentController = require('../cam-cockpit-delete-deployment-modal-ctrl');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cockpit.repository.deployment.action deleteDeployment modal controller', function() {
  var $controller;
  var $rootScope;
  var $q;
  var $scope;
  var camAPI;
  var Notifications;
  var deploymentData;
  var deployment;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($injector) {
    $controller = $injector.get('$controller');
    $rootScope = $injector.get('$rootScope');
    $q = $injector.get('$q');

    $scope = $rootScope.$new();

    camAPI = {
      resource: sinon.stub().returnsThis(),
      count: sinon.spy(),
      delete: sinon.stub()
    };

    Notifications = {
      addError: sinon.spy()
    };

    deploymentData = {
      newChild: sinon.stub().returnsThis(),
      provide: sinon.stub().callsArg(1),
      observe: sinon.stub().callsArg(1)
    };

    deployment = {
      id: 'deployment-id'
    };

    $controller(deleteDeploymentController, {
      $scope: $scope,
      $q: $q,
      camAPI: camAPI,
      Notifications: Notifications,
      deploymentData: deploymentData,
      deployment: deployment
    });
  }));

  // For now only new features added as part of CAM-7534
  // will be tested for lack of time. Rest is assumed to be tested as part of
  // some broken e2e tests (at time of writing this comment that is 20.04.2017).
  // There is possiblity open for writing more unit tests if needed though.
  it('should have option with skipIoMappings flag set to true', () => {
    expect($scope.options.skipIoMappings).to.eql(true);
  });

  describe('deleteDeployment', () => {
    it('should be existing method on scope', () => {
      expect(typeof $scope.deleteDeployment).to.eql('function');
    });

    it('should perform delete deployment request', () => {
      $scope.deleteDeployment();

      expect(
        camAPI.delete.calledWith(deployment.id, {
          cascade: false,
          skipCustomListeners: true,
          skipIoMappings: true
        })
      ).to.eql(true);
    });

    it('should perform delete deployment request with skipIoMappings set to false', () => {
      $scope.options.skipIoMappings = false;

      $scope.deleteDeployment();

      expect(
        camAPI.delete.calledWith(deployment.id, {
          cascade: false,
          skipCustomListeners: true,
          skipIoMappings: false
        })
      ).to.eql(true);
    });
  });
});
