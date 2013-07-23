ngDefine('tasklist.directives', [
  'angular', 
  'require'
], function(module, angular, require) {

  var SitebarController = [ 
    "$scope", "$location", "EngineApi", "Authentication", 
    function($scope, $location, EngineApi, Authentication) {
    
    var currentUser = Authentication.username();

    if (!currentUser) {
      return;
    }

    var tasks;

    $scope.colleagueCount = {};
    $scope.groupCount = {};

    function loadSitebar() {
      tasks = $scope.tasks = {
        mytasks: EngineApi.getTaskCount().get({ "assignee" : currentUser }),
        unassigned: EngineApi.getTaskCount().get({ "candidateUser" : currentUser })
      };

      $scope.groupInfo = EngineApi.getGroups(currentUser);

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
      return (params.filter || "mytasks") == filter && params.search == search;
    };

    $scope.$on("tasklist.reload", function () {
      loadSitebar();
    });

    loadSitebar();
  }];

  var SitebarDirective = function() {
    return {
      templateUrl: require.toUrl('./sitebar.html'),
      controller: SitebarController
    };
  };

  module
    .directive('sitebar', SitebarDirective);
});