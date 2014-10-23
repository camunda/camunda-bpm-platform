define([
  'text!camunda-tasklist-ui/filter/modals/cam-tasklist-filter-form.html'
], function(
  template
) {
  'use strict';

  return [
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

    var filterFormData = $scope.tasklistData.newChild($scope),
        filter = $scope.filter;

    var Filter = camAPI.resource('filter'),
        Authorization = camAPI.resource('authorization');

    // provide (and observe) for dialog neccesary data ////////////////////////////////////////////////////////

    if (!filter) {
      // a new filter will be created!

      $scope.userCanCreateFilter = false;

      filterFormData.provide('filterAuthorizations', function() {
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

      filterFormData.provide('userCanCreateFilter', ['filterAuthorizations', function(filterAuthorizations) {
        filterAuthorizations = filterAuthorizations || {};
        var links = filterAuthorizations.links || [];

        for (var i = 0, link; !!(link = links[i]); i++) {
          if (link.rel === 'create') {
            return true;
          }
        }

        return false;

      }]);

      filterFormData.observe('userCanCreateFilter', function(userCanCreateFilter) {
        $scope.userCanCreateFilter = userCanCreateFilter;
      });

    }
    else {
      // the selected filter will be changed or deleted.

      filterFormData.provide('filterToEdit', $scope.filter);

      filterFormData.provide('userFilterAccess', ['filterToEdit', function (filter) {
        var deferred = $q.defer();

        Filter.authorizations(filter.id, function(err, resp) {

          if(!!err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(resp);
          }

        });            

        return deferred.promise;

      }]);

      filterFormData.provide('definedAuthorizationOnFilterToEdit', ['filterToEdit', function (filter) {
        var deferred = $q.defer();

        Authorization.list({
          resourceType: 5,
          resourceId: filter.id
        }, function (err, resp) {

          if(!!err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(resp);
          }

        });

        return deferred.promise;

      }]);

    }

    // open the dialog ///////////////////////////////////////////////////////////////////////////////////

    $scope.openDialog = function ($event, action) {
      $event.stopPropagation();
      
      if ($scope.filter) {
        // Don't remove the following line!
        // The following line triggers to load filter authorizations and
        // user permission.
        // If this will not be done, the filter authorizations
        // and user permission will be loaded only once. The problem
        // is that the $scope of the modal dialog is still available.
        filterFormData.set('filterToEdit', angular.copy($scope.filter));
      }
      
      $modal.open({
        // creates a child scope of a provided scope
        scope: $scope,
        windowClass: 'filter-edit-modal',
        size: 'lg',
        controller: 'camEditFilterModalCtrl',
        template: template,
        resolve: {
          'filter': function() { return filter; },
          'action': function() { return action; },
          'filterFormData': function() { return filterFormData; }
        }
      }).result.then(function(result) {
        filterFormData.changed('filters');
      });
    };

  }];

});
