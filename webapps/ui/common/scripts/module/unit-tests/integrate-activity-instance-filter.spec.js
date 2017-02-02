'use strict';

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var angular = require('angular');
var testModule = require('../index');

require('angular-mocks');

var module = angular.mock.module;
var inject = angular.mock.inject;

describe('cam-common integrateActivityInstanceFilter', function() {
  var activityIds;
  var params;
  var search;
  var $location;
  var searchWidgetUtils;
  var processData;
  var options;
  var $scope;
  var setDefaultTab;

  beforeEach(module(testModule.name));

  beforeEach(module(function($provide) {
    activityIds = ['a1', 'as2'];
    params = {
      activityIds: activityIds.join(','),
      page: '20'
    };
    search = sinon.stub().returns(params);
    search.updateSilently = sinon.spy();
    $location = {
      path: sinon.stub()
    };
    searchWidgetUtils ={
      replaceActivitiesInSearchQuery: sinon.stub(),
      getActivityIdsFromUrlParams: sinon.stub().returns(activityIds),
      shouldUpdateFilte: sinon.stub()
    };

    $provide.value('search',search);
    $provide.value('$location',$location);
    $provide.value('searchWidgetUtils',searchWidgetUtils);
  }));

  beforeEach(inject(function(integrateActivityInstanceFilter) {
    processData = {
      provide: sinon.spy(),
      observe: sinon.spy(),
      set: sinon.spy()
    };
    $scope = {
      $on: sinon.spy(),
      processData: processData,
      processInstance: {
        id: 'inst-id-23'
      }
    };
    setDefaultTab = sinon.spy();
    options = {};

    integrateActivityInstanceFilter($scope, setDefaultTab, options);
  }));

  it('should set filter on $scope', function() {
    expect($scope.filter).to.be.ok;
  });

  it('should provide filter', function() {
    expect(processData.provide.calledWith('filter')).to.eql(true);
  });

  it('should observe filter, instanceIdToInstanceMap, activityIdToInstancesMap', function() {
    expect(processData.observe
      .calledWith(['filter', 'instanceIdToInstanceMap', 'activityIdToInstancesMap'])
    ).to.eql(true);
  });

  it('should listen to $locationChangeSuccess event', function() {
    expect($scope.$on.calledWith('$locationChangeSuccess')).to.eql(true);
  });

  describe('parsing filter from url', function() {
    it('should get url param from search', function() {
      expect(search.called).to.eql(true);
    });

    it('should create filter with activityIds', function() {
      expect($scope.filter.activityIds).to.eql(activityIds);
    });

    it('should create filter with activityInstanceIds', function() {
      expect($scope.filter.activityInstanceIds).to.eql(activityIds);
    });

    it('should create filter with current page', function() {
      expect($scope.filter.page).to.eql(20);
    });

    it('should replace current filter in url', function() {
      expect($scope.filter.replace).to.eql(true);
    });
  });

  describe('autocompliting filter', function() {
    var autoCompleteFilter;
    var instanceIdToInstanceMap;
    var activityIdToInstancesMap;

    beforeEach(function() {
      delete $scope.filter;

      autoCompleteFilter = processData.observe.lastCall.args[1];

      instanceIdToInstanceMap = {
        a1: {
          activityId: 'a1-activity'
        },
        'a1-b': {
          activityId: 'a1-activity'
        }
      };
      activityIdToInstancesMap = {
        'a1-activity': [
          {
            id:'a1'
          },
          {
            id: 'a1-b'
          }
        ]
      };
    });

    it('should add activity id to filter based on activity instance id', function() {
      var filter = {
        activityInstanceIds: ['a1']
      };

      autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect($scope.filter.activityIds).to.eql([instanceIdToInstanceMap.a1.activityId]);
    });

    it('should add activity instance id to filter based on activity id', function() {
      var filter = {
        activityIds: ['a1-activity']
      };

      autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect($scope.filter.activityInstanceIds).to.eql(['a1', 'a1-b']);
    });

    it('should remove non existing ids from filter if options has shouldRemoveActivityIds set to true', function() {
      var filter = {
        activityIds: ['b', 'a1-activity'],
        activityInstanceIds: ['c', 'a1', 'a1-b']
      };
      options.shouldRemoveActivityIds = true;

      autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect($scope.filter.activityIds).to.eql([instanceIdToInstanceMap.a1.activityId]);
      expect($scope.filter.activityInstanceIds).to.eql(['a1', 'a1-b']);
    });

    it('should not remove non existing ids from filter if options has no shouldRemoveActivityIds', function() {
      var filter = {
        activityIds: ['b', 'a1-activity'],
        activityInstanceIds: ['c', 'a1', 'a1-b']
      };

      autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect($scope.filter.activityIds).to.eql(['b', 'a1-activity']);
      expect($scope.filter.activityInstanceIds).to.eql(['a1', 'a1-b']);
    });


    it('should scroll to last selected activity', function() {
      var filter = {
        activityIds: ['a1-activity']
      };

      autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect($scope.filter.scrollToBpmnElement).to.eql(filter.activityIds[0]);
    });

    it('should set new instance of filter on processData', function() {
      autoCompleteFilter({}, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect(processData.set.calledWith('filter')).to.eql(true);
    });

    it('should not set filter on processData if it has not changed', function() {
      var filter = {
        activityIds: ['a1-activity'],
        activityInstanceIds: ['a1', 'a1-b'],
        page: 20,
        scrollToBpmnElement: 'a1-activity'
      };

      $scope.filter = filter;
      autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

      expect(processData.set.called).to.eql(false);
    });

    describe('url serialization', function() {
      var filter;
      var urlParams;
      var params;

      beforeEach(function() {
        filter = {
          activityIds: ['a1-activity'],
          activityInstanceIds: ['a1', 'a1-b'],
          page: 20,
          scrollToBpmnElement: 'a1-activity',
          replace: true
        };
        urlParams = {
          searchQuery: JSON.stringify([])
        };

        search.returns(urlParams);
        searchWidgetUtils.replaceActivitiesInSearchQuery.returns('searchQuery');
        autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

        params = search.updateSilently.lastCall.args[0];
      });

      it('should replace url if filter has replace flag set to true', function() {
        expect(
          search.updateSilently.lastCall.args[1]
        ).to.eql(true);
      });

      it('should not replace url if filter has no replace falsy replace flag', function() {
        filter.replace = false;

        autoCompleteFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap);

        expect(
          search.updateSilently.lastCall.args[1]
        ).to.eql(false);
      });

      it('should create properly searchQuery in params', function() {
        expect(params.searchQuery).to.be.ok;
        expect(searchWidgetUtils.replaceActivitiesInSearchQuery.calledWith);
      });

      it('should serialize activity ids', function() {
        expect(params.activityIds).to.eql('a1-activity');
      });
    });
  });
});
