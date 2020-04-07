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

const apiUrl = "/camunda/api/engine/engine/default/";

export default {
  id: "vanillaJS Plugin",
  pluginPoint: "cockpit.dashboard",
  priority: 5,
  render: container => {
    const header = document.createElement("h1");
    header.innerHTML = "Open Incidents";

    const mainContent = document.createElement("div");
    mainContent.innerHTML = "Loading...";

    container.appendChild(header);
    container.appendChild(mainContent);

    fetch(apiUrl + "incident/?maxResults=500").then(async res => {
      const result = await res.json();
      if (!result.length) {
        mainContent.innerHTML = "No incidents";
        return;
      }

      const table = document.createElement("table");
      table.style = "table-layout: fixed; width: 100%;";

      result.forEach(element => {
        const row = document.createElement("tr");

        const time = document.createElement("td");
        time.innerHTML = new Date(element.incidentTimestamp).toLocaleString();

        const message = document.createElement("td");
        message.innerHTML = element.incidentMessage;

        const process = document.createElement("td");
        const link = document.createElement("a");
        if (element.processInstanceId) {
          link.href = "#/process-instance/" + element.processInstanceId;
          link.innerText = "Process Instance";
        } else {
          link.href = "#/process-definition/" + element.processDefinitionId;
          link.innerText = "Process Definition";
        }
        process.appendChild(link);

        row.appendChild(time);
        row.appendChild(message);
        row.appendChild(process);

        table.appendChild(row);
      });

      mainContent.innerHTML = "";
      mainContent.appendChild(table);
    });
  }
};
