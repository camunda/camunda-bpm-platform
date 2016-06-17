  'use strict';

  module.exports = [
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
            exclusive: true,
            scope: $scope
          });
        });
      }

      $scope.submit = function() {
        Task.createComment(task.id, $scope.comment.message, function(err) {
          if (err) {
            return errorNotification('COMMENT_SAVE_ERROR', err);
          }

          $scope.$close();
        });
      };
    }];
