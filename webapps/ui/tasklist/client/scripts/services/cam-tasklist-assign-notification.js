  'use strict';
  module.exports = ['camAPI', 'Notifications', '$translate',
  function(camAPI,   Notifications,   $translate) {
    var Task = camAPI.resource('task');
    /**
     * Search for tasks which are assigned to the user and display a notification containin a list of these tasks
     *
     * @param {Object} params
     * @param {String} [params.assignee]              The name of the user for which the tasks should be retrieved
     * @param {String} [params.processInstanceId]     The ID of the process instance.
     * @param {String} [params.caseInstanceId]        The ID of the case instance.
     */
    return function(params) {
      if(!params.assignee || !(params.processInstanceId || params.caseInstanceId)) {
        return;
      }
      Task.list(params, function(err, data) {
        if(data._embedded.task.length > 0) {
          var msg = '';
          for(var task, i = 0; (task = data._embedded.task[i]); i++) {
            msg += '<a ng-href="#/?task='+ task.id +'" ng-click="removeNotification(notification)">'+task.name+'</a>, ';
          }
          $translate(params.processInstanceId ? 'ASSIGN_NOTE_PROCESS' : 'ASSIGN_NOTE_CASE').then(function(translated) {
            Notifications.addMessage({
              duration: 16000,
              status: translated,
              unsafe: true,
              message: msg.slice(0,-2)
            });
          });
        }
      });
    };
  }];
