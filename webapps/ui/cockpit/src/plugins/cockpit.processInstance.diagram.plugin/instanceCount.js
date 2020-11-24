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

import React from "react";
import ReactDOM from "react-dom";
import { DiagramBadge } from "components";

import "./instanceCount.scss";

const getBadges = (instanceCount, incidentCount) => {
  const div = document.createElement("div");
  const portal = ReactDOM.createPortal(
    <div className="instanceCount">
      <DiagramBadge.InstancesBadge count={instanceCount} />
      <DiagramBadge.IncidentsBadge count={incidentCount} />
    </div>,
    div
  );

  return [div, portal];
};

export default function addInstanceCount(
  viewer,
  activityIdToInstancesMap,
  activityIdToIncidentsMap
) {
  const overlays = viewer.get("overlays");
  const elementRegistry = viewer.get("elementRegistry");

  const elements = [];
  elementRegistry.forEach(el => {
    if (activityIdToInstancesMap[el.id] || activityIdToIncidentsMap[el.id]) {
      const [HTML, portal] = getBadges(
        (activityIdToInstancesMap[el.id] || []).length,
        (activityIdToIncidentsMap[el.id] || []).length
      );

      overlays.add(el, {
        position: {
          bottom: 0,
          left: 0
        },
        show: {
          minZoom: -Infinity,
          maxZoom: +Infinity
        },
        html: HTML
      });
      elements.push(portal);
    }
  });

  return elements;
}
