'use strict';

var expect = require('chai').expect;
var sinon = require('sinon');
var angular = require('angular');
var testModule = require('./module');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cockpit.plugin.process-instance-runtime-tab.external-tasks externalTasks', function() {
  var externalTasksResource;
  var externalTasks;
  var $rootScope;
  var $q;
  var count;
  var list;

  beforeEach(module(testModule.name));

  beforeEach(module(function($provide) {
    externalTasksResource = {
      count: sinon.stub(),
      list: sinon.stub()
    };

    $provide.value('camAPI', {
      resource: sinon.stub().returns(externalTasksResource)
    });
  }));

  beforeEach(inject(function(_$rootScope_, _$q_, _externalTasks_) {
    $rootScope = _$rootScope_;
    $q = _$q_;
    externalTasks = _externalTasks_;

    count = 1;
    list = ['a'];

    externalTasksResource.count.returns(
      $q.when({
        count: count
      })
    );

    externalTasksResource.list.returns(
      $q.when(list)
    );
  }));

  describe('getActiveExternalTasksForProcess', function() {
    var processId;
    var pages;
    var otherParams;
    var promise;
    var countQuery;
    var listQuery;

    beforeEach(function() {
      processId = 'abcd-process-id-01';
      pages = {
        size: 2,
        current: 1
      };
      otherParams = {
        other: 1
      };

      promise = externalTasks.getActiveExternalTasksForProcess(
        processId,
        pages,
        otherParams
      );

      $rootScope.$digest();

      countQuery = externalTasksResource.count.firstCall.args[0];
      listQuery = externalTasksResource.list.firstCall.args[0];
    });

    it('should return response with count and tasks', function(done) {
      promise.then(function(response) {
        expect(response.count).to.be.eql(count);
        expect(response.list).to.be.eql(list);

        done();
      });

      $rootScope.$digest();
    });

    it('should execute count with query with processId', function() {
      expect(countQuery).to.contain.keys('processInstanceId');
      expect(countQuery.processInstanceId).to.be.eql(processId);
    });

    it('should execute count with query with other', function() {
      expect(countQuery).to.contain.keys('other');
      expect(countQuery.other).to.be.eql(otherParams.other);
    });

    it('should execute count with query without pagination', function() {
      expect(countQuery).not.to.contain.keys('firstResult');
      expect(countQuery).not.to.contain.keys('maxResults');
    });

    it('should execute list with query with processId', function() {
      expect(listQuery).to.contain.keys('processInstanceId');
      expect(listQuery.processInstanceId).to.be.eql(processId);
    });

    it('should execute list with query with other', function() {
      expect(listQuery).to.contain.keys('other');
      expect(listQuery.other).to.be.eql(otherParams.other);
    });
  });
});
