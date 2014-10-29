define([], function() {
  'use strict';
  return ['camAPI', 'Notifications', '$translate',
  function(camAPI,   Notifications,   $translate) {
    var Task = camAPI.resource('task');

    return function(processInstanceId, assignee) {
      Task.list({
        processInstanceId : processInstanceId,
        assignee : assignee
      },function(err, data) {
        if(data._embedded.task.length > 0) {
          var msg = "";
          for(var task, i = 0; !!(task = data._embedded.task[i]); i++) {
            msg += '<a ng-href="#/?task='+ task.id +'" ng-click="removeNotification(notification)">'+task.name+'</a>, ';
          }
          $translate('ASSIGN_NOTE').then(function(translated) {
            Notifications.addMessage({
              duration: 16000,
              status: translated,
              message: msg.slice(0,-2)
            });
          });
        }
      });
    };
  }];
});
