ngDefine('tasklist.directives', [
  'angular',
  'require'
], function(module, angular, require) {

  var SitebarController = [
    '$scope', '$location', 'EngineApi', 'Authentication',
    function($scope, $location, EngineApi, Authentication) {

    var tasks;
    var authenticatedUser;

    $scope.colleagueCount = {};
    $scope.groupCount = {};

    $scope.$watch(Authentication.username, function(newValue) {
      if (newValue) {
        authenticatedUser = newValue;
        loadSitebar();
      }
    });

    function loadSitebar() {

      if (!authenticatedUser) {
        return;
      }

      tasks = $scope.tasks = {
        mytasks: EngineApi.getTaskCount().get({ 'assignee' : authenticatedUser }),
        unassigned: EngineApi.getTaskCount().get({ 'candidateUser' : authenticatedUser })
      };

      $scope.groupInfo = EngineApi.getGroups(authenticatedUser);

      $scope.groupInfo.$then(function(data){

        angular.forEach(data.data.groupUsers, function(user) {

          EngineApi.getColleagueCount(user.id)
            .$then(function(data) {
              $scope.colleagueCount[user.id] = data.data.count;
            });
        });

        angular.forEach(data.data.groups, function(group) {
          EngineApi.getGroupTaskCount(group.id)
            .$then(function(data) {
              $scope.groupCount[group.id] = data.data.count;
          });
        });
      });
    };

    $scope.isActive = function(filter, search) {
      var params = $location.search();
      return (params.filter || 'mytasks') == filter && params.search == search;
    };

    $scope.$on('tasklist.reload', function () {
      loadSitebar();
    });
  }];

  var SitebarDirective = [ 'AuthenticationService', function(AuthenticationService) {
    return {
      templateUrl: require.toUrl('./sidebar.html'),
      controller: SitebarController
    };
  }];

  module.directive('sidebar', SitebarDirective);
});
