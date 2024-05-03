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
import logger from "./logger.js";
import events from "events";

describe("logger", () => {
  let client;

  describe("events", () => {
    beforeAll(() => {
      client = new events();
      logger(client, "silly");
    });

    beforeEach(() => {
      jest.spyOn(console, "log").mockImplementation(() => jest.fn());
    });

    afterEach(() => {
      console.log.mockClear();
    });

    it("should log subscribe event", () => {
      // when
      client.emit("subscribe", "some topic");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log unsubscribe event", () => {
      // when
      client.emit("unsubscribe", "some topic");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log poll:start event", () => {
      // when
      client.emit("poll:start");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log poll:stop event", () => {
      // when
      client.emit("poll:stop");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log poll:success event", () => {
      // when
      client.emit("poll:success", ["task1", "task2"]);

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log poll:error event", () => {
      // when
      client.emit("poll:error");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log complete:success", () => {
      // when
      client.emit("complete:success", { id: "some task id" });

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log complete:error", () => {
      // when
      client.emit("complete:error", { id: "some task id" }, "some error");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log handleFailure:success", () => {
      // when
      client.emit("handleFailure:success", { id: "some task id" });

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log handleFailure:error", () => {
      // when
      client.emit("handleFailure:error", { id: "some task id" }, "some error");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log handleBpmnError:success", () => {
      // when
      client.emit("handleBpmnError:success", { id: "some task id" });

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log handleBpmnError:error", () => {
      // when
      client.emit(
        "handleBpmnError:error",
        { id: "some task id" },
        "some error"
      );

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log extendLock:success", () => {
      // when
      client.emit("extendLock:success", { id: "some task id" });

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log extendLock:error", () => {
      // when
      client.emit("extendLock:error", { id: "some task id" }, "some error");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log unlock:success", () => {
      // when
      client.emit("unlock:success", { id: "some task id" });

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });

    it("should log unlock:error", () => {
      // when
      client.emit("unlock:error", { id: "some task id" }, "some error");

      // then
      expect(console.log.mock.calls[0][0]).toMatchSnapshot();
    });
  });

  describe("config", () => {
    beforeEach(() => {
      client = new events();
      jest.spyOn(console, "log").mockImplementation(() => jest.fn());
    });

    afterEach(() => {
      console.log.mockClear();
    });

    it("should have debug level", () => {
      // when
      logger(client, "debug");

      client.emit("poll:start"); // debug
      client.emit("subscribe", "some topic"); // info
      client.emit("poll:error"); // error

      // then
      expect(console.log).toHaveBeenCalledTimes(3);
    });

    it("should have info level", () => {
      // when
      logger(client);

      client.emit("poll:start"); // debug
      client.emit("subscribe", "some topic"); // info
      client.emit("poll:error"); // error

      // then
      expect(console.log).toHaveBeenCalledTimes(2);
    });

    it("should have error level", () => {
      // when
      logger(client, "error");

      client.emit("poll:start"); // debug
      client.emit("subscribe", "some topic"); // info
      client.emit("poll:error"); // error

      // then
      expect(console.log).toHaveBeenCalledTimes(1);
    });

    it("should use info level on default", () => {
      // when
      logger(client);

      client.emit("poll:start"); // debug
      client.emit("subscribe", "some topic"); // info
      client.emit("poll:error"); // error

      // then
      expect(console.log).toHaveBeenCalledTimes(2);
    });

    it("should allow NPM log level numbers", () => {
      // when
      logger(client, 2); // info

      client.emit("poll:start"); // debug
      client.emit("subscribe", "some topic"); // info
      client.emit("poll:error"); // error

      // then
      expect(console.log).toHaveBeenCalledTimes(2);
    });
  });
});
