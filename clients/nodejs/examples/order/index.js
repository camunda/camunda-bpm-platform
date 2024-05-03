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

import {
  Client,
  logger,
  Variables,
  File,
} from "camunda-external-task-client-js";

// configuration for the Client:
//  - 'baseUrl': url to the Process Engine
//  - 'logger': utility to automatically log important events
const config = {
  baseUrl: "http://localhost:8080/engine-rest",
  use: logger,
  usePriority: false,
  sorting: [
    {
      sortBy: Client.SortBy.CreateTime,
      sortOrder: Client.SortOrder.DESC,
    },
  ],
};

// create a Client instance with custom configuration
const client = new Client(config);

// susbscribe to the topic: 'invoiceCreator'
client.subscribe("invoiceCreator", async function ({ task, taskService }) {
  // Put your business logic
  // complete the task
  const date = new Date();
  const invoice = await new File({ localPath: "./assets/invoice.txt" }).load();
  const minute = date.getMinutes();
  const variables = new Variables().setAll({ invoice, date });

  // check if minute is even
  if (minute % 2 === 0) {
    // for even minutes, store variables in the process scope
    await taskService.complete(task, variables);
  } else {
    // for odd minutes, store variables in the task local scope
    await taskService.complete(task, null, variables);
  }
});
