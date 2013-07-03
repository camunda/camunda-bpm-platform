ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $http, $location, Uri) {

    // input: processInstanceId, selection, processInstance

    var pages = $scope.pages = { size: 50, total: 0 };

    var activityInstanceIds = null;

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {
        $location.search('page', currentPage);
      }

      // initially the array of activity instance ids is null
      // to supress a second load of the variables.
      if (activityInstanceIds) {
        updateView(currentPage);  
      }
      
    });

    $scope.$watch('selection.treeToDiagramMap.activityInstances', function (newValue) {
      var instances = newValue || [];
      activityInstanceIds = [];
      // collect the instance ids
      angular.forEach(instances, function (instance) {
        var instanceId = instance.id;
        activityInstanceIds.push(instanceId);
      });

      // always reset the current page to null
      $location.search('page', null);  

      // the variables has to be loaded initially
      if (!pages.currentPage || pages.currentPage ===1) {
        updateView(1);  
      }

    });

    function updateView(page) {
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
      $scope.selection.treeToDiagramMap = {activityInstances: [ variable.instance ], scrollTo: variable.instance};
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
