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
      expect(
        instance.getStacktraceUrl()
      ).to.contain('/external-task/' + instance.taskId + '/errorDetails');
    });

    it('should create link to history error details for given task id', function() {
      instance.historic = true;

      expect(
        instance.getStacktraceUrl()
      ).to.contain('/history/external-task-log/' + instance.taskId + '/error-details');
    });
  });
});
