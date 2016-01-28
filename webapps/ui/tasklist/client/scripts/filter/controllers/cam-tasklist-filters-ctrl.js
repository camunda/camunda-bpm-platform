'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/../modals/cam-tasklist-filter-modal.html', 'utf8');

  module.exports = [
    '$scope',
    '$modal',
    '$q',
    'camAPI',
  function(
    $scope,
    $modal,
    $q,
    camAPI
  ) {

    var filtersData = $scope.filtersData = $scope.tasklistData.newChild($scope);

    var Filter = camAPI.resource('filter');

    $scope.userCanCreateFilter = false;

    // provide /////////////////////////////////////////////////////////////////////////////////

    filtersData.provide('filterAuthorizations', function() {
      var deferred = $q.defer();

      Filter.authorizations(function(err, res) {
        if(err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }

      });

      return deferred.promise;

    });

    filtersData.provide('userCanCreateFilter', ['filterAuthorizations', function(filterAuthorizations) {
      filterAuthorizations = filterAuthorizations || {};
      var links = filterAuthorizations.links || [];

      for (var i = 0, link; !!(link = links[i]); i++) {
        if (link.rel === 'create') {
          return true;
        }
      }

      return false;

    }]);

    // observe ////////////////////////////////////////////////////////////////////////////////

    filtersData.observe('userCanCreateFilter', function(userCanCreateFilter) {
      $scope.userCanCreateFilter = userCanCreateFilter;
    });

    // open modal /////////////////////////////////////////////////////////////////////////////

    $scope.openModal = function ($event, filter) {
      $event.stopPropagation();

      $modal.open({
        windowClass: 'filter-modal',
        size: 'lg',
        controller: 'camFilterModalCtrl',
        template: template,
        resolve: {
          'filter': function() { return filter; },
          'filtersData': function() { return filtersData; }
        }

      }).result.then(function() {
        filtersData.changed('filters');
      }, function () {
        filtersData.changed('filters');
      });

    };

  }];
