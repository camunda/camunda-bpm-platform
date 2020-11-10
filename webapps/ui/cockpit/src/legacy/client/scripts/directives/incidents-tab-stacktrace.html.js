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

module.exports = `<!-- # CE - camunda-bpm-webapp/ui/cockpit/client/scripts/directives/incidents-tab-stacktrace.html -->
<div class="modal-header">
    <h3>{{ 'PLUGIN_INCIDENTS_TAB_STACKTRACE_HEADER' | translate }}</h3>
</div>

<div class="modal-body">
  <div class="form-group">
    <label for="value"
            cam-widget-clipboard="variable.value"
            no-tooltip
            class="hovered">
    {{ 'PLUGIN_INCIDENTS_TAB_STACKTRACE_COPY' | translate }}
    </label>
    <textarea ng-model="variable.value"
              id="value"
              rows="20"
              class="form-control cam-string-variable vertical-resize"
              ng-readonly="readonly"
              ng-mouseenter="toggleHover('var-value')"
              ng-mouseleave="toggleHover()"></textarea>
  </div>
  <span><a target="_blank" rel="noopener noreferrer" ng-href="{{variable.url}}">
    {{ 'PLUGIN_INCIDENTS_TAB_STACKTRACE_OPEN_NEW_TAB' | translate }}
  </a></span>
</div>

<div class="modal-footer">
  <button class="btn btn-default"
          ng-click="$dismiss('close')">
    {{ 'CAM_WIDGET_STRING_DIALOG_LABEL_CLOSE' | translate }}
  </button>
</div>
<!-- / CE - camunda-bpm-webapp/ui/cockpit/client/scripts/directives/incidents-tab-stacktrace.html -->`;
