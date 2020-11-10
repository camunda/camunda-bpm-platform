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

module.exports = `<div uib-alert class="alert alert-danger"
     ng-if="error">
  <strong>{{ 'CAM_WIDGET_DMN_VIEWER_ERROR' | translate }}</strong><br/>
  {{ error.message }}
</div>

<div ng-show="!error"
     ng-if="!loaded && !disableLoader"
     class="placeholder-container">
  <div class="placeholder-content">
    {{ 'CAM_WIDGET_DMN_VIEWER_LOADING' | translate }}<br />
    <span class="glyphicon glyphicon-refresh animate-spin"></span>
  </div>
</div>

<div ng-show="!error"
     ng-class="{
      'grab-cursor': isDrd && !grabbing,
      'cursor-move': isDrd && grabbing
     }"
     class="table-holder">
</div>

<div ng-if="!error && !disableNavigation && isDrd">
  <div class="navigation zoom">
    <button class="btn btn-default in"
            title="zoom in"
            ng-click="zoomIn()">
      <span class="glyphicon glyphicon-plus"></span>
    </button>
    <button class="btn btn-default out"
            title="zoom out"
            ng-click="zoomOut()">
      <span class="glyphicon glyphicon-minus"></span>
    </button>
  </div>

  <div class="navigation reset">
    <button class="btn btn-default"
            title="reset zoom"
            ng-click="resetZoom()">
      <span class="glyphicon glyphicon-screenshot"></span>
    </button>
  </div>
</div>
`;
