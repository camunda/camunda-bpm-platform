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

import events from "events";

import EngineService from "./__internal/EngineService.js";
import TaskService from "./TaskService.js";
import Variables from "./Variables.js";

import {
  MISSING_BASE_URL,
  ALREADY_REGISTERED,
  MISSING_HANDLER,
  WRONG_INTERCEPTOR,
  WRONG_MIDDLEWARES,
  WRONG_SORTING,
  WRONG_SORTING_SORT_ORDER,
  WRONG_SORTING_SORT_BY,
} from "./__internal/errors.js";

import {
  isFunction,
  isArrayOfFunctions,
  isUndefinedOrNull,
} from "./__internal/utils.js";

const defaultOptions = {
  workerId: "some-random-id",
  maxTasks: 10,
  usePriority: true,
  interval: 300,
  lockDuration: 50000,
  autoPoll: true,
};

/**
 * @throws Error
 * @param customOptions
 * @param customOptions.baseUrl: baseUrl to api | REQUIRED
 * @param customOptions.workerId
 * @param customOptions.maxTasks
 * @param customOptions.interval
 * @param customOptions.lockDuration
 * @param customOptions.autoPoll
 * @param customOptions.asyncResponseTimeout
 * @param customOptions.interceptors
 * @param customOptions.usePriority
 * @param customOptions.maxParallelExecutions
 * @param customOptions.sorting
 * @constructor
 */
class Client extends events {
  static SortBy = Object.freeze({
    CreateTime: "createTime",
  });

  static SortOrder = Object.freeze({
    ASC: "asc",
    DESC: "desc",
  });

  constructor(customOptions) {
    super();

    /**
     * Bind member methods
     */
    this.sanitizeOptions = this.sanitizeOptions.bind(this);
    this.start = this.start.bind(this);
    this.poll = this.poll.bind(this);
    this.subscribe = this.subscribe.bind(this);
    this.executeTask = this.executeTask.bind(this);
    this.stop = this.stop.bind(this);

    /**
     *  Initialize data
     */
    this.activeTasksCount = 0;
    this.sanitizeOptions(customOptions);
    this.topicSubscriptions = {}; // Map which contains all subscribed topics
    this.engineService = new EngineService(this.options);
    this.taskService = new TaskService(this, this.engineService);

    if (this.options.use) {
      this.options.use.forEach((f) => f(this));
    }

    if (this.options.autoPoll) {
      this.start();
    }
  }

  /**
   * @throws Error
   * @param customOptions
   */
  sanitizeOptions(customOptions) {
    this.options = { ...defaultOptions, ...customOptions };

    if (
      !customOptions ||
      !customOptions.baseUrl ||
      !customOptions.baseUrl.length
    ) {
      throw new Error(MISSING_BASE_URL);
    }

    const { interceptors, use } = customOptions;

    // sanitize request interceptors
    if (
      interceptors &&
      !isFunction(interceptors) &&
      !isArrayOfFunctions(interceptors)
    ) {
      throw new Error(WRONG_INTERCEPTOR);
    }

    if (isFunction(interceptors)) {
      this.options.interceptors = [interceptors];
    }

    // sanitize middlewares
    if (use && !isFunction(use) && !isArrayOfFunctions(use)) {
      throw new Error(WRONG_MIDDLEWARES);
    }

    if (isFunction(use)) {
      this.options.use = [use];
    }

    if (this.options.sorting) {
      // sanitize sorting
      if (!Array.isArray(this.options.sorting)) {
        throw new Error(WRONG_SORTING);
      }

      // sanitize sorting.sortBy
      const sortByPredicate = (sorting) =>
        !sorting.sortBy ||
        !Object.values(Client.SortBy).includes(sorting.sortBy);
      if (this.options.sorting.some(sortByPredicate)) {
        throw new Error(WRONG_SORTING_SORT_BY + Object.values(Client.SortBy));
      }

      // sanitize sorting.sortOrder
      const sortOrderPredicate = (sorting) =>
        !sorting.sortOrder ||
        !Object.values(Client.SortOrder).includes(sorting.sortOrder);
      if (this.options.sorting.some(sortOrderPredicate)) {
        throw new Error(
          WRONG_SORTING_SORT_ORDER + Object.values(Client.SortOrder)
        );
      }
    }
  }

  /**
   * Starts polling
   */
  start() {
    this._isPollAllowed = true;
    this.poll();
  }

  /**
   * Polls tasks from engine and executes them
   */
  async poll() {
    if (!this._isPollAllowed) {
      return;
    }

    this.emit("poll:start");

    const { topicSubscriptions, engineService, executeTask, poll } = this;
    const {
      maxTasks,
      usePriority,
      sorting,
      interval,
      asyncResponseTimeout,
      maxParallelExecutions,
    } = this.options;

    const requiredTasksCount = isUndefinedOrNull(maxParallelExecutions)
      ? maxTasks
      : Math.min(maxTasks, maxParallelExecutions - this.activeTasksCount);
    // If all previously received tasks are running now, reschedule polling
    if (requiredTasksCount <= 0) {
      return setTimeout(poll, interval);
    }

    // setup polling options
    let pollingOptions = {
      maxTasks: requiredTasksCount,
      usePriority: usePriority,
    };
    if (asyncResponseTimeout) {
      pollingOptions = { ...pollingOptions, asyncResponseTimeout };
    }
    if (sorting) {
      pollingOptions = { ...pollingOptions, sorting };
    }

    // if there are no topic subscriptions, reschedule polling
    if (!Object.keys(topicSubscriptions).length) {
      return setTimeout(poll, interval);
    }

    // collect topics that have subscriptions
    const topics = Object.entries(topicSubscriptions).map(
      ([
        topicName,
        {
          businessKey,
          lockDuration,
          variables,
          processDefinitionId,
          processDefinitionIdIn,
          processDefinitionKey,
          processDefinitionKeyIn,
          processDefinitionVersionTag,
          processVariables,
          tenantIdIn,
          withoutTenantId,
          localVariables,
          includeExtensionProperties,
        },
      ]) => {
        let topic = { topicName, lockDuration };

        if (!isUndefinedOrNull(variables)) {
          topic.variables = variables;
        }

        if (!isUndefinedOrNull(businessKey)) {
          topic.businessKey = businessKey;
        }

        if (!isUndefinedOrNull(processDefinitionId)) {
          topic.processDefinitionId = processDefinitionId;
        }

        if (!isUndefinedOrNull(processDefinitionIdIn)) {
          topic.processDefinitionIdIn = processDefinitionIdIn;
        }

        if (!isUndefinedOrNull(processDefinitionKey)) {
          topic.processDefinitionKey = processDefinitionKey;
        }

        if (!isUndefinedOrNull(processDefinitionKeyIn)) {
          topic.processDefinitionKeyIn = processDefinitionKeyIn;
        }

        if (!isUndefinedOrNull(processDefinitionVersionTag)) {
          topic.processDefinitionVersionTag = processDefinitionVersionTag;
        }

        if (!isUndefinedOrNull(processVariables)) {
          topic.processVariables = processVariables;
        }

        if (!isUndefinedOrNull(tenantIdIn)) {
          topic.tenantIdIn = tenantIdIn;
        }

        if (!isUndefinedOrNull(withoutTenantId)) {
          topic.withoutTenantId = withoutTenantId;
        }

        if (!isUndefinedOrNull(localVariables)) {
          topic.localVariables = localVariables;
        }

        if (!isUndefinedOrNull(includeExtensionProperties)) {
          topic.includeExtensionProperties = includeExtensionProperties;
        }

        return topic;
      }
    );

    const requestBody = { ...pollingOptions, topics };

    try {
      const tasks = await engineService.fetchAndLock(requestBody);
      this.emit("poll:success", tasks);
      tasks.forEach(executeTask);
    } catch (e) {
      this.emit("poll:error", e);
    }
    setTimeout(poll, interval);
  }

  /**
   * Subscribes a handler by adding a topic subscription
   * @param topic
   * @param customOptions
   * @param customOptions.lockDuration
   * @param handler
   */
  subscribe(topic, customOptions, handler) {
    const topicSubscriptions = this.topicSubscriptions;
    const options = this.options;
    const lockDuration = options.lockDuration;

    if (topicSubscriptions[topic]) {
      throw new Error(ALREADY_REGISTERED);
    }

    // handles the case if there is no options being
    if (typeof customOptions === "function") {
      handler = customOptions;
      customOptions = null;
    }

    if (!handler) {
      throw new Error(MISSING_HANDLER);
    }
    const unsubscribe = () => {
      delete topicSubscriptions[topic];
      this.emit("unsubscribe");
    };

    const topicSubscription = {
      handler,
      unsubscribe,
      lockDuration,
      ...customOptions,
    };
    topicSubscriptions[topic] = topicSubscription;
    this.emit("subscribe", topic, topicSubscription);

    return topicSubscription;
  }

  /**
   * Executes task using the worker registered to its topic
   */
  async executeTask(task) {
    const taskService = this.taskService;
    const topicSubscription = this.topicSubscriptions[task.topicName];
    const variables = new Variables(task.variables, {
      readOnly: true,
      processInstanceId: task.processInstanceId,
      engineService: this.engineService,
    });

    this.activeTasksCount++;
    const newTask = { ...task, variables };
    try {
      await topicSubscription.handler({ task: newTask, taskService });
      this.emit("handler:success", task);
    } catch (e) {
      this.emit("handler:error", e);
    } finally {
      this.activeTasksCount--;
    }
  }

  /**
   * Stops polling
   */
  stop() {
    this._isPollAllowed = false;
    this.emit("poll:stop");
  }
}

export default Client;
