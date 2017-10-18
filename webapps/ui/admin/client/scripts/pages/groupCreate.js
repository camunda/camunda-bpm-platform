'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groupCreate.html', 'utf8');

var Controller = ['$scope', 'page', 'GroupResource', 'Notifications', '$location', '$translate', function($scope, pageService, GroupResource, Notifications, $location, $translate) {

  $scope.$root.showBreadcrumbs = true;

  pageService.titleSet($translate.instant('GROUP_CREATE_NEW_GROUP'));

  pageService.breadcrumbsClear();

  pageService.breadcrumbsAdd([
    {
      label: $translate.instant('GROUP_CREATE_LABEL_GROUP'),
      href: '#/groups'
    },
    {
      label: $translate.instant('GROUP_CREATE_LABEL_NEW_GROUP'),
      href: '#/group-create'
    }
  ]);

    // data model for new group
  $scope.group = {
    id : '',
    name : '',
    type : ''
  };

  $scope.createGroup = function() {
    var group = $scope.group;
    GroupResource.createGroup(group).$promise.then(
        function() {
          Notifications.addMessage({type:'success', status:$translate.instant('NOTIFICATIONS_STATUS_SUCCESS'), message: $translate.instant('GROUP_CREATE_MESSAGE_SUCCESS', { group: group.id })});
          $location.path('/groups');
        },
        function() {
          Notifications.addError({ status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'), message: $translate.instant('GROUP_CREATE_MESSAGE_ERROR', { group: group.id }) });
        }
      );
  };

}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/group-create', {
    template: template,
    controller: Controller,
    authentication: 'required'
  });
}];
