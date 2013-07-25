ngDefine('tasklist.pages', [
  'angular'
], function(module, angular) {

  var Controller = function($scope, $routeParams, $location, $rootScope, Forms, EngineApi) {

    var processDefinitionId = $routeParams.id,
        variables = $scope.variables = [];

    var form = $scope.form = {
      generic: $location.hash() == "generic"
    };

    var processDefinition = $scope.processDefinition = EngineApi.getProcessDefinitions().get({ id: processDefinitionId });

    processDefinition.$then(function() {
      form.data = EngineApi.getProcessDefinitions().getStartForm({ id: processDefinitionId }).$then(function(response) {
        var data = response.resource;

        Forms.parseFormData(data, form);

        if (form.external) {
          var externalUrl = encodeURI(form.key + "?processDefinitionKey=" + processDefinition.key + "&callbackUrl=" + $location.absUrl() + "/complete");
          window.location.href = externalUrl;
        }

        form.loaded = true;
      });
    });

    $scope.activateGeneric = function() {
      $location.hash('generic');
      form.generic = true;
    };

    $scope.submit = function() {
      var variablesMap = Forms.variablesToMap(variables);

      EngineApi.getProcessDefinitions().startInstance({ id: processDefinitionId }, { variables : variablesMap }).$then(function() {
        $rootScope.$broadcast("tasklist.reload");
        $location.url("/process-definition/" + processDefinitionId + "/complete");
      });
    };

    $scope.cancel = function() {
      $location.url("/overview");
    };
  };

  Controller.$inject = ["$scope", "$routeParams", "$location", "$rootScope", "Forms", "EngineApi"];

  var CompleteController = function($scope, $location, $routeParams, Notifications, EngineApi) {

    var processDefinitionId = $routeParams.id;

    EngineApi.getProcessDefinitions().get({ id: processDefinitionId }).$then(function(response) {
      var processDefinition = response.resource;

      Notifications.addMessage({ status: "Completed", message: "Instance of <a>" + (processDefinition.name || processDefinition.key) + "</a> has been started", duration: 5000 });
      $location.url("/overview");
    });
  };

  CompleteController.$inject = ["$scope", "$location", "$routeParams", "Notifications", "EngineApi"];

  var RouteConfig = function($routeProvider) {

    $routeProvider.when("/process-definition/:id", {
      templateUrl: "pages/start.html",
      controller: Controller
    });

    // controller which handles process instance start completion

    $routeProvider.when("/process-definition/:id/complete", {
      controller: CompleteController,
      templateUrl: "pages/complete.html"
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("StartProcessInstanceController", Controller)
    .controller("StartProcessInstanceCompleteController", CompleteController);
});