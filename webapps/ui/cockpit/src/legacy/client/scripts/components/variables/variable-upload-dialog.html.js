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

module.exports = `<!-- # CE - camunda-bpm-webapp/ui/cockpit/scripts/components/variables/variable-upload-modal.html -->
<div class="modal-header">
  <h3>{{ 'VARIABLE_UPLOAD_MODAL_TITLE' | translate }}</h3>
</div>

<!--[if lt IE 10]>

This feature is unsupported in your Browser.

<![endif]-->

<![if gt IE 9]>

<div class="modal-body variable-upload">

  <div notifications-panel></div>

  <div ng-show="status === 'beforeUpload'">

    <p>{{ 'VARIABLE_UPLOAD_SELECT_FILE' | translate }}:</p>

    <p>
      <input name="data"
             type="file"
             size="50"
             maxlength="100000"
             accept="*/*"
             onchange="angular.element(this).scope().setFile(this)" />
    </p>

  </div>

  <div ng-show="status === 'performUpload'"
       translate="VARIABLE_UPLOAD_PROGRESS"
       translate-values="{progress: progress}">
  </div>

</div>

<div class="modal-footer">
  <button class="btn btn-default"
          ng-click="$dismiss()"
          ng-hide="status === 'uploadSuccess' || status === 'uploadFailed'">
    {{ 'VARIABLE_UPLOAD_CLOSE' | translate }}
  </button>

  <button class="btn btn-primary"
          ng-click="$close()"
          ng-show="status === 'uploadSuccess' || status === 'uploadFailed'">
    {{ 'VARIABLE_UPLOAD_OK' | translate }}
  </button>

  <button class="btn btn-primary"
          ng-click="upload()"
          ng-disabled="status !== 'beforeUpload'"
          ng-hide="status === 'uploadSuccess' || status === 'uploadFailed'">
    {{ 'VARIABLE_UPLOAD_UPLOAD' | translate }}
  </button>
</div>

<![endif]>

<!-- / CE - camunda-bpm-webapp/ui/cockpit/scripts/components/variables/variable-upload-modal.html -->
`;
