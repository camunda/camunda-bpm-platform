define([
  'angular',
  'text!./groupEdit.html',
  'text!./generic-confirmation.html'
], function(
  angular,
  template,
  confirmationTemplate
) {
  'use strict';

  var Controller = [
    '$scope',
    '$routeParams',
    'GroupResource',
    'UserResource',
    'AuthorizationResource',
    'Notifications',
    '$location',
    '$modal',
  function (
    $scope,
    $routeParams,
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

    var loadGroupUsers = $scope.loadGroupUsers = function() {
      $scope.userLoadingState = 'LOADING';
      UserResource.query({'memberOfGroup' : $scope.encodedGroupId}).$promise.then(function(response) {
        $scope.groupUserList = response;
        $scope.userLoadingState = response.length ? 'LOADED' : 'EMPTY';
      }, function () {
        $scope.userLoadingState = 'ERROR';
      });
    };

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
    loadGroupUsers();

    if(!$location.search().tab) {
      $location.search({'tab': 'group'});
      $location.replace();
    }

  }];

  return [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/groups/:groupId', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }];
});
