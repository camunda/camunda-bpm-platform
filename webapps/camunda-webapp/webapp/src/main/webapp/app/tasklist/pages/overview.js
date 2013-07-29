ngDefine('tasklist.pages', [
  'angular',
  'bpmn/Bpmn',
], function(module, angular, Bpmn) {

  var Controller = function($rootScope, $scope, $location, debounce, EngineApi, Notifications, Authentication) {

    var fireTaskListChanged = debounce(function() {
      $rootScope.$broadcast("tasklist.reload");
    }, 300);

    var notifyScopeChange = function(message) {
      Notifications.addMessage({ status: "Scope change", message: message, exclusive: [ "status" ], duration: 5000 });
    };

    function searchQuery(query, view) {
      angular.forEach(view, function(value, key) {
        if (key == "filter" || key == "search") {
          return;
        }

        query[key] = value;
      });

      return query;
    };

    var reloadTasks = debounce(function() {
      var view = $scope.taskList.view;

      $scope.loadTasks(view);
    }, 300);

    $scope.taskList = {
      sort: {
        by: "created",
        order: "desc"
      },
      selection: []
    };

    $scope.$on("sortChanged", function() {
      reloadTasks();
    });

    $scope.loadTasks = function(view) {
      var filter = view.filter,
          search = view.search,
          user = Authentication.username();

      $scope.groupInfo = EngineApi.getGroups(user);

      $scope.taskList.view = { filter: filter, search: search };

      var queryObject = {},
          user = Authentication.username(),
          sort = $scope.taskList.sort;

      queryObject.userId = user;

      switch (filter) {
        case "mytasks":
          queryObject.assignee = user;
          break;
        case "unassigned":
          queryObject.candidateUser = user;
          break;
        case "colleague":
          queryObject.assignee = search;
          break;
        case "group":
          queryObject.candidateGroup = search;
          break;
        case "search":
          searchQuery(queryObject, view);
          break;
      }

      queryObject.sortBy = sort.by;
      queryObject.sortOrder = sort.order;

      EngineApi.getProcessDefinitions().query().$then(function(response) {
        var processDefinitions = {};
        $.each(response.resource, function(index, definition) {
          processDefinitions[definition.id] = definition;
        });
        $scope.processDefinitions = processDefinitions;
      });

      EngineApi.getTaskList().query(queryObject).$then(function(response) {
        $scope.taskList.tasks = response.resource;
      });
    };

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      var view = angular.extend({ filter: "mytasks" }, newValue);
      $scope.loadTasks(view);
    });

    $scope.claimTask = function(task) {

      return EngineApi.getTaskList().claim({ id : task.id }, { userId: Authentication.username() }).$then(function () {
        var tasks = $scope.taskList.tasks,
            view = $scope.taskList.view;

        if (view.filter == "mytasks") {
          $scope.addTask(task);
        } else {
          $scope.removeTask(task);
        }

        notifyScopeChange("Claimed task <a href='#/overview?filter=mytasks&selection=" + task.id + "'>" + task.name + "</a>");

        fireTaskListChanged();
      });
    };

    $scope.unclaimTask = function(task) {
      return EngineApi.getTaskList().unclaim({ id : task.id }).$then(function () {
        $scope.removeTask(task);

        notifyScopeChange("Unclaimed task <a href='#/overview?filter=unassigned&selection=" + task.id + "'>" + task.name + "</a>");

        fireTaskListChanged();
      });
    };

    $scope.addTask = function (task) {
      $scope.taskList.tasks.push(task);
    };

    $scope.removeTask = function (task) {
      var tasks = $scope.taskList.tasks;

      var idx = tasks.indexOf(task);
      if (idx != -1) {
        tasks.splice(idx, 1);
      }
    };

    $scope.delegateTask = function(task, user) {
      return EngineApi.getTaskList().delegate( { id : task.id}, { userId: user.id }).$then(function () {
        $scope.removeTask(task);

        notifyScopeChange("Delegated task");
        fireTaskListChanged();
      });
    };

    $scope.claimTasks = function (selection) {
      for (var index in selection) {
        var task = selection[index];
        $scope.claimTask(task);
      }

      notifyScopeChange("Claimed " + selection.length + " tasks");
    };

    $scope.delegateTasks = function (selection) {
      for (var index in selection) {
        var task = selection[index];
        $scope.delegateTask(task);
      }

      notifyScopeChange("Delegated " + selection.length + " tasks");
    };

    $scope.isSelected = function(task) {
      return $scope.taskList.selection.indexOf(task) != -1;
    };

    $scope.select = function (task) {
      $scope.taskList.selection = [];
      $scope.taskList.selection.push(task);
    };

    $scope.selectAllTasks = function() {

      $scope.deselectAllTasks();

      var selection = $scope.taskList.selection,
          tasks = $scope.taskList.tasks;

      angular.forEach(tasks, function(task) {
        selection.push(task);
      });
    };

    $scope.deselectAllTasks = function() {
      return $scope.taskList.selection = [];
    };

    $scope.bpmn = { };

    $scope.isDiagramActive = function(task) {
      return $scope.bpmn.task == task;
    };

    $scope.toggleShowDiagram = function (task, index) {
      var diagram = $scope.bpmn.diagram,
          oldTask = $scope.bpmn.task;

      $scope.bpmn = {};

      if (diagram) {
        // destroy old diagram
        diagram.clear();

        if (task == oldTask) {
          return;
        }
      }

      $scope.bpmn.task = task;

      EngineApi.getProcessDefinitions().xml({ id : task.processDefinitionId }).$then(function (result) {
        var diagram = $scope.bpmn.diagram,
            xml = result.data.bpmn20Xml;

        if (diagram) {
          diagram.clear();
        }

        var width = $("#diagram").width();
        var height = $("#diagram").height();

        diagram = new Bpmn().render(xml, {
          diagramElement : "diagram",
          width: width,
          height: 400
        });

        diagram.annotation(task.taskDefinitionKey).addClasses([ "bpmn-highlight" ]);

        $scope.bpmn.diagram = diagram;
      });
    };
  };

  Controller.$inject = ["$rootScope", "$scope", "$location", "debounce", "EngineApi", "Notifications", "Authentication"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/overview", {
      templateUrl: "pages/overview.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("OverviewController", Controller);
});