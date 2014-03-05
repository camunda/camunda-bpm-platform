ngDefine('cockpit.plugin.failed-jobs-plugin', function(module) {

  var DashboardController = function($scope, $http, Uri) {
    
    $scope.livePath = function(instanceId) {
      return Uri.appUri("app://#/process-instance/" + instanceId + "/live");
    };

    $scope.jobRestUrl = Uri.appUri("engine://engine/default/job");
    $scope.totalPages = 0;
    $scope.pageNumber = 1;
    $scope.failedJobsCount = 0;

    $scope.loading = true;
    $scope.failedJobs = [];

    $scope.filterCriteria = {
      firstResult : 0,
      maxResults : 10,
      withException : true
    };

    var loadPage = function() {
      $scope.loading = true;
      $scope.filterCriteria.firstResult = ($scope.pageNumber - 1) * 10;
      $http.get($scope.jobRestUrl, {
        params : $scope.filterCriteria
      }).success(function(data) {
        $scope.loading = false;
        $scope.failedJobs = data;
      });
    };

    $scope.$watch('pageNumber', function(newValue, oldValue) {
      if (newValue == oldValue)
        return;
      loadPage();
    });

    $http.get($scope.jobRestUrl + "/count", {
      params : {
        withException : true
      }
    }).success(function(data) {
      var pg = parseInt(($scope.failedJobsCount = data.count) / 10);
      $scope.totalPages = (data.count % 10) ? (pg + 1) : pg;
    });

    loadPage();
  };

  DashboardController.$inject = [ "$scope", "$http", "Uri" ];

  var Configuration = function Configuration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id : 'failed-jobs',
      label : 'Failed Jobs',
      url : 'plugin://failed-jobs-plugin/static/app/dashboard.html',
      controller : DashboardController,
      priority : 15
    });
  };

  Configuration.$inject = [ 'ViewsProvider' ];

  module.config(Configuration);

  return module;
});
