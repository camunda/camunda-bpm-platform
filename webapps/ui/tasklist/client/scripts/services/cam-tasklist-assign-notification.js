/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
module.exports = [
  'camAPI',
  'Notifications',
  '$translate',
  function(camAPI, Notifications, $translate) {
    var Task = camAPI.resource('task');

    var escapeHtml = function(html) {
      var text = document.createTextNode(html);
      var div = document.createElement('div');
      div.appendChild(text);
      return div.innerHTML;
    };

    /**
     * Search for tasks which are assigned to the user and display a notification containin a list of these tasks
     *
     * @param {Object} params
     * @param {String} [params.assignee]              The name of the user for which the tasks should be retrieved
     * @param {String} [params.processInstanceId]     The ID of the process instance.
     * @param {String} [params.caseInstanceId]        The ID of the case instance.
     */
    return function(params) {
      if (
        !params.assignee ||
        !(params.processInstanceId || params.caseInstanceId)
      ) {
        return;
      }
      Task.list(params, function(err, data) {
        if (data._embedded.task.length > 0) {
          var msg = '';
          for (var task, i = 0; (task = data._embedded.task[i]); i++) {
            msg +=
              '<a href="#/?forceDisplayTask=true&task=' +
              task.id +
              '">' +
              escapeHtml(task.name || task.taskDefinitionKey) +
              '</a>, ';
          }
          $translate(
            params.processInstanceId
              ? 'ASSIGN_NOTE_PROCESS'
              : 'ASSIGN_NOTE_CASE'
          )
            .then(function(translated) {
              Notifications.addMessage({
                duration: 16000,
                status: translated,
                unsafe: true,
                message: msg.slice(0, -2)
              });
            })
            .catch(function() {});
        }
      });
    };
  }
];
