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

describe('cam-common.external-task ExternalTaskErrorMessageLink', function() {
  var $scope;
  var Uri;
  var instance;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($controller) {
    $scope = {
      taskId: 'task-cool-id',
      historic: false
    };
    Uri = {
      appUri: sinon.stub().returnsArg(0)
    };

    instance = $controller('ExternalTaskErrorMessageLinkController', {
      $scope: $scope,
      Uri: Uri
    });
  }));

  it('should expose taskId and historic $scope properties', function() {
    expect(instance.taskId).to.eql($scope.taskId);
    expect(instance.historic).to.eql($scope.historic);
  });

  describe('getStacktraceUrl', function() {
    it('should create link to runtime error details for given task id', function() {
      expect(instance.getStacktraceUrl()).to.contain(
        '/external-task/' + instance.taskId + '/errorDetails'
      );
    });

    it('should create link to history error details for given task id', function() {
      instance.historic = true;

      expect(instance.getStacktraceUrl()).to.contain(
        '/history/external-task-log/' + instance.taskId + '/error-details'
      );
    });
  });
});
