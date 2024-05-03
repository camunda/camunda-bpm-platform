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
import events from "events";

import {
  MISSING_TASK,
  MISSING_ERROR_CODE,
  MISSING_DURATION,
  MISSING_NEW_DURATION,
} from "./__internal/errors.js";
import TaskService from "./TaskService.js";
import EngineService from "./__internal/EngineService.js";
import Variables from "./Variables.js";

jest.mock("got");

describe("TaskService", () => {
  let engineService, taskService, sanitizeTaskSpy;
  beforeEach(() => {
    engineService = new EngineService({
      workerId: "someWorker",
      baseUrl: "some/baseUrl",
    });
    taskService = new TaskService(new events(), engineService);
    sanitizeTaskSpy = jest.spyOn(taskService, "sanitizeTask");
  });

  it("error(event, data) should emit error event with data: ", () => {
    // given
    const emitSpy = jest.spyOn(taskService.events, "emit");
    const event = "some_event";
    const expectedEvent = `${event}:error`;
    const expectedData = "some data";

    // when
    taskService.error(event, expectedData);

    // then
    expect(emitSpy).toBeCalledWith(expectedEvent, expectedData);
  });

  describe("sanitizeTask", () => {
    test("should return task with id when task id is provided", () => {
      // given
      const expectedTask = { id: "2" };

      // then
      expect(taskService.sanitizeTask("2")).toEqual(expectedTask);
    });

    test("should return task with id when task is provided", () => {
      // given
      const expectedTask = { id: "2" };

      // then
      expect(taskService.sanitizeTask(expectedTask)).toEqual(expectedTask);
    });
  });

  describe("complete", () => {
    test("should throw an error if no parameter is provided", async () => {
      try {
        await taskService.complete();
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_TASK));
      }
    });

    test("should call sanitizeTask with given parameter", () => {
      // given
      const expectedTaskId = "foo";

      // when
      taskService.complete(expectedTaskId);

      // then
      expect(sanitizeTaskSpy).toBeCalledWith(expectedTaskId);
    });

    test("should call api complete with provided task", () => {
      // given
      const completeSpy = jest.spyOn(engineService, "complete");
      const expectedTaskId = "foo";

      // when
      taskService.complete(expectedTaskId);

      // then
      expect(completeSpy).toBeCalledWith({ id: expectedTaskId });
    });

    test("should call api complete with provided variables and localVariables", () => {
      // given
      const completeSpy = jest.spyOn(engineService, "complete");
      const expectedTaskId = "foo";
      const expectedVariables = {
        someVariable: {
          value: "some variable value",
          type: "string",
          valueInfo: {},
        },
      };
      const expectedLocalVariables = {
        someLocalVariable: {
          value: "some local variable value",
          type: "string",
          valueInfo: {},
        },
      };
      const variables = new Variables();
      variables.setAllTyped(expectedVariables);
      const localVariables = new Variables(expectedLocalVariables);
      localVariables.setAllTyped(expectedLocalVariables);

      // when
      taskService.complete(expectedTaskId, variables, localVariables);

      // then
      expect(completeSpy).toBeCalledWith({
        id: expectedTaskId,
        variables: expectedVariables,
        localVariables: expectedLocalVariables,
      });
    });

    test("should rethrow error", async () => {
      // given
      engineService.complete = jest.fn().mockImplementation(() => {
        throw new Error("my error message");
      });

      try {
        // when
        await taskService.complete("myTaskId");
        expect(true).toBe(false);
      } catch (e) {
        // then
        expect(e).toStrictEqual(new Error("my error message"));
      }
    });
  });

  describe("handleFailure", () => {
    test("should throw an error if no taskid is provided", async () => {
      try {
        await taskService.handleFailure();
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_TASK));
      }
    });

    test("should call sanitizeTask with given parameter", () => {
      // given
      const expectedTaskId = "foo";

      // when
      taskService.handleFailure(expectedTaskId);

      // then
      expect(sanitizeTaskSpy).toBeCalledWith(expectedTaskId);
    });

    test("should call api handleFailure with provided task and error message", () => {
      // given
      const handleFailureSpy = jest.spyOn(engineService, "handleFailure");
      const expectedPayload = { errorMessage: "some error message" };
      const expectedTaskId = "foo";

      // when
      taskService.handleFailure(expectedTaskId, expectedPayload);

      // then
      expect(handleFailureSpy).toBeCalledWith(
        { id: expectedTaskId },
        expectedPayload
      );
    });

    test("should rethrow error", async () => {
      // given
      engineService.handleFailure = jest.fn().mockImplementation(() => {
        throw new Error("my error message");
      });

      try {
        // when
        await taskService.handleFailure("myTaskId");
        expect(true).toBe(false);
      } catch (e) {
        // then
        expect(e).toStrictEqual(new Error("my error message"));
      }
    });
  });

  describe("handleBpmnError", () => {
    test("should throw an error if no taskid is provided", async () => {
      try {
        await taskService.handleBpmnError();
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_TASK));
      }
    });

    test("should throw an error if no error code is provided", async () => {
      try {
        await taskService.handleBpmnError("fooId");
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_ERROR_CODE));
      }
    });

    test("should call sanitizeTask with given parameter", () => {
      // given
      const expectedTaskId = "foo";

      // when
      taskService.handleBpmnError(expectedTaskId, "some bpmn error");

      // then
      expect(sanitizeTaskSpy).toBeCalledWith(expectedTaskId);
    });

    test("should call api handleBpmnError with provided task and error code", () => {
      // given
      const handleBpmnErrorSpy = jest.spyOn(engineService, "handleBpmnError");
      const expectedTaskId = "foo";
      const expectedErrorCode = "foo123";

      // when
      taskService.handleBpmnError(expectedTaskId, expectedErrorCode);

      // then
      expect(handleBpmnErrorSpy).toBeCalledWith(
        { id: expectedTaskId },
        expectedErrorCode,
        undefined,
        undefined
      );
    });

    test("should call api handleBpmnError with provided task, error code, error message and variables", () => {
      // given
      const handleBpmnErrorSpy = jest.spyOn(engineService, "handleBpmnError");
      const expectedTaskId = "foo";
      const expectedErrorCode = "foo123";
      const expectedErrorMessage = "errMess";
      const expectedVariables = {
        someVariable: {
          value: "some variable value",
          type: "string",
          valueInfo: {},
        },
      };
      const variables = new Variables();
      variables.setAllTyped(expectedVariables);

      // when
      taskService.handleBpmnError(
        expectedTaskId,
        expectedErrorCode,
        expectedErrorMessage,
        variables
      );

      // then
      expect(handleBpmnErrorSpy).toBeCalledWith(
        { id: expectedTaskId },
        expectedErrorCode,
        expectedErrorMessage,
        expectedVariables
      );
    });

    test("should rethrow error", async () => {
      // given
      engineService.handleBpmnError = jest.fn().mockImplementation(() => {
        throw new Error("my error message");
      });

      try {
        // when
        await taskService.handleBpmnError("myTaskId", 123);
        expect(true).toBe(false);
      } catch (e) {
        // then
        expect(e).toStrictEqual(new Error("my error message"));
      }
    });
  });

  describe("extendLock", () => {
    test("should throw an error if no taskid is provided", async () => {
      try {
        await taskService.extendLock();
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_TASK));
      }
    });

    test("should throw an error if no new lock duration is provided", async () => {
      try {
        await taskService.extendLock("fooId");
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_NEW_DURATION));
      }
    });

    test("should call sanitizeTask with given parameter", () => {
      // given
      const expectedTaskId = "foo";

      // when
      taskService.extendLock(expectedTaskId, 2000);

      // then
      expect(sanitizeTaskSpy).toBeCalledWith(expectedTaskId);
    });

    test("should call api extendLock with provided task and lock duration", () => {
      // given
      const extendLockSpy = jest.spyOn(engineService, "extendLock");
      const expectedTaskId = "foo";
      const expectedNewDuration = 100;

      // when
      taskService.extendLock(expectedTaskId, expectedNewDuration);

      // then
      expect(extendLockSpy).toBeCalledWith(
        { id: expectedTaskId },
        expectedNewDuration
      );
    });

    test("should rethrow error", async () => {
      // given
      engineService.extendLock = jest.fn().mockImplementation(() => {
        throw new Error("my error message");
      });

      try {
        // when
        await taskService.extendLock("myTaskId", 10);
        expect(true).toBe(false);
      } catch (e) {
        // then
        expect(e).toStrictEqual(new Error("my error message"));
      }
    });
  });

  describe("lock", () => {
    test("should throw an error if no task id is provided", async () => {
      try {
        await taskService.lock();
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_TASK));
      }
    });

    test("should throw an error if no duration is provided", async () => {
      try {
        await taskService.lock("foo");
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_DURATION));
      }
    });

    test("should call sanitizeTask with given parameter", async () => {
      // given
      const expectedTaskId = "foo";
      const lockDuration = 42;

      // when
      await taskService.lock(expectedTaskId, lockDuration);

      // then
      expect(sanitizeTaskSpy).toBeCalledWith(expectedTaskId);
    });

    test("should call api lock with provided task", async () => {
      const lockSpy = jest.spyOn(engineService, "lock");
      // given
      const expectedTaskId = "foo";
      const lockDuration = 42;

      // when
      await taskService.lock(expectedTaskId, lockDuration);

      // then
      expect(lockSpy).toHaveBeenCalledWith(
        { id: expectedTaskId },
        lockDuration
      );
    });

    test("should rethrow error", async () => {
      // given
      engineService.lock = jest.fn().mockImplementation(() => {
        throw new Error("my error message");
      });

      try {
        // when
        await taskService.lock("myTaskId", 10);
        expect(true).toBe(false);
      } catch (e) {
        // then
        expect(e).toStrictEqual(new Error("my error message"));
      }
    });
  });

  describe("unlock", () => {
    let unlockSpy;
    beforeEach(() => {
      unlockSpy = jest.spyOn(engineService, "unlock");
    });

    test("should throw an error if no task id is provided", async () => {
      try {
        await taskService.unlock();
      } catch (e) {
        expect(e).toEqual(new Error(MISSING_TASK));
      }
    });

    test("should call sanitizeTask with given parameter", () => {
      // given
      const expectedTaskId = "foo";

      // when
      taskService.unlock(expectedTaskId);

      // then
      expect(sanitizeTaskSpy).toBeCalledWith(expectedTaskId);
    });

    test("should call api unlock with provided task", () => {
      // given
      const expectedTaskId = "foo";

      // when
      taskService.unlock(expectedTaskId);

      // then
      expect(unlockSpy).toBeCalledWith({ id: expectedTaskId });
    });

    test("should rethrow error", async () => {
      // given
      engineService.unlock = jest.fn().mockImplementation(() => {
        throw new Error("my error message");
      });

      try {
        // when
        await taskService.unlock("myTaskId", 10);
        expect(true).toBe(false);
      } catch (e) {
        // then
        expect(e).toStrictEqual(new Error("my error message"));
      }
    });
  });
});
