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
var cancelInstanceDialog = require('../cancelProcessInstanceDialog');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cockpit.plugin.process-instance-runtime-tab ProcessInstanceRuntimeTabController', function() {
  var $controller;
  var $rootScope;
  var $scope;
  var $location;
  var Notifications;
  var ProcessInstanceResource;
  var $modalInstance;
  var processInstance;
  var processData;
  var Views;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($injector) {
    $controller = $injector.get('$controller');
    $rootScope = $injector.get('$rootScope');

    $scope = $rootScope.$new();

    $location = {
      path: sinon.spy(),
      search: sinon.spy(),
      replace: sinon.spy()
    };

    Notifications = {
      addMessage: sinon.spy(),
      addError: sinon.spy()
    };

    ProcessInstanceResource = {
      count: sinon.stub().returns({
        $promise: 'count'
      }),
      query: sinon.stub().returns({
        $promise: 'query'
      })
    };

    $modalInstance = {
      close: sinon.spy()
    };

    processInstance = {
      $delete: sinon.spy(),
      id: 'some-id'
    };

    processData = {
      newChild: sinon.stub().returnsThis(),
      provide: sinon.stub().callsArg(1),
      observe: sinon.spy()
    };

    Views = {
      getProvider: sinon.stub().returnsThis()
    };

    $controller(cancelInstanceDialog, {
      $scope: $scope,
      $location: $location,
      Notifications: Notifications,
      ProcessInstanceResource: ProcessInstanceResource,
      $modalInstance: $modalInstance,
      processInstance: processInstance,
      processData: processData,
      Views: Views
    });
  }));

  it('should close modal when route changes', function() {
    $scope.$broadcast('$routeChangeStart');

    expect($modalInstance.close.calledOnce).to.eql(true);
  });

  it('should create new data provider on $scope', function() {
    expect(processData.newChild.calledWith($scope)).to.eql(true);
  });

  it('should create default request options', function() {
    expect($scope.options).to.exist;
  });

  describe('subProcessInstances', function() {
    it('should provide sub process instances', function() {
      expect(processData.provide.calledWith('subProcessInstances')).to.eql(
        true
      );
    });

    it('should provide sub process instances count', function() {
      expect(processData.provide.calledWith('subProcessInstancesCount')).to.eql(
        true
      );
    });

    it('should query and count process instances', function() {
      var params = ProcessInstanceResource.query.lastCall.args[1];

      expect(params).to.eql({
        superProcessInstance: processInstance.id
      });

      expect(
        ProcessInstanceResource.count.calledWith({
          superProcessInstance: processInstance.id
        })
      ).to.eql(true);
    });

    it('should observe sub processes', function() {
      expect(
        processData.observe.calledWith([
          'subProcessInstancesCount',
          'subProcessInstances'
        ])
      ).to.eql(true);
    });
  });

  describe('cancelProcessInstance', function() {
    it('should delete process instance with options', function() {
      $scope.cancelProcessInstance();

      expect(processInstance.$delete.calledWith($scope.options)).to.eql(true);
    });

    it('should set status to success and notifiction when delete request succeded', function() {
      $scope.cancelProcessInstance();

      var successCallback = processInstance.$delete.lastCall.args[1];

      successCallback();

      expect($scope.status).to.eql('cancellationSuccess');
      expect(Notifications.addMessage.calledOnce).to.eql(true);
    });

    it('should set status to fail and notifiction when delete request failed', function() {
      var err = {
        data: {
          message: 'whatever'
        }
      };

      $scope.cancelProcessInstance();

      var failCallback = processInstance.$delete.lastCall.args[2];

      failCallback(err);

      expect($scope.status).to.eql('cancellationFailed');
      expect(
        Notifications.addError.calledWith({
          status: 'Failed',
          message:
            'The cancellation of the process instance failed. ' +
            err.data.message,
          exclusive: ['type']
        })
      ).to.eql(true);
    });
  });
});
