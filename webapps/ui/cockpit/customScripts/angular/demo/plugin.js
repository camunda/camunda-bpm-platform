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

let cb = el => console.error("No callback defined: ", el);
const diagramPlugin = {
  id: "Diagram Interaction",
  pluginPoint: "cockpit.processDefinition.diagram.plugin",
  priority: 5,

  render: (viewer, { processDefinitionId }) => {
    viewer.get("eventBus").on("element.click", event => {
      if (event.element.type.includes("Task")) {
        cb(event.element);
      } else {
        cb(false);
      }
    });
  }
};

const tabPlugin = {
  id: "Angular9 Plugin",
  pluginPoint: "cockpit.processDefinition.runtime.tab",
  label: "MyAngular",
  priority: 5,
  render: (container, { processDefinitionId }) => {
    container.innerHTML = `<activity-table id="myActivityTable" process-definition-id="${processDefinitionId}"></activity-table>`;
    cb = el => {
      if (el.id)
        document
          .getElementById("myActivityTable")
          .setAttribute("activity-id", el.id);
      else
        document
          .getElementById("myActivityTable")
          .removeAttribute("activity-id");
    };
  }
};

export default [tabPlugin, diagramPlugin];
