'use strict';

var expect = require('chai').expect;
var sinon = require('sinon');
var angular = require('angular');
var testModule = require('./module');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common.external-tasks ExternalTasksTabController', function() {
  var $rootScope;
  var $q;
  var $scope;
  var processData;
  var count;
  var tasks;
  var instance;
  var onLoad;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($controller, _$rootScope_, _$q_) {
    $rootScope = _$rootScope_;
    $q = _$q_;

    $scope = $rootScope.$new();
    processData = {
      observe: sinon.spy(),
      newChild: sinon.stub().returnsThis()
    };
    $scope.processData = processData;
    $scope.processInstance = {
      id: 'p-instance-id-01'
    };

    onLoad = sinon.stub().returns($q.when({
      count,
      list: tasks
    }));
    $scope.onLoad = onLoad;

    count = 20;
    tasks = ['a'];

    instance = $controller('ExternalTasksTabController', {
      $scope: $scope
    });
  }));

  it('should create new instance of data depend for $scope', function() {
    expect(processData.newChild.calledWith($scope)).to.eql(true);
  });

  it('should observe filter', function() {
    expect(processData.observe.calledWith('filter')).to.eql(true);
  });

  describe('onFilterChanged', function() {
    var filter;

    beforeEach(function() {
      filter = 'filter';
      instance.isFilterChanged = sinon.stub();
      instance.loadTasks = sinon.spy();
    });

    it('should check if filter has changed', function() {
      instance.onFilterChanged(filter);

      expect(instance.isFilterChanged.calledWith(filter)).to.eql(true);
    });

    it('should not set filter on instance if filter has not changed', function() {
      instance.isFilterChanged.returns(false);

      instance.onFilterChanged(filter);

      expect(instance.filter).to.eql(undefined);
    });

    describe('when filter has truly changed', function() {
      beforeEach(function() {
        instance.isFilterChanged.returns(true);
      });

      it('should set filter on instance if filter have changed', function() {
        instance.onFilterChanged(filter);

        expect(instance.filter).to.eql(filter);
      });

      it('should not load tasks if pagination is not set', function() {
        instance.onFilterChanged(filter);

        expect(instance.loadTasks.called).not.to.eql(true);
      });

      it('should load tasks if pagination is set', function() {
        instance.pages = 'whatever';
        instance.onFilterChanged(filter);

        expect(instance.loadTasks.calledOnce).to.eql(true);
      });
    });
  });

  describe('isFilterChanged', function() {
    var originalFilter;
    var newFilter;

    beforeEach(function() {
      originalFilter = {
        activityIds: ['id-01']
      };
      newFilter = {
        activityIds: ['id-02']
      };

      instance.filter = originalFilter;
    });

    it('should return true when filter activity has changed', function() {
      expect(instance.isFilterChanged(newFilter)).to.eql(true);
    });

    it('should return false when filter activity has not changed', function() {
      expect(instance.isFilterChanged(originalFilter)).to.eql(false);
    });

    it('should return true when filter is not defined on instance', function() {
      delete instance.filter;

      expect(instance.isFilterChanged(originalFilter)).to.eql(true);
    });
  });

  describe('onPaginatioChange', function() {
    var pages;

    beforeEach(function() {
      pages = 'pages';
      instance.loadTasks = sinon.spy();
    });

    it('should set pages on instance', function() {
      instance.onPaginationChange(pages);

      expect(instance.pages).to.eql(pages);
    });

    it('should load tasks if filter is set', function() {
      instance.filter = 'some-filter';
      instance.onPaginationChange(pages);

      expect(instance.loadTasks.calledOnce).to.eql(true);
    });
  });

  describe('loadTasks', function() {
    var pages;
    var activityIds;

    beforeEach(function() {
      pages = 'pages';
      instance.pages = pages;

      activityIds = ['a'];
      instance.filter = {
        activityIds: activityIds
      };

      instance.loadTasks();
    });

    it('should set loading state to LOADING', function() {
      expect(instance.loadingState).to.eql('LOADING');
    });

    it('should set loading state to loaded', function() {
      $rootScope.$digest();

      expect(instance.loadingState).to.eql('LOADED');
    });

    it('should set total on instance', function() {
      $rootScope.$digest();

      expect(instance.total).to.eql(count);
    });

    it('should set loading state to EMPTY when tasks are not returned', function() {
      onLoad.returns(
        $q.when({
          count: count
        })
      );
      instance.loadTasks();
      $rootScope.$digest();

      expect(instance.loadingState).to.eql('EMPTY');
    });

    it('should pass pages and activityIds to onLoad', function() {
      expect(onLoad.calledWith({
        pages: pages,
        activityIds: activityIds
      })).to.eql(true);
    });
  });
});
