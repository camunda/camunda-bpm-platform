'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groupEdit.html', 'utf8');
var confirmationTemplate = fs.readFileSync(__dirname + '/generic-confirmation.html', 'utf8');

var angular = require('angular');

  var Controller = [
    '$scope',
    '$routeParams',
    'search',
    'GroupResource',
    'UserResource',
    'AuthorizationResource',
    'Notifications',
    '$location',
    '$modal',
  function (
    $scope,
    $routeParams,
    search,
    GroupResource,
    UserResource,
    AuthorizationResource,
    Notifications,
    $location,
    $modal
  ) {

    $scope.group = null;
    $scope.groupName = null;
    $scope.encodedGroupId = $routeParams.groupId
                                            .replace(/\//g, '%2F')
                                            .replace(/\\/g, '%5C');

    $scope.availableOperations = {};
    $scope.groupUserList = null;

    var groupUserPages = $scope.groupUserPages = { size: 25, total: 0 };

    // common form validation //////////////////////////

    /** form must be valid & user must have made some changes */
    $scope.canSubmit = function(form, modelObject) {
      return form.$valid &&
             !form.$pristine &&
             (!modelObject || !angular.equals($scope[modelObject], $scope[modelObject + 'Copy']));
    };

    // update group form /////////////////////////////

    var loadGroup = $scope.loadGroup = function() {
      $scope.groupLoadingState = 'LOADING';
      GroupResource.get({groupId : $scope.encodedGroupId}).$promise.then(function(response) {
        $scope.groupLoadingState = 'LOADED';
        $scope.group = response;
        $scope.groupName = (!!response.name ? response.name : response.id);
        $scope.groupCopy = angular.copy(response);
      }, function () {
        $scope.groupLoadingState = 'ERROR';
      });
    };

    $scope.$watch(function() {
      return $location.search().tab === 'users' && parseInt(($location.search() || {}).page || '1');
    }, function(newValue) {
      if (newValue) {
        groupUserPages.current = newValue;
        updateGroupUserView();
      }
    });

    $scope.pageChange = function(page) {
      search.updateSilently({ page: !page || page == 1 ? null : page });
    };

    function updateGroupUserView() {
      var page = groupUserPages.current,
          count = groupUserPages.size,
          firstResult = (page - 1) * count;

      var searchParams = {
        memberOfGroup : $scope.encodedGroupId
      };

      var pagingParams = {
        firstResult: firstResult,
        maxResults: count
      };

      $scope.userLoadingState = 'LOADING';
      UserResource.query(angular.extend({}, searchParams, pagingParams)).$promise.then(function(response) {
        $scope.groupUserList = response;
        $scope.userLoadingState = response.length ? 'LOADED' : 'EMPTY';
      }, function() {
        $scope.userLoadingState = 'ERROR';
      });

      UserResource.count(searchParams).$promise.then(function(response) {
        groupUserPages.total = response.count;
      });
    }

    GroupResource.OPTIONS({groupId : $scope.encodedGroupId}).$promise.then(function(response) {
      // angular.forEach(response.data.links, function(link){
      angular.forEach(response.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });
    });

    $scope.updateGroup = function() {

      GroupResource.update({groupId: $scope.encodedGroupId}, $scope.group).$promise.then(function(){
          Notifications.addMessage({
            type: 'success',
            status: 'Success',
            message: 'Group successfully updated.'
          });
          loadGroup();
        },
        function() {
          Notifications.addError({
            status: 'Failed',
            message: 'Failed to update group'
          });
        }
      );
    };

    // delete group form /////////////////////////////

    $scope.deleteGroup = function() {
      $modal.open({
        template: confirmationTemplate,
        controller: ['$scope', function ($dialogScope) {
          $dialogScope.question = 'Really delete group ' + $scope.group.id + '?';
        }]
      }).result.then(function () {
        GroupResource.delete({
          'groupId':$scope.encodedGroupId
        }).$promise.then(
          function(){
            Notifications.addMessage({
              type: 'success',
              status: 'Success',
              message: 'Group ' + $scope.group.id + ' successfully deleted.'
            });
            $location.path('/groups');
          }
        );
      });
    };

    // page controls ////////////////////////////////////

    $scope.show = function(fragment) {
      return fragment == $location.search().tab;
    };

    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? 'active' : '';
    };

    // initialization ///////////////////////////////////

    loadGroup();

    if(!$location.search().tab) {
      $location.search({'tab': 'group'});
      $location.replace();
    }

  }];

  module.exports = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/groups/:groupId', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }];
