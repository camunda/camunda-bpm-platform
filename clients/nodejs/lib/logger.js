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

import chalk from "chalk";

/**
 * @returns a formatted success message
 */
const success = (message) => `${chalk.green("✓")} ${chalk.green(message)}`;

/**
 * @returns a formatted error message
 */
const error = (message) => `${chalk.red("✖")} ${chalk.red(message)}`;

const levels = {
  error: 0,
  warn: 1,
  info: 2,
  verbose: 3,
  debug: 4,
  silly: 5,
};

/**
 * logs various events from client
 * @param client
 */
const logger = (client, clientLogLevel) => {
  const log = (messageLogLevel, message) => {
    if (!message) {
      console.log(messageLogLevel);
      return;
    }

    if (levels[messageLogLevel] <= clientLogLevel) {
      console.log(message);
    }
  };

  switch (typeof clientLogLevel) {
    case "string":
      clientLogLevel = levels[clientLogLevel];
      break;
    case "number":
      break;
    default:
      clientLogLevel = levels["info"];
      break;
  }

  client.on("subscribe", (topic) => {
    log("info", success(`subscribed to topic ${topic}`));
  });

  client.on("unsubscribe", (topic) => {
    log("info", success(`unsubscribed from topic ${topic}`));
  });

  client.on("poll:start", () => {
    log("debug", "polling");
  });

  client.on("poll:stop", () => {
    log("debug", error("polling stopped"));
  });

  client.on("poll:success", (tasks) => {
    const output = success(`polled ${tasks.length} tasks`);
    log("debug", output);
  });

  client.on("poll:error", (e) => {
    const output = error(`polling failed with ${e}`);
    log("error", output);
  });

  client.on("complete:success", ({ id }) => {
    log("info", success(`completed task ${id}`));
  });

  client.on("complete:error", ({ id }, e) => {
    log("error", error(`couldn't complete task ${id}, ${e}`));
  });

  client.on("handleFailure:success", ({ id }) => {
    log("info", success(`handled failure of task ${id}`));
  });

  client.on("handleFailure:error", ({ id }, e) => {
    log("error", error(`couldn't handle failure of task ${id}, ${e}`));
  });

  client.on("handleBpmnError:success", ({ id }) => {
    log("info", success(`handled BPMN error of task ${id}`));
  });

  client.on("handleBpmnError:error", ({ id }, e) => {
    log("error", error(`couldn't handle BPMN error of task ${id}, ${e}`));
  });

  client.on("extendLock:success", ({ id }) => {
    log("info", success(`handled extend lock of task ${id}`));
  });

  client.on("extendLock:error", ({ id }, e) => {
    log("error", error(`couldn't handle extend lock of task ${id}, ${e}`));
  });

  client.on("unlock:success", ({ id }) => {
    log("info", success(`unlocked task ${id}`));
  });

  client.on("unlock:error", ({ id }, e) => {
    log("error", error(`couldn't unlock task ${id}, ${e}`));
  });

  client.on("lock:success", ({ id }) => {
    log("info", success(`locked task ${id}`));
  });

  client.on("lock:error", ({ id }, e) => {
    log("error", error(`couldn't lock task ${id}, ${e}`));
  });
};

/**
 * Returns Logger with configured log-level
 * @param level
 */
const level = (level) => {
  return function (client) {
    logger(client, level);
  };
};

// export logger & attach to it success and error methods
export default Object.assign(logger, { success, error, level });
