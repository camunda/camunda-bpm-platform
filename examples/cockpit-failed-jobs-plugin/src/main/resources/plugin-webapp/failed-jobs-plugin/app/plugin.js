ngDefine('cockpit.plugin.failed-jobs-plugin', function(module) {

	var DashboardController = function($scope, $http, Uri) {

		$scope.livePath = function(instanceId) {
			return "/camunda/app/cockpit/default/#/process-instance/"
					+ instanceId + "/live";
		};

		$scope.jobRestUrl = Uri.appUri("engine://engine/default/job");
		$scope.totalPages = 0;
		$scope.failedJobsCount = 0;
		$scope.loading = true;
		$scope.pageLoading = false;
		$scope.failedJobs = [];
		$scope.pageNumber = 1;
		$scope.filterCriteria = {
			firstResult : 0,
			maxResults : 10,
			withException : true
		};
		
		$scope.fetchResult = function() {
			$http.get($scope.jobRestUrl, { params : $scope.filterCriteria }).success(function(data) {
				$scope.loading = false;
				$scope.pageLoading = false;
				$scope.failedJobs = data;
			});
		};
		
		var loadPage = function() {
			if($scope.pageNumber < 1) $scope.pageNumber = 1;
			$scope.pageLoading = true;
			$scope.filterCriteria.firstResult = ($scope.pageNumber - 1) * 10;
			$scope.fetchResult();
		};
		
		$scope.goToPage = function(ev) {
			var pg;
			if(!$scope.pageNumber || isNaN(pg = parseInt($scope.pageNumber)) || pg < 1) $scope.pageNumber = 1;
			else if($scope.pageNumber > $scope.totalPages) $scope.pageNumber = $scope.totalPages;
			loadPage();
		};
		
		$scope.goToNext = function() {
			if($scope.pageNumber < $scope.totalPages){
				$scope.pageNumber++;
				loadPage();
			}
		};
		
		$scope.goToPrevious = function() {
			if($scope.pageNumber > 1){
				$scope.pageNumber--;
				loadPage();
			}
		};
		
		$http.get($scope.jobRestUrl + "/count", {params : {withException : true}}).success(function(data) {
			var pg = parseInt(($scope.failedJobsCount = data.count) / 10);
			$scope.totalPages = (data.count % 10) ? (pg + 1) : pg;
		});
		
		$scope.fetchResult();
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
