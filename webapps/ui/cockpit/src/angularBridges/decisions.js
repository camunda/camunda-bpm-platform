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

import angular from "angular";
import directive from "../legacy/client/scripts/pages/decisions";

export default function() {
  var ngModule = angular.module("cam.cockpit.decisions", []);

  ngModule.directive("camCockpitDecisions", directive);

  const node = document.createElement("div");
  node.innerHTML = `  <div
  cam-breadcrumbs-panel
  divider="&raquo;"
  ng-cloak
  class="breadcrumbs-panel"></div>
  <div cam-cockpit-decisions></div>
  <div notifications-panel class="page-notifications"></div>
  `;
  node.className = "ctn-main";

  return { node, module: ngModule };
}
