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
      taskId: 'task-cool-id'
    };
    Uri = {
      appUri: sinon.stub().returnsArg(0)
    };

    instance = $controller('ExternalTaskErrorMessageLinkController', {
      $scope: $scope,
      Uri: Uri
    });
  }));

  it('should expose taskId', function() {
    expect(instance.taskId).to.eql($scope.taskId);
  });

  describe('getStacktraceUrl', function() {
    it('should create link to error details for given task id', function() {
      expect(
        instance.getStacktraceUrl()
      ).to.contain('/external-task/' + instance.taskId + '/errorDetails');
    });
  });
});
