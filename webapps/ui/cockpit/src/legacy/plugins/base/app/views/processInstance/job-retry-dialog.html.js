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

module.exports = `<!-- # CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/job-retry-dialog.html -->
<div class="modal-header">
  <h3>{{ 'PLUGIN_JOB_RETRY_LEGEND_1' | translate }}</h3>
</div>

<div class="job-retry modal-body">
  <div notifications-panel></div>

  <div ng-hide="status === 'finished' || status === 'failed'">
    <p>{{ 'PLUGIN_JOB_RETRY_LEGEND_2' | translate }}</p>
    <p>{{ 'PLUGIN_JOB_RETRY_LEGEND_3' | translate }}</p>
  </div>

  <div ng-show="status === 'finished'">
    {{ 'PLUGIN_JOB_RETRY_LEGEND_4' | translate }}
  </div>

  <div ng-show="status === 'failed'">
    {{ 'PLUGIN_JOB_RETRY_LEGEND_5' | translate }}
  </div>

</div>

<div class="modal-footer">
  <button class="btn btn-default"
          ng-click="close(status)"
          ng-disabled="status === 'performing'"
          ng-hide="status === 'finished' || status === 'failed'">
    {{ 'PLUGIN_JOB_RETRY_BTN_CLOSE' | translate }}
  </button>

  <button type="submit"
          class="btn btn-primary"
          ng-click="incrementRetry()"
          ng-hide="status === 'finished' || status === 'failed'"
          ng-disabled="status === 'performing'">
    {{ 'PLUGIN_JOB_RETRY_BTN_RETRY' | translate }}
  </button>

  <button class="btn btn-primary"
          ng-click="close(status)"
          ng-show="status === 'finished' || status === 'failed'">
    {{ 'PLUGIN_JOB_RETRY_BTN_OK' | translate }}
  </button>
</div>
<!-- / CE - camunda-bpm-webapp/webapp/src/main/resources-plugin/base/app/views/processInstance/job-retry-dialog.html -->
`;
