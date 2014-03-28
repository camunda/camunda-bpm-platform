/* global ngDefine: false, require: false */
ngDefine('tasklist.pages', [
  'angular',
  'jquery',
  'bpmn/Bpmn',
], function(module, angular, $, Bpmn) {
  'use strict';

  var OverviewController = [
    '$rootScope',
    '$scope',
    '$location',
    'debounce',
    'EngineApi',
    'Notifications',
    'authenticatedUser',
  function(
     $rootScope,
     $scope,
     $location,
     debounce,
     EngineApi,
     Notifications,
     authenticatedUser
   ) {

    var fireTaskListChanged = debounce(function() {
      $rootScope.$broadcast('tasklist.reload');
    }, 300);

    var notifyScopeChange = function(message) {
      Notifications.addMessage({ status: 'Scope change', message: message, exclusive: [ 'status' ], duration: 5000 });
    };

    function searchQuery(query, view) {
      angular.forEach(view, function(value, key) {
        if (key == 'filter' || key == 'search') {
          return;
        }

        query[key] = value;
      });

      return query;
    }

    var reloadTasks = debounce(function() {
      var view = $scope.taskList.view;

      loadTasks(view);
    }, 300);

    $scope.taskList = {
      sort: {
        by: 'created',
        order: 'desc'
      },
      selection: []
    };

    $scope.allTasksSelected = false;

    $scope.$on('sortChanged', function() {
      reloadTasks();
    });

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      var view = angular.extend({ filter: 'mytasks' }, newValue);
      loadTasks(view);
    });

    $scope.groupInfo = EngineApi.getGroups(authenticatedUser);

    function loadTasks(view) {
      var filter = view.filter,
          search = view.search;

      $scope.taskList.view = { filter: filter, search: search };

      var queryObject = {},
          // user = authenticatedUser,
          sort = $scope.taskList.sort;

      queryObject.userId = authenticatedUser;

      switch (filter) {
        case 'mytasks':
          queryObject.assignee = authenticatedUser;
          break;
        case 'unassigned':
          queryObject.candidateUser = authenticatedUser;
          break;
        case 'colleague':
          queryObject.assignee = search;
          break;
        case 'group':
          queryObject.candidateGroup = search;
          break;
        case 'search':
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
    }

    $scope.claimTask = function(task) {

      return EngineApi.getTaskList().claim({ id : task.id }, { userId: authenticatedUser }).$then(function () {
        // var tasks = $scope.taskList.tasks,
        //     view = $scope.taskList.view;
        var view = $scope.taskList.view;

        if (view.filter == 'mytasks') {
          $scope.addTask(task);
        } else {
          $scope.removeTask(task);
        }

        notifyScopeChange('Claimed task <a href="#/overview?filter=mytasks&selection=' + task.id + '">' + task.name + '</a>');

        fireTaskListChanged();
      });
    };

    $scope.unclaimTask = function(task) {
      return EngineApi.getTaskList().unclaim({ id : task.id }).$then(function () {
        $scope.removeTask(task);

        notifyScopeChange('Unclaimed task <a href="#/overview?filter=unassigned&selection=' + task.id + '">' + task.name + '</a>');

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

      var selectionIdx = $scope.taskList.selection.indexOf(task);
      if (selectionIdx !== -1) {
        $scope.taskList.selection.splice(selectionIdx, 1);
      }

      if (!$scope.taskList.selection.length) {
        $scope.allTasksSelected = false;
      }

    };

    $scope.delegateTask = function(task, user) {
      return EngineApi.getTaskList().delegate( { id : task.id}, { userId: user.id }).$then(function () {
        $scope.removeTask(task);

        notifyScopeChange('Delegated task');
        fireTaskListChanged();
      });
    };

    $scope.claimTasks = function (selection) {
      for (var index in selection) {
        var task = selection[index];
        $scope.claimTask(task);
      }

      notifyScopeChange('Claimed ' + selection.length + ' tasks');
    };

    $scope.unclaimTasks = function (selection) {
      for (var i = 0, task; !!(task = selection[i]); i++) {
        $scope.unclaimTask(task);
      }

      notifyScopeChange('Unclaimed ' + selection.length + ' tasks');
    };

    $scope.delegateTasks = function (selection, user) {
      for (var index in selection) {
        var task = selection[index];
        $scope.delegateTask(task, user);
      }

      notifyScopeChange('Delegated ' + selection.length + ' tasks');
    };

    $scope.isSelected = function(task) {
      return $scope.taskList.selection.indexOf(task) != -1;
    };

    $scope.selectTask = function (task) {
      var index = $scope.taskList.selection.indexOf(task);

      if (task.selected === true) {
        if (index === -1) {
          $scope.taskList.selection.push(task);
        }
        return;
      }

      if (task.selected === false) {
        $scope.taskList.selection.splice(index, 1);

        if ($scope.allTasksSelected === true) {
          $scope.allTasksSelected = false;
        }
        return;
      }
    };

    $scope.selectAllTasks = function(allTasksSelected) {
      angular.forEach($scope.taskList.tasks, function (task) {
        task.selected = allTasksSelected;
        $scope.selectTask(task);
      });
    };

    $scope.bpmn = { };
    $scope.showing = false;

    $scope.isDiagramActive = function(task) {
      // return $scope.showing;
      return $scope.bpmn.task === task;
    };

    $scope.toggleShowDiagram = function (task /*, index */) {
      var diagram = $scope.bpmn.diagram,
          oldTask = $scope.bpmn.task;

      $scope.bpmn = {};

      if (diagram) {
        // destroy old diagram
        diagram.clear();

        if (task === oldTask) {
          $scope.showing = false;
          return;
        }
      }
      else if ($scope.showing) {
        $scope.showing = false;
        return;
      }

      $scope.showing = true;
      $scope.bpmn.task = task;

      EngineApi.getProcessDefinitions().xml({ id : task.processDefinitionId }).$then(function (result) {
        var diagram = $scope.bpmn.diagram,
            xml = result.data.bpmn20Xml;

        if (diagram) {
          diagram.clear();
        }

        var $diagramEl = $('#diagram');
        var width = $diagramEl.width();

        try {
          diagram = new Bpmn().render(xml, {
            diagramElement : 'diagram',
            width: width,
            height: 400
          });

          diagram.annotation(task.taskDefinitionKey).addClasses([ 'bpmn-highlight' ]);

          $scope.bpmn.diagram = diagram;
        }
        catch (up) {
          $diagramEl
            .html('<div class="alert alert-error diagram-rendering-error">Unable to render process diagram.</div>');
        }
      });
    };
  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/overview', {
      templateUrl: require.toUrl('./app/tasklist/pages/overview.html'),
      controller: OverviewController,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      }
    });
  }];

  module
    .config(RouteConfig)
    .controller('OverviewOverviewController', OverviewController);
});
