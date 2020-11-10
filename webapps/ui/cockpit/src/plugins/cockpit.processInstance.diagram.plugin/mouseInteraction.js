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

let filter = {};
let setFilter = () => {};

export function addMouseInteraction(
  viewer,
  activityIdToInstancesMap,
  activityIdToIncidentsMap
) {
  const eventBus = viewer.get("eventBus");
  const canvas = viewer.get("canvas");

  function isSelectable(element) {
    return (
      activityIdToInstancesMap[element.id] ||
      activityIdToIncidentsMap[element.id]
    );
  }

  eventBus.on("element.click", ev => {
    const element = ev.element;

    if (isSelectable(element)) {
      selectRunningInstances(element, ev.originalEvent);
    } else {
      setFilter({});
    }
  });
  eventBus.on("element.hover", ev => {
    const element = ev.element;
    if (isSelectable(element)) {
      canvas.addMarker(element.businessObject.id, "selectable");
      canvas.addMarker(element.businessObject.id, "highlight");
    }
  });
  eventBus.on("element.out", ev => {
    if ((filter.activityIds || []).includes(ev.element.id)) return;

    canvas.removeMarker(ev.element.businessObject.id, "highlight");
  });

  function selectRunningInstances(element, event) {
    const newFilter = { ...filter };
    const ctrl = event.ctrlKey;
    const activityId = element.id;
    let activityIds = newFilter.activityIds || [];
    const idx = activityIds.indexOf(activityId);
    const selected = idx !== -1;
    const multiInstance = element.businessObject.multiInstance;

    if (!activityId) {
      activityIds = null;
    } else {
      if (ctrl) {
        if (selected) {
          activityIds.splice(idx, 1);
          if (multiInstance) {
            activityIds.splice(
              activityIds.indexOf(activityId + "#multiInstanceBody"),
              1
            );
          }
        } else {
          activityIds.push(activityId);
          if (multiInstance) {
            activityIds.push(activityId + "#multiInstanceBody");
          }
        }
      } else {
        activityIds = [activityId];
        if (multiInstance) {
          activityIds.push(activityId + "#multiInstanceBody");
        }
      }
    }

    const instanceIds = activityIds
      .map(id => {
        return activityIdToInstancesMap[id].map(instance => instance.id);
      })
      .flat();

    newFilter.activityIds = activityIds;
    newFilter.activityInstanceIds = instanceIds;

    setFilter(newFilter);
  }
}

export function configureFilter(newFilter, newSetFilter) {
  filter = newFilter;
  setFilter = newSetFilter;
}
