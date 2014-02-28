ngDefine('cockpit.plugin.failed-jobs-plugin', function(module) {

  var DashboardController = function($scope, $http, Uri) {
	  
	  $scope.livePath = function(instanceId){
		  return "/camunda/app/cockpit/default/#/process-instance/" + instanceId + "/live";
	  };
	  
	  $scope.jobRestUrl = Uri.appUri("engine://engine/default/job");
	  $scope.loading = true; 
	  $scope.failedJobs=[];
	  $http.get($scope.jobRestUrl, { params: { withException: true } })
      .success(function(data) {    
    	  $scope.loading = false;
    	  $scope.failedJobs = data;
      });
  };

  DashboardController.$inject = ["$scope", "$http", "Uri"];

  var Configuration = function Configuration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'failed-jobs',
      label: 'Failed Jobs',
      url: 'plugin://failed-jobs-plugin/static/app/dashboard.html',
      controller: DashboardController,
      priority: 15
    });
  };

  
  
  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);

  return module;
});
