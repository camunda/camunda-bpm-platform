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

    instance = $controller('ProcessInstanceRuntimeTabController', {
      $scope: $scope,
      externalTasks: externalTasks
    });
  }));

  it('should expose processInstance, processData on instance', function() {
    expect(instance.processInstance).to.equal($scope.processInstance);
    expect(instance.processData).to.equal($scope.processData);
  });

  describe('onLoad', function() {
    var pages;
    var params;
    var promise;

    beforeEach(() => {
      pages = 'pages';
      params = 'params';

      promise = instance.onLoad(pages, params);
    });

    it('should call externalTasks.getActiveExternalTasksForProcess with process instance id, pages and params', function() {
      expect(externalTasks.getActiveExternalTasksForProcess.calledWith(
        $scope.processInstance.id,
        pages,
        params
      )).to.eql(true);
    });

    it('should set tasks on instance', function(done) {
      promise.then(function() {
        expect(instance.tasks).to.eql(tasks);

        done();
      });

      $rootScope.$digest();
    });
  });
});
