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
  function(
    $scope,
    $translate,
    Notifications,
    camAPI
  ) {

    var Task = camAPI.resource('task');

    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : '')
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
      Task.createComment($scope.comment.task.id, $scope.comment.message, function(err) {
        if (err) {
          return errorNotification('COMMENT_SAVE_ERROR', err);
        }

        successNotification('COMMENT_SAVE_SUCCESS');
        //$scope.$emit('tasklist.filter.saved');
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

    function open(task) {
      $scope.comment = {
        message: '',
        task: task
      };
      $modal.open({
        scope: $scope,
        //TODO: extract filter edit modal class to super style sheet
        windowClass: 'filter-edit-modal',
        size: 'lg',
        template: template,
        controller: commentCreateModalCtrl
      });
    }

    $scope.createComment = function(task) {
      open(task);
    };
  }];
});
