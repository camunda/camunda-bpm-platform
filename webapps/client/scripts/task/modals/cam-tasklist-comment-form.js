define([
  'angular',
  'camunda-bpm-sdk',
  'text!./cam-tasklist-comment-form.html'
], function(
  angular,
  camSDK,
  template
) {
  'use strict';
  var commentCreateModalCtrl = [
    '$scope',
    '$translate',
    'Notifications',
    'camAPI',
    'task',
  function(
    $scope,
    $translate,
    Notifications,
    camAPI,
    task
  ) {

    var Task = camAPI.resource('task');

    $scope.comment = { message: '' };

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : ''),
          exclusive: true
        });
      });
    }

    function successNotification(src) {
      $translate(src).then(function(translated) {
        Notifications.addMessage({
          duration: 3000,
          status: translated
        });
      });
    }

    $scope.submit = function() {
      Task.createComment(task.id, $scope.comment.message, function(err) {
        if (err) {
          return errorNotification('COMMENT_SAVE_ERROR', err);
        }

        successNotification('COMMENT_SAVE_SUCCESS');
        $scope.$close();
      });
    };
  }];


  return [
    '$modal',
    '$scope',
  function(
    $modal,
    $scope
  ) {

    var commentData = $scope.taskData.newChild($scope);

    function open(task) {
      $modal.open({
        // creates a child scope of a provided scope
        scope: $scope,
        //TODO: extract filter edit modal class to super style sheet
        windowClass: 'filter-edit-modal',
        size: 'lg',
        template: template,
        controller: commentCreateModalCtrl,
        resolve: {
          task: function() { return task; }
        }
      }).result.then(function() {

        commentData.changed('task');

      });
    }

    $scope.createComment = function(task) {
      open(task);
    };
  }];
});
