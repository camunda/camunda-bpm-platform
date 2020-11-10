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

import $ from "jquery";
import translate from "utils/translation";
import { abbreviateNumber } from "utils/filters";

const getBadges = (instanceCount = "", incidentCount = "") => {
  const template = `<div class="activity-bottom-left-position instances-overlay">
<span class="badge instance-count">${instanceCount}</span>
<span class="badge badge-important instance-incidents">${incidentCount}</span>
</div>`;

  const node = $(template);

  // Add Bootstrap Tooltip using JQuery
  node.find(".instance-count").tooltip({
    container: "body",
    title: translate("PLUGIN_ACTIVITY_INSTANCE_RUNNING_ACTIVITY_INSTANCES"),
    placement: "top",
    animation: false
  });
  node.find(".instance-incidents").tooltip({
    container: "body",
    title: translate("PLUGIN_ACTIVITY_INSTANCE_OPEN_INCIDENTS"),
    placement: "top",
    animation: false
  });

  return node;
};

export default function addInstanceCount(
  viewer,
  activityIdToInstancesMap,
  activityIdToIncidentsMap
) {
  const overlays = viewer.get("overlays");
  const elementRegistry = viewer.get("elementRegistry");

  elementRegistry.forEach(el => {
    if (activityIdToInstancesMap[el.id] || activityIdToIncidentsMap[el.id]) {
      overlays.add(el, {
        position: {
          bottom: 0,
          left: 0
        },
        show: {
          minZoom: -Infinity,
          maxZoom: +Infinity
        },
        html: getBadges(
          abbreviateNumber((activityIdToInstancesMap[el.id] || []).length) ||
            "",
          abbreviateNumber((activityIdToIncidentsMap[el.id] || []).length) || ""
        )
      });
    }
  });
}
