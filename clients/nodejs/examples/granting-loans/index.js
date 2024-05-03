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

import { Client, logger, Variables } from "camunda-external-task-client-js";

// configuration for the Client:
//  - 'baseUrl': url to the Process Engine
//  - 'logger': utility to automatically log important events
const config = {
  baseUrl: "http://localhost:8080/engine-rest",
  use: logger,
};

// create a Client instance with custom configuration
const client = new Client(config);

// create a handler for the task
const handler = async ({ task, taskService }) => {
  // get task variable 'defaultScore'
  const defaultScore = task.variables.get("defaultScore");

  // set process variable 'creditScores'
  const creditScores = [defaultScore, 9, 1, 4, 10];
  const processVariables = new Variables()
    .set("creditScores", creditScores)
    .set("bar", new Date());

  // complete the task
  try {
    await taskService.complete(task, processVariables);
    console.log("I completed my task successfully!!");
  } catch (e) {
    console.error(`Failed completing my task, ${e}`);
  }
};

// susbscribe to the topic 'creditScoreChecker' & provide the created handler
client.subscribe("creditScoreChecker", handler);

client.subscribe("requestRejecter", async ({ task, taskService }) => {
  console.log(task.variables.get("bar"));
  console.log(task.variables.get("creditScores"));
});
