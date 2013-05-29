ngDefine('cockpit.plugin.base.pages', function(module) {

  var Controller = function ($scope, $routeParams, $location, PluginProcessInstanceResource, ProcessInstanceResource) {

    var processDefinitionId = $routeParams.processDefinitionId;

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      $scope.currentPage = newValue || 1;
    });

    $scope.$watch('currentPage', function(newValue) {
      $location.search('page', newValue || 1);
    });

    $scope.pageChanged = function (page) {
      PluginProcessInstanceResource.query(
          {
            processDefinitionId: processDefinitionId,
            firstResult: page.firstResult,
            offset: page.offset
          })
          .$then(function(data) {
            $scope.processInstances = data.resource;
          });

      $scope.initNumInstances();

    };

    $scope.initNumInstances = function () {
      ProcessInstanceResource.count(
          {
            processDefinitionId : processDefinitionId
           })
        .$then(function(data) {
          $scope.numInstances = data.data.count;
        });
    };

    $scope.initNumInstances();

    // $location

//    $scope.$watch(function() { return $location.search().page; }, function(newPage) {
//      // recompute view based on current page
//    });
//
//    $location.search('page', 1000);

    $scope.shortCutProcessInstanceId = function (processInstanceId) {
      if (processInstanceId.length > 10) {
        return processInstanceId.substring(0, 10) + "...";
      }
      return processInstanceId;
    };

  };

  Controller.$inject = [ '$scope', '$routeParams', '$location', 'PluginProcessInstanceResource', 'ProcessInstanceResource' ];


  var PluginConfiguration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.process.instances', {
      id: 'process-instances-table',
      label: 'Process Instances',
      url: 'plugin://base/static/app/pages/process-instance-table.html',
      controller: Controller
    });
  };

  PluginConfiguration.$inject = ['ViewsProvider'];

  module
    .config(PluginConfiguration);

  return module;

});
