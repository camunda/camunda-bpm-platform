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
import { File } from "../index.js";
import { MISSING_FILE_OPTIONS } from "./__internal/errors.js";

describe("File", () => {
  describe("constructor", () => {
    it("should throw Error when neither localPath nor typedValue are provided", () => {
      expect(() => new File()).toThrowError(MISSING_FILE_OPTIONS);
    });

    it("should create instance with proper values when typedValue is provided", () => {
      // given
      const typedValue = {
        valueInfo: {
          filename: "somefile",
          mimetype: "application/json",
          encoding: "utf-8",
        },
      };
      const file = new File({ localPath: "foo", typedValue });

      // then
      expect(file).toMatchSnapshot();
    });

    it("should create instance with proper values", () => {
      // given
      const options = {
        filename: "somefile",
        mimetype: "application/json",
        encoding: "utf-8",
        localPath: "foo",
      };

      const file = new File(options);

      // then
      expect(file).toMatchSnapshot();
    });
  });

  describe("load", () => {
    it("should load content from remotePath when it's provided", async () => {
      // given
      const engineService = {
        get: jest
          .fn()
          .mockImplementation(() => Promise.resolve(Buffer.from("", "utf-8"))),
      };
      const remotePath = "some/remote/path";
      const expectedBuffer = Buffer.from(await engineService.get(remotePath));
      const file = await new File({ remotePath, engineService }).load();

      // then
      expect(file.content).toEqual(expectedBuffer);
    });

    it("should load content from localPath when it's provided", async () => {
      // given
      const localPath = "some/local/path";
      let file = await new File({ localPath });
      file.__readFile = jest.fn().mockImplementation(() => {
        return Promise.resolve("some content");
      });
      const expectedContent = "some content";
      file = await file.load();
      const content = file.content;

      // then
      expect(content).toBe(expectedContent);
    });
  });

  describe("createTypedValue", () => {
    it("should create typedValue with provided parameters", async () => {
      // given
      const valueInfo = {
        filename: "somefile",
        mimetype: "application/text",
        encoding: "utf-8",
      };
      const value = "this some random value";
      const expectedTypedValue = {
        value: Buffer.from(value).toString("utf-8"),
        type: "file",
        valueInfo,
      };
      const engineService = {
        get: jest.fn().mockImplementation(() => Promise.resolve(value)),
      };
      const remotePath = "some/remote/path";
      const file = await new File({
        remotePath,
        engineService,
        ...valueInfo,
      }).load();

      // then
      expect(file.createTypedValue()).toEqual(expectedTypedValue);
    });
  });
});
