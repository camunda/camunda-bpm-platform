ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $http, $location, Uri) {

    // input: processInstanceId, selection, processInstance

    var pages = $scope.pages = { size: 50, total: 0 };

    var activityInstanceIds = null;
    var alreadyUpdated = false;

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {
        $location.search('page', currentPage);
        updateView(currentPage);  
      }
      
    });

    $scope.$watch('selection.treeDiagramMapping.activityInstances', function (newValue) {
      var instances = newValue || [];
      activityInstanceIds = [];
      // collect the instance ids
      angular.forEach(instances, function (instance) {
        var instanceId = instance.id;
        activityInstanceIds.push(instanceId);
      });

      // always reset the current page to null
      $location.search('page', null);
      if ($scope.processInstance.instanceIdToInstanceMap) {
        alreadyUpdated = true;
        updateView(1);    
      }

    });

    // This $watch on 'processInstance.instanceIdToInstance' is necessary due to race
    // conditions. It can happen, that the view will be updated before the the properpty
    // instanceIdToInstanceMap of the variable processInstance has been set.
    $scope.$watch('processInstance.instanceIdToInstanceMap', function (newValue) {
      if (newValue && activityInstanceIds && !alreadyUpdated) {
        updateView(1);
      }

    });

    function updateView(page) {
      $scope.variables = null;
      
      var count = pages.size;
      var firstResult = (page - 1) * count;

      // get the 'count' of variables
      $http.post(Uri.appUri("engine://engine/:engine/variable-instance/count"), {
        processInstanceIdIn : [ $scope.processInstanceId ],
        activityInstanceIdIn :  activityInstanceIds
      })
      .success(function(data) {
        pages.total = Math.ceil(data.count / pages.size);
      });

      // get the variables
      $http.post(Uri.appUri("engine://engine/:engine/variable-instance/"), {
        processInstanceIdIn : [ $scope.processInstanceId ],
        activityInstanceIdIn :  activityInstanceIds
      }, {
        params: {firstResult: firstResult, maxResults: count}
      })
      .success(function(data) {
        var instanceIdToInstanceMap = $scope.processInstance.instanceIdToInstanceMap;
        angular.forEach(data, function(currentVariable) {
          var instance = instanceIdToInstanceMap[currentVariable.activityInstanceId];
          currentVariable.instance = instance;
        });              
        $scope.variables = data;
      });
    };

    $scope.selectActivityInstance = function (variable) {
      $scope.selection.treeDiagramMapping = {activityInstances: [ variable.instance ], scrollTo: variable.instance};
    }
  };

  Controller.$inject = [ '$scope', '$http', '$location', 'Uri' ];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.instanceDetails', {
      id: 'variables-tab',
      label: 'Variables',
      url: 'plugin://base/static/app/views/processInstance/variables-tab.html',
      controller: Controller
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
