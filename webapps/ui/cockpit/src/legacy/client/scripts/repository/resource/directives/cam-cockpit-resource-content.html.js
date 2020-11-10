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

module.exports = `<!-- # cockpit client/scripts/repository/resource/directives/cam-cockpit-resource-content.html -->
<div ng-if="binary || (resource && isImageResource(resource))"
     class="resource-wrapper">

  <div ng-if="isBpmnResource(resource)"
       cam-widget-bpmn-viewer
       diagram-data="binary"
       bpmn-js-conf="bpmnJsConf">
  </div>


  <div ng-if="isDmnResource(resource)"
       cam-widget-dmn-viewer
       enable-drd-navigation="true"
       xml="binary">
  </div>

  <div ng-if="isCmmnResource(resource)"
       cam-widget-cmmn-viewer
       diagram-data="binary">
  </div>

  <div ng-if="isImageResource(resource)"
       class="image-resource">
    <img ng-src="{{ imageLink(deployment, resource) }}">
  </div>


  <div ng-if="isUnkownResource(resource)"
       class="unkown-resource">
    <div cam-source
         name="resource.name"
         source="binary">
    </div>
  </div>

  <div>

</div>
<!-- / cockpit client/scripts/repository/resource/directives/cam-cockpit-resource-content.html -->
`;
