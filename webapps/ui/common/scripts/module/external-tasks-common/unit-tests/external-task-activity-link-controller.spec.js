'use strict';

var expect = require('chai').expect;
var sinon = require('sinon');
var angular = require('angular');
var testModule = require('./module');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common.external-tasks ExternalTaskActivityLinkController', function() {
  var $scope;
  var params;
  var search;
  var path;
  var $location;
  var instance;

  beforeEach(module(testModule.name));

  beforeEach(inject(function($rootScope, $controller) {
    $scope = $rootScope.$new();
    $scope.activityId = 'act-id';
    $scope.bpmnElements = {
      'act-id': {
        name: 'd'
      }
    };

    params = {
      a: 1
    };
    search = sinon.stub().returns(params);

    path = '/some/path';
    $location = {
      path: sinon.stub().returns(path)
    };

    instance = $controller('ExternalTaskActivityLinkController', {
      $scope: $scope,
      search: search,
      $location: $location
    });
  }));

  it('should expose activityId and bpmnElements', function() {
    expect(instance.activityId).to.eql($scope.activityId);
    expect(instance.bpmnElements).to.eql($scope.bpmnElements);
  });

  it('should get current path', function() {
    expect(instance.path).to.eql(path);
  });

  describe('getLink', function() {
    var link;

    beforeEach(function() {
      link = instance.getLink();
    });

    it('should preserve other params', function() {
      expect(link).to.contain('a=1');
    });

    it('should add activityId', function() {
      expect(link).to.contain('activityIds=' + $scope.activityId);
    });

    it('should start with hash fallowed by path', function() {
      expect(link.substr(0, path.length +  1)).to.eql('#' + path);
    });

    it('should modify search query when searchQueryType is set', function() {
      var params = {
        searchQuery: JSON.stringify([
          {
            type: 'activityIdIn',
            operator: 'eq',
            value: 'ab1'
          }
        ])
      };

      search.returns(params);
      instance.searchQueryType = 'activityIdIn';

      var link = instance.getLink();

      expect(link).to.contain(
        'searchQuery=' +
        encodeURIComponent(
          JSON.stringify([
            {
              type: 'activityIdIn',
              operator: 'eq',
              value: $scope.activityId
            }
          ])
        )
      );
    });
  });

  describe('getActivityName', function() {
    it('should return name of known activity', function() {
      expect(instance.getActivityName()).to.eql('d');
    });

    it('should return activity id when activity is unknown', function() {
      instance.activityId = 'some-other-act';

      expect(instance.getActivityName()).to.eql('some-other-act');
    });
  });
});
