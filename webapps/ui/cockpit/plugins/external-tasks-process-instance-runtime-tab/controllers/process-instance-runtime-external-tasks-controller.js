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
  '$scope',
  'exposeScopeProperties',
  'externalTasks',
  'observeBpmnElements',
  '$translate',
  'localConf',
  ProcessInstanceRuntimeTab
];

function ProcessInstanceRuntimeTab(
  $scope,
  exposeScopeProperties,
  externalTasks,
  observeBpmnElements,
  $translate,
  localConf
) {
  exposeScopeProperties($scope, this, ['processInstance', 'processData']);
  observeBpmnElements($scope, this);

  this.scope = $scope;
  this.translate = $translate;
  this.localConf = localConf;
  this.externalTasks = externalTasks;
  this.query = null;
  this.pages = null;
  this.activityIds = null;

  this.sorting = this._loadLocal({sortBy: 'taskPriority', sortOrder: 'asc'});

  this.headColumns = [
    {
      class: 'state',
      request: '',
      sortable: false,
      content: this.translate.instant('PLUGIN_EXT_EXTERNAL_TASK_ID')
    },
    {
      class: 'activity',
      request: '',
      sortable: false,
      content: this.translate.instant('PLUGIN_EXT_ACTIVITY')
    },
    {
      class: 'retries',
      request: '',
      sortable: false,
      content: this.translate.instant('PLUGIN_EXT_RETRIES')
    },
    {
      class: 'worker-id',
      request: '',
      sortable: false,
      content: this.translate.instant('PLUGIN_EXT_WORKER_ID')
    },
    {
      class: 'expiration',
      request: '',
      sortable: false,
      content: this.translate.instant('PLUGIN_EXT_LOCK_EXPIRATION_TIME')
    },
    {
      class: 'topic',
      request: '',
      sortable: false,
      content: this.translate.instant('PLUGIN_EXT_TOPIC')
    },
    {
      class: 'priority',
      request: 'taskPriority',
      sortable: true,
      content: this.translate.instant('PLUGIN_EXT_PRIORITY')
    }
  ];
}

ProcessInstanceRuntimeTab.prototype._loadLocal = function(defaultValue) {
  return this.localConf.get('sortExternalTaskRuntimeTab', defaultValue);
};

ProcessInstanceRuntimeTab.prototype._saveLocal = function(sorting) {
  return this.localConf.set('sortExternalTaskRuntimeTab', sorting);
};

ProcessInstanceRuntimeTab.prototype.onSortChange = function(sorting) {
  this.sorting = sorting;
  this._saveLocal(this.sorting);
  this.onLoad();
};

ProcessInstanceRuntimeTab.prototype.onLoad = function(pages, activityIds) {
  this.pages = pages || this.pages;
  this.activityIds = activityIds || this.activityIds;

  return this.externalTasks
    .getActiveExternalTasksForProcess(
      this.processInstance.id,
      this.pages,
      this.sorting,
      this.getActivityParams(this.activityIds)
    )
    .then(
      function(data) {
        this.tasks = data.list;

        setTimeout(() => {
          this.scope.$apply();
        }, 0);

        return data;
      }.bind(this)
    )
    .catch(function() {});
};

ProcessInstanceRuntimeTab.prototype.getActivityParams = function(activityIds) {
  if (!activityIds || !activityIds.length) {
    return {};
  }

  return {
    activityIdIn: activityIds
  };
};
