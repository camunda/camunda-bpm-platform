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

import { jest } from "@jest/globals";
import Client from "./Client.js";
import TaskService from "./TaskService.js";
import Variables from "./Variables.js";

import {
  WRONG_INTERCEPTOR,
  MISSING_BASE_URL,
  ALREADY_REGISTERED,
  MISSING_HANDLER,
  WRONG_MIDDLEWARES,
} from "./__internal/errors.js";

// workaround for https://github.com/facebook/jest/issues/2157
const flushPromises = () =>
  new Promise(jest.requireActual("timers").setImmediate);
const advanceTimersByTime = async (msToRun) => {
  await flushPromises();
  return jest.advanceTimersByTime(msToRun);
};

jest.mock("got");
jest.mock("../lib/Variables");

const customClientOptions = {
  baseUrl: "http://localhost:XXXX/engine-rest/",
  workerId: "foobarId",
  maxTasks: 3,
  interval: 100,
  lockDuration: 30000,
  sorting: [
    { sortBy: Client.SortBy.CreateTime, sortOrder: Client.SortOrder.ASC },
  ],
};

describe("Client", () => {
  describe("poll ", () => {
    jest.useFakeTimers();
    describe("when autoPoll is false", () => {
      let pollSpy, client, engineService;

      beforeEach(() => {
        client = new Client({ ...customClientOptions, autoPoll: false });
        engineService = client.engineService;
        pollSpy = jest.spyOn(client, "poll");
      });

      test("calling start should call poll", () => {
        // when
        client.start();

        // then
        expect(pollSpy).toHaveBeenCalled();
      });

      test("should call itself again after timeout when there are no topic subscriptions", async () => {
        // when
        client.start();
        await advanceTimersByTime(customClientOptions.interval);

        // then
        expect(pollSpy).toHaveBeenCalledTimes(2);
      });

      test("should not call itself again after timeout when there are registered client", async () => {
        // given
        client.subscribe("foo", () => {});

        // when
        client.start();
        await advanceTimersByTime(customClientOptions.interval);

        // then
        expect(pollSpy).toHaveBeenCalledTimes(2);
      });

      test("should fetchAndLock and then call itself again when there are registered client", async () => {
        // given
        const fetchAndLockSpy = jest.spyOn(engineService, "fetchAndLock");
        client.subscribe("foo", () => {});

        // when
        client.start();
        await advanceTimersByTime(customClientOptions.interval);

        // then
        expect(fetchAndLockSpy).toBeCalled();
        expect(pollSpy).toHaveBeenCalledTimes(2);
      });
    });

    describe("when autoPoll is true", () => {
      it("should call itself automatically when autoPoll is true", async () => {
        // given
        const client = new Client({ ...customClientOptions, autoPoll: true });
        const pollSpy = jest.spyOn(client, "poll");

        // when
        await advanceTimersByTime(2 * customClientOptions.interval);

        // then
        expect(pollSpy).toHaveBeenCalledTimes(1);
      });
    });

    describe("usePriority", () => {
      test("should use Priority by default", () => {
        const client = new Client({
          ...customClientOptions,
          autoPoll: false,
        });

        const fetchSpy = jest.spyOn(client.engineService, "fetchAndLock");
        client.subscribe("foo", {}, function () {});
        client.start();

        expect(fetchSpy).toHaveBeenCalled();
        const args = fetchSpy.mock.calls[0][0];
        expect(args.usePriority).toBeTruthy();
      });

      test("should configure priority use", () => {
        const client = new Client({
          ...customClientOptions,
          usePriority: false,
          autoPoll: false,
        });

        const fetchSpy = jest.spyOn(client.engineService, "fetchAndLock");
        client.subscribe("foo", {}, function () {});
        client.start();

        expect(fetchSpy).toHaveBeenCalled();
        const args = fetchSpy.mock.calls[0][0];
        expect(args.usePriority).toBeFalsy();
      });
    });

    describe("when maxParallelExecutions is configured", () => {
      test("should not call fetchAndLock if maximum parallel executions limit exceeded", async () => {
        // given
        const client = new Client({
          ...customClientOptions,
          autoPoll: false,
          maxParallelExecutions: 1,
        });
        const pollSpy = jest.spyOn(client, "poll");

        const engineService = client.engineService;
        engineService.fetchAndLock = jest
          .fn()
          .mockResolvedValue([{ topicName: "foo", variables: {} }]);

        client.subscribe("foo", async () => {
          await new Promise((resolve) =>
            setTimeout(resolve, customClientOptions.interval)
          );
          await new Promise((resolve) =>
            setTimeout(resolve, 0.5 * customClientOptions.interval)
          );
        });

        // when
        client.start();

        // then
        expect(engineService.fetchAndLock).toBeCalled();

        await advanceTimersByTime(customClientOptions.interval);

        expect(pollSpy).toHaveBeenCalledTimes(2);
        expect(engineService.fetchAndLock).toHaveBeenCalledTimes(1);

        await advanceTimersByTime(customClientOptions.interval);

        expect(pollSpy).toHaveBeenCalledTimes(3);
        expect(engineService.fetchAndLock).toHaveBeenCalledTimes(1);
      });
    });

    describe("stop", () => {
      it("should stop polling", async () => {
        // given: we advance time twice then call stop
        const client = new Client(customClientOptions);
        await advanceTimersByTime(2 * customClientOptions.interval);
        client.stop();
        const pollSpy = jest.spyOn(client, "poll");

        // when
        await advanceTimersByTime(2 * customClientOptions.interval);

        // then
        expect(pollSpy).not.toHaveBeenCalled();
      });
    });
  });

  describe("subscribe", () => {
    test("should throw error if api baseUrl wasn't passed as parameter", () => {
      expect(() => {
        new Client();
      }).toThrowError(MISSING_BASE_URL);
    });

    let client, fooWork, customConfig;

    beforeEach(() => {
      client = new Client(customClientOptions);
      fooWork = () => "foobar";
      customConfig = {
        lockDuration: 3000,
        variables: ["fooVariable", "barVariable"],
        processDefinitionId: "processId",
        processDefinitionIdIn: ["processId", "processId2"],
        processDefinitionKey: "processKey",
        processDefinitionKeyIn: ["processKey", "processKey2"],
        processDefinitionVersionTag: "versionTag",
        tenantIdIn: ["tenantId"],
        withoutTenantId: true,
        localVariables: true,
        includeExtensionProperties: true,
      };
    });

    test("should overwrite default with custom client config", () => {
      expect(client.options.workerId).toBe(customClientOptions.workerId);
      expect(client.options.maxTasks).toBe(customClientOptions.maxTasks);
      expect(client.options.interval).toBe(customClientOptions.interval);
      expect(client.options.lockDuration).toBe(
        customClientOptions.lockDuration
      );
    });

    test("should subscribe to topic without custom config ", () => {
      // given
      const footopicSubscription = client.subscribe("foo", fooWork);

      // then
      expect(footopicSubscription.handler).not.toBeUndefined();
    });

    test("should subscribe to topic with custom lockDuration config ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );

      // then
      expect(footopicSubscription.lockDuration).toBe(customConfig.lockDuration);
    });

    test("should subscribe to topic with custom variable subset ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.variables).toBe(customConfig.variables);
    });

    test("should subscribe to topic based on processDefinitionId ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.processDefinitionId).toBe(
        customConfig.processDefinitionId
      );
    });

    test("should subscribe to topic based on processDefinitionIdIn ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.processDefinitionIdIn).toBe(
        customConfig.processDefinitionIdIn
      );
    });

    test("should subscribe to topic based on processDefinitionKey ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.processDefinitionKey).toBe(
        customConfig.processDefinitionKey
      );
    });

    test("should subscribe to topic based on processDefinitionKeyIn ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.processDefinitionKeyIn).toBe(
        customConfig.processDefinitionKeyIn
      );
    });

    test("should subscribe to topic based on processDefinitionVersionTag ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.processDefinitionVersionTag).toBe(
        customConfig.processDefinitionVersionTag
      );
    });

    test("should subscribe to topic based on tenantIdIn ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.tenantIdIn).toBe(customConfig.tenantIdIn);
    });

    test("should subscribe to topic based on withoutTenantId ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.withoutTenantId).toBe(
        customConfig.withoutTenantId
      );
    });

    test("should subscribe to topic based on localVariables ", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.localVariables).toBe(
        customConfig.localVariables
      );
    });

    test("should subscribe to topic with includeExtensionProperties = true", () => {
      // given
      const footopicSubscription = client.subscribe(
        "foo",
        customConfig,
        fooWork
      );
      // then
      expect(footopicSubscription.includeExtensionProperties).toBe(
        customConfig.includeExtensionProperties
      );
    });

    test("should call the API with the custom configs", () => {
      // given
      const fetchAnLockSpy = jest.spyOn(client.engineService, "fetchAndLock");
      client.subscribe("foo", customConfig, fooWork);
      client.poll();

      // then
      expect(fetchAnLockSpy).toBeCalled();
      const args = fetchAnLockSpy.mock.calls[0];
      expect(args).toMatchSnapshot();
    });

    test("should throw error if try to subscribe twice", () => {
      // given
      client.subscribe("foo", fooWork);

      // then
      expect(() => {
        client.subscribe("foo", fooWork);
      }).toThrowError(ALREADY_REGISTERED);
    });

    test("should throw error if handler is not passed", () => {
      expect(() => {
        client.subscribe("foo2");
      }).toThrowError(MISSING_HANDLER);
    });

    test("should allow to unsubscribe from a topic", () => {
      // given
      const footopicSubscription = client.subscribe("foo", fooWork);

      // when
      footopicSubscription.unsubscribe();

      // then
      expect(client.topicSubscriptions["foo"]).toBeUndefined();
    });
  });

  describe("interceptors", () => {
    it("should not add interceptors if they are not provided as a function or array of functions", () => {
      // given
      const options = { ...customClientOptions, interceptors: [] };

      // then
      expect(() => new Client(options)).toThrowError(WRONG_INTERCEPTOR);
    });

    it("should add interceptors if they are provided as a function", () => {
      // given
      const foo = () => {};
      const expectedInterceptors = [foo];
      const options = { ...customClientOptions, interceptors: foo };
      const client = new Client(options);

      // then
      expect(client.engineService.interceptors).toEqual(expectedInterceptors);
    });

    it("should add interceptors if they are provided as an array of functions", () => {
      // given
      const foo = () => {};
      const expectedInterceptors = [foo];
      const options = { ...customClientOptions, interceptors: [foo] };
      const client = new Client(options);

      // then
      expect(client.engineService.interceptors).toEqual(expectedInterceptors);
    });
  });

  describe("middlewares", () => {
    it("should not add middlewares if they are not provided as a function or array of functions", () => {
      // given
      const options = { ...customClientOptions, use: [] };

      // then
      expect(() => new Client(options)).toThrowError(WRONG_MIDDLEWARES);
    });

    it("should accept middleware function", () => {
      // givena
      const middleware = jest.fn();
      const options = { ...customClientOptions, use: middleware };
      const client = new Client(options);

      // then
      expect(middleware).toBeCalledWith(client);
    });

    it("should accept middlewares array", () => {
      // given
      const middlewares = [jest.fn(), jest.fn()];
      const options = { ...customClientOptions, use: middlewares };
      const client = new Client(options);

      // then
      middlewares.forEach((middleware) =>
        expect(middleware).toBeCalledWith(client)
      );
    });
  });

  it("executeTask should call handler with task and taskService", () => {
    // given
    Variables.mockClear();

    let client = new Client({ ...customClientOptions, autoPoll: false });
    const handler = jest.fn();
    client.subscribe("foo", handler);

    const expectedTask = { topicName: "foo", variables: {} };

    // when
    client.executeTask(expectedTask);

    // then
    // Variable service is called with specific parameters (c.f. snapshot)
    const VariablesCall = Variables.mock.calls[0];
    expect(VariablesCall).toMatchSnapshot();
    // handler should be called with specific parameters
    expect(handler).toBeCalled();
    const { task, taskService } = handler.mock.calls[0][0];
    expect(taskService).toBeInstanceOf(TaskService);
    expect(task).toBeDefined();
    expect(task.topicName).toBe(expectedTask.topicName);
    expect(task.variables).toBeInstanceOf(Variables);
  });
});
