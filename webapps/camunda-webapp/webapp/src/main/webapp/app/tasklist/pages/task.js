ngDefine('tasklist.pages', [
  'angular'
], function(module, angular) {

  var Controller = function($rootScope, $scope, $location, $routeParams, $window, Forms, Notifications, EngineApi) {

    var taskId = $routeParams.id,
        variables = $scope.variables = [];

    var form = $scope.form = {
      generic: $location.hash() == 'generic'
    };

    function getVariableByName(name, variables) {

      for (var i = 0, variable; !!(variable = variables[i]); i++) {
        if (variable.name == name) {
          return variable;
        }
      }

      return null;
    };

    var task = $scope.task = EngineApi.getTaskList().get({ id: taskId });

    task.$then(function() {
      form.data = EngineApi.getTaskList().getForm({ id: taskId }).$then(function(response) {
        var data = response.resource;

        Forms.parseFormData(data, form);

        if (form.external) {
          var externalUrl = encodeURI(form.key + "?taskId=" + taskId + "&callbackUrl=" + $location.absUrl() + "/complete");

          $window.location.href = externalUrl;
        } else {
          form.loaded = true;

          switch (task.delegationState) {
            case "PENDING":
              Notifications.addMessage({ status: "Delegation", message: "This task was delegated to you by " + task.owner });
              break;
            case "RESOLVED":
              Notifications.addMessage({ status: "Delegation", message: "The colleague you delegated that task to resolved it" });
              break;
          }

          EngineApi.getProcessInstance().variables({ id : task.processInstanceId }).$then(function (result) {
            var variables = Forms.mapToVariablesArray(result.data),
                scopeVariables = $scope.variables;

            for (var i = 0, variable; !!(variable = variables[i]); i++) {
              var variableInScope = getVariableByName(variable.name, scopeVariables);
              if (!variableInScope) {
                $scope.variables.push({ name: variable.name, value: variable.value, type: variable.type.toLowerCase() });
              } else {
                variableInScope.value = variable.value;
              }
            }
          });
        }
      });
    });

    $scope.activateGeneric = function() {
      $location.hash('generic');
      form.generic = true;
    };

    $scope.submit = function() {
      var variablesMap = Forms.variablesToMap(variables);

      var taskList = EngineApi.getTaskList();

      var action = "complete";
      if (task.delegationState) {
        action = "resolve";
      }

      taskList[action]({ id: taskId }, { variables : variablesMap }).$then(function() {
        $rootScope.$broadcast("tasklist.reload");
        $location.url("/task/" + taskId + "/complete");
      });
    };

    $scope.cancel = function() {
      $location.url("/overview");
    };
  };

  Controller.$inject = ["$rootScope", "$scope", "$location", "$routeParams", "$window", "Forms", "Notifications", "EngineApi"];


  var CompleteController = function($scope, $location, Notifications) {
    Notifications.addMessage({ status: "Completed", message: "Task has been completed", duration: 5000 });
    $location.url("/overview");
  };

  CompleteController.$inject = ["$scope", "$location", "Notifications"];


  var RouteConfig = function($routeProvider) {

    $routeProvider.when("/task/:id", {
      templateUrl: "pages/task.html",
      controller: Controller
    });

    // controller which handles task completion

    $routeProvider.when("/task/:id/complete", {
      controller: CompleteController,
      templateUrl: "pages/complete.html"
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("CompleteTaskController", CompleteController)
    .controller("TaskController", Controller);

});