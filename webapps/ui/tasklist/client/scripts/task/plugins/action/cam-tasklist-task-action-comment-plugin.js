'use strict';
var fs = require('fs');

var addCommentTemplate = fs.readFileSync(__dirname + '/cam-tasklist-task-action-comment-plugin.html', 'utf8');
var addCommentFormTemplate = fs.readFileSync(__dirname + '/modals/cam-tasklist-comment-form.html', 'utf8');

var Controller = [
  '$scope',
  '$modal',
  function(
    $scope,
    $modal
  ) {

    var commentData = $scope.taskData.newChild($scope);

    commentData.observe('task', function(task) {
      $scope.task = task;
    });

    $scope.createComment = function() {
      $modal.open({
        // creates a child scope of a provided scope
        scope: $scope,
        //TODO: extract filter edit modal class to super style sheet
        windowClass: 'filter-edit-modal',
        size: 'lg',
        template: addCommentFormTemplate,
        controller: 'camCommentCreateModalCtrl',
        resolve: {
          task: function() { return $scope.task; }
        }
      }).result.then(function() {
        commentData.changed('task');
        document.querySelector('.createCommentLink').focus();
      }, function() {
        document.querySelector('.createCommentLink').focus();
      });

    };

  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('tasklist.task.action', {
    id: 'task-action-comment',
    template: addCommentTemplate,
    controller: Controller,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
