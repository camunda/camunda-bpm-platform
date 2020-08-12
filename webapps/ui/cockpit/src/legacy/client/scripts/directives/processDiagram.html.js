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

module.exports = `<div ng-if="diagramEnabled" style="position: relative; height: 100%">
  <div diagram-statistics-loader>
  </div>
  <div cam-widget-bpmn-viewer
    diagram-data='diagramData'
    key='{{key}}'
    control='control'
    on-load='onLoad()'
    on-click='onClick(element, $event)'
    on-mouse-enter='onMouseEnter(element, $event)'
    on-mouse-leave='onMouseLeave(element, $event)'
    bpmn-js-conf="bpmnJsConf"
    style='height: 100%'>
  </div>
</div>
<div ng-if="!diagramEnabled" class="placeholder-container">
  <div class="placeholder-content">
    <span class="glyphicon glyphicon-refresh animate-spin"></span>
    <span class="loading-text">Loading diagram</span>
  </div>
</div>
`;
