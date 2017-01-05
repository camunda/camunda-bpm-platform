'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-tasks.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');
var $ = require('jquery');

module.exports = [function() {

  return {

    restrict: 'A',
    scope: {
      tasklistData: '='
    },

    template: template,

    controller: [
      '$element',
      '$scope',
      '$location',
      'search',
      'Views',
      '$timeout',
      function(
        $element,
        $scope,
        $location,
        search,
        Views,
        $timeout
      ) {
        function updateSilently(params) {
          search.updateSilently(params);
        }

        $scope.expanded = {};
        $scope.toggle = function(delta, $event) {
          $scope.expanded[delta] = !$scope.expanded[delta];
          if ($event && $event.preventDefault) {
            $event.preventDefault();
          }
          $event.stopPropagation();
        };

        $scope.pageNum = 1;
        $scope.pageSize = null;
        $scope.totalItems = 0;
        $scope.now = (new Date()).toJSON();

        $scope.filterProperties = null;

        var tasksData = $scope.tasklistData.newChild($scope);

        $scope.query = {};

        var assignees = $scope.assignees = {};
        var parseAssignees = function(assigneeList) {
          for(var i = 0; i < assigneeList.length; i++) {
            $scope.assignees[assigneeList[i].id] = assigneeList[i];
          }
        };



        function updateShutters() {
          $('.task-card-details').each(function() {
            var h = 0;
            $('view', this).each(function() {
              h += this.clientHeight;
            });

            if (h <= 20) {
              $(this).addClass('no-shutter');
            }
          });
        }

        var postLoadingJobs = [];
        var executePostLoadingJobs = function() {
          postLoadingJobs.push(function() {
            $timeout(updateShutters);
          });
          postLoadingJobs.forEach(function(job) {
            job();
          });
          postLoadingJobs = [];
          $scope.expanded = {};
        };


        /**
         * observe the list of tasks
         */
        $scope.state = tasksData.observe('taskList', function(taskList) {
          $scope.totalItems = taskList.count;
          $scope.tasks = taskList._embedded.task;
          if(taskList._embedded.assignee) {
            parseAssignees(taskList._embedded.assignee);
          }
          executePostLoadingJobs();
        });

        $scope.$on('shortcut:focusList', function() {
          var el = document.querySelector('[cam-tasks] .tasks-list li:first-child a');
          if(el) {
            el.focus();
          }
        });

        $scope.assigneeDisplayedName = function(task) {
          var _assignee = assignees[task.assignee] || {};
          var hasFirstLastName = _assignee.firstName || _assignee.lastName;
          if (hasFirstLastName) {
            return ((_assignee.firstName || '') + ' ' + (_assignee.lastName || ''));
          }
          else if (!(assignees[task.assignee] && hasFirstLastName)) {
            return task.assignee;
          }
          return '&lt;nobody&gt;';
        };

        /**
         * observe the task list query
         */
        tasksData.observe('taskListQuery', function(taskListQuery) {
          if (taskListQuery) {
            // parse pagination properties from query
            $scope.query = angular.copy(taskListQuery);
            $scope.pageSize = $scope.query.maxResults;
            // Sachbearbeiter starts counting at '1'
            $scope.pageNum = ($scope.query.firstResult / $scope.pageSize) + 1;
          }
        });

        tasksData.observe('taskId', function(taskId) {
          $scope.currentTaskId = taskId.taskId;
        });

        /**
         * Observes the properties of the current filter.
         * Used to retrieve information about variables displayed on a task.
         */
        tasksData.observe(['currentFilter', function(currentFilter) {
          if (currentFilter) {
            $scope.filterProperties = currentFilter !== null ? currentFilter.properties : null;
          }
        }]);

        $scope.focus = function($event, task) {
          if ($event) {
            $event.preventDefault();
          }

          var taskId = task.id;
          tasksData.set('taskId', { 'taskId' : taskId });
          $scope.currentTaskId = taskId;

          var searchParams = $location.search() || {};
          searchParams.task = taskId;
          updateSilently(searchParams);

          var el = document.querySelector('[cam-tasks] .tasks-list .task [href*="#/?task=' + taskId + '"]');
          if(el) {
            el.focus();
          }
        };

        var selectNextTask = function() {
          for(var i = 0; i < $scope.tasks.length - 1; i++) {
            if($scope.tasks[i].id === $scope.currentTaskId) {
              return $scope.focus(null, $scope.tasks[i+1]);
            }
          }
          if($scope.pageNum < Math.ceil($scope.totalItems / $scope.pageSize)) {
            $scope.pageNum++;
            $scope.pageChange();
            postLoadingJobs.push(function() {
              // wait until the html is applied so you can focus the html element
              $timeout(function() {
                $scope.focus(null, $scope.tasks[0]);
              });
            });
          }
        };

        var selectPreviousTask = function() {
          for(var i = 1; i < $scope.tasks.length; i++) {
            if($scope.tasks[i].id === $scope.currentTaskId) {
              return $scope.focus(null, $scope.tasks[i-1]);
            }
          }
          if($scope.pageNum > 1) {
            $scope.pageNum--;
            $scope.pageChange();
            postLoadingJobs.push(function() {
              // wait until the html is applied so you can focus the html element
              $timeout(function() {
                $scope.focus(null, $scope.tasks[$scope.tasks.length - 1]);
              });
            });
          }
        };

        $scope.handleKeydown = function($event) {
          if($event.keyCode === 40) {
            $event.preventDefault();
            selectNextTask($event);
          }
          else if($event.keyCode === 38) {
            $event.preventDefault();
            selectPreviousTask();
          }
          // wait for angular to update the classes and scroll to the newly selected task
          $timeout(function() {
            var $el = $($event.target).find('li.active')[0];
            if ($el) {
              $el.scrollIntoView(false);
            }
          });
        };

        $scope.getHrefUrl = function(task) {
          var href = '#/?task=' + task.id;
          var detailsTab = $location.search().detailsTab;
          if (detailsTab) {
            href = href + '&detailsTab=' + detailsTab;
          }

          return href;
        };

        $scope.cardPluginVars = { read: [ 'task', 'filterProperties' ] };
        $scope.cardPlugins = Views.getProviders({
          component: 'tasklist.card'
        });

        /**
         * invoked when pagination is changed
         */
        $scope.pageChange = function() {
          // update query
          updateSilently({
            page:  $scope.pageNum
          });
          tasksData.changed('taskListQuery');
        };

        $scope.resetPage = function() {
          updateSilently({
            page: 1
          });
          tasksData.changed('taskListQuery');
        };

      }]
  };
}];
