'use strict';

var chai = require('chai');
var sinon = require('sinon');
var expect = chai.expect;
var angular = require('angular');
var testModule = require('./module');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cockpit.plugin.decisionList.views.dashboard decisionList service', function() {
  var decisionList;
  var camAPI;
  var $rootScope;
  var $q;

  beforeEach(module(testModule.name));

  beforeEach(module(function($provide) {
    camAPI = {
      list: sinon.stub(),
      resource: sinon.stub().returnsThis()
    };

    $provide.value('camAPI', camAPI);
  }));

  beforeEach(inject(function(_decisionList_, _$rootScope_, _$q_) {
    decisionList = _decisionList_;
    $rootScope = _$rootScope_;
    $q = _$q_;
  }));

  it('should use decision-definition and drd resources of camAPI', function() {
    expect(camAPI.resource.calledWith('drd')).to.eql(true);
    expect(camAPI.resource.calledWith('decision-definition')).to.eql(true);
  });

  describe('getDecisionsLists', function() {
    var mockDecisions;
    var mockDrds;
    var responsePromise;

    beforeEach(function() {
      mockDecisions = [
        {
          name: '1',
          decisionRequirementsDefinitionId: '1'
        },
        {
          name: '2'
        }
      ];

      mockDrds = [
        {
          id: '1',
          name: 'drd1'
        }
      ];

      camAPI.list.onCall(0).returns($q.when(mockDecisions));
      camAPI.list.onCall(1).returns($q.when(mockDrds));

      responsePromise = decisionList.getDecisionsLists();
    });

    it('should return object with drds and decisions lists', function(done) {
      responsePromise.then(function(response) {
        expect(angular.isArray(response.decisions)).to.eql(true);
        expect(angular.isArray(response.drds)).to.eql(true);

        done();
      });

      $rootScope.$digest();
    });

    it('should connect drds to decisions', function(done) {
      responsePromise.then(function(response) {
        expect(response.decisions[0].drd.name).to.eql(mockDrds[0].name);

        done();
      });

      $rootScope.$digest();
    });
  });
});
