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

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cockpit.plugin.process-instance-runtime-tab ProcessInstanceRuntimeTabController', function() {
  var $rootScope;
  var $q;
  var $scope;
  var tasks;
  var externalTasks;
  var observeBpmnElements;
  var instance;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($controller, _$rootScope_, _$q_) {
    $rootScope = _$rootScope_;
    $q = _$q_;

    $scope = $rootScope.$new();
    $scope.processInstance = {
      id: 'process-instance'
    };
    $scope.processData = 'process-data';

    tasks = 'tasks';
    externalTasks = {
      getActiveExternalTasksForProcess: sinon.stub().returns(
        $q.when({
          list: tasks
        })
      )
    };

    observeBpmnElements = sinon.spy();

    instance = $controller('ProcessInstanceRuntimeTabController', {
      $scope: $scope,
      externalTasks: externalTasks,
      observeBpmnElements: observeBpmnElements
    });
  }));

  it('should observe bpmn elements', function() {
    expect(observeBpmnElements.calledWith($scope, instance)).to.eql(true);
  });

  it('should expose processInstance, processData on instance', function() {
    expect(instance.processInstance).to.equal($scope.processInstance);
    expect(instance.processData).to.equal($scope.processData);
  });

  describe('onLoad', function() {
    var pages;
    var activityIds;
    var promise;

    beforeEach(() => {
      pages = 'pages';
      activityIds = ['params', 'd1'];

      promise = instance.onLoad(pages, activityIds);
    });

    it('should call externalTasks.getActiveExternalTasksForProcess with process instance id, pages and activityId', function() {
      expect(
        externalTasks.getActiveExternalTasksForProcess.calledWith(
          $scope.processInstance.id,
          pages,
          {
            activityIdIn: activityIds
          }
        )
      ).to.eql(true);
    });

    it('should set tasks on instance', function(done) {
      promise.then(function() {
        expect(instance.tasks).to.eql(tasks);

        done();
      });

      $rootScope.$digest();
    });

    it('should call externalTasks.getActiveExternalTasksForProcess with process instance id, pages when activityIds is empty', function() {
      externalTasks.getActiveExternalTasksForProcess.reset();

      instance.onLoad(pages, []);

      expect(
        externalTasks.getActiveExternalTasksForProcess.calledWith(
          $scope.processInstance.id,
          pages,
          {}
        )
      ).to.eql(true);
    });
  });
});
