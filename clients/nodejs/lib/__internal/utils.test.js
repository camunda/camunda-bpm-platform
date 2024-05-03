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
import {
  isFunction,
  andArrayWith,
  isArrayOfFunctions,
  isUndefinedOrNull,
  getVariableType,
  mapEntries,
  deserializeVariable,
  serializeVariable,
} from "./utils.js";

import File from "../File";

describe("utils", () => {
  describe("isFunction", () => {
    it("should return false if param is not a function", () => {
      expect(isFunction()).toBe(false);
      expect(isFunction(2)).toBe(false);
    });

    it("should return true if param is a function", () => {
      expect(isFunction(() => {})).toBe(true);
    });
  });

  describe("andArrayWith", () => {
    it("should apply test function on each element on the array and AND the results", () => {
      const biggerThan5 = (a) => a > 5;
      const arr1 = [1, 2, 3, 4];
      const arr2 = [6, 7, 8, 9];
      const arr3 = [6, 7, 2, 8, 9];

      expect(andArrayWith(arr1, biggerThan5)).toBe(false);
      expect(andArrayWith(arr2, biggerThan5)).toBe(true);
      expect(andArrayWith(arr3, biggerThan5)).toBe(false);
    });
  });

  describe("isArrayOfFunctions", () => {
    it("should return false for non array", () => {
      expect(isArrayOfFunctions(3)).toBe(false);
    });

    it("should return false for non array of functions", () => {
      expect(isArrayOfFunctions([1, 2])).toBe(false);
    });

    it("should return true for an array of functions", () => {
      expect(isArrayOfFunctions([() => {}, () => {}])).toBe(true);
    });
  });

  describe("isUndefinedOrNull", () => {
    it("should return false for non undefined or null", () => {
      expect(isUndefinedOrNull(1)).toBe(false);
      expect(isUndefinedOrNull("foo")).toBe(false);
      expect(isUndefinedOrNull([])).toBe(false);
      expect(isUndefinedOrNull(() => {})).toBe(false);
    });

    it("should return true for undefined", () => {
      expect(isUndefinedOrNull(undefined)).toBe(true);
    });

    it("should return true for null", () => {
      expect(isUndefinedOrNull(null)).toBe(true);
    });
  });

  describe("getVariableType", () => {
    test("getVariableType(null) should be null", () => {
      expect(getVariableType(null)).toBe("null");
    });

    test("getVariableType() should be null", () => {
      expect(getVariableType()).toBe("null");
    });

    test("getVariableType(1) should be integer", () => {
      expect(getVariableType(1)).toBe("integer");
    });

    test("getVariableType(2^32) should be long", () => {
      expect(getVariableType(Math.pow(2, 32))).toBe("long");
    });

    test("getVariableType(2.32) should be double", () => {
      expect(getVariableType(2.32)).toBe("double");
    });

    test("getVariableType(true) should be boolean", () => {
      expect(getVariableType(true)).toBe("boolean");
    });

    test("getVariableType('foo') should be string", () => {
      expect(getVariableType("foo")).toBe("string");
    });

    test("getVariableType(new File) should be file", () => {
      const file = new File({ localPath: "foo" });
      expect(getVariableType(file)).toBe("file");
    });

    test('getVariableType({"x": 2}) should be json', () => {
      expect(getVariableType({ x: 2 })).toBe("json");
    });

    test("getVariableType({ x: 2 }) should be json", () => {
      expect(getVariableType({ x: 2 })).toBe("json");
    });
  });

  describe("mapEntries", () => {
    it("should map entries with mapper: entry -> entry × 2", () => {
      // given
      const initialObject = { a: 2, b: 3, c: 4 };
      const expectedObject = { a: 4, b: 6, c: 8 };
      const mapper = ({ key, value }) => ({ [key]: value * 2 });

      // then
      expect(mapEntries(initialObject, mapper)).toEqual(expectedObject);
    });
  });

  describe("deserializeVariable", () => {
    it("value should remain the same if type is neither file, json nor date", () => {
      // given
      let typedValue = { value: "some value", type: "string", valueInfo: {} };

      // then
      expect(deserializeVariable({ typedValue })).toMatchObject(typedValue);
    });

    it("value should be parsed if type is JSON", () => {
      // given
      let parsedValue = { x: 10 };
      let typedValue = {
        value: JSON.stringify(parsedValue),
        type: "json",
        valueInfo: {},
      };
      let expectedTypedValue = { ...typedValue, value: parsedValue };

      // then
      expect(deserializeVariable({ typedValue })).toMatchObject(
        expectedTypedValue
      );
    });

    it("value should be a File instance if type is file", () => {
      // given
      let typedValue = { value: "", type: "File", valueInfo: {} };
      let options = {
        key: "variable_key",
        typedValue,
        processInstanceId: "process_instance_id",
        engineService: {},
      };
      let result = deserializeVariable(options);

      // then
      // the value should be a file
      expect(result.value).toBeInstanceOf(File);

      // match file parameters to snapshot
      expect(result).toMatchSnapshot();
    });

    it("value should become a Date object if type is date", () => {
      // given
      let dateStr = "2013-06-30T21:04:22.000+0200";
      let dateObj = new Date(dateStr);
      let typedValue = {
        value: dateStr,
        type: "Date",
        valueInfo: {},
      };

      // then
      expect(deserializeVariable({ typedValue }).value).toMatchObject(dateObj);
    });
  });

  describe("serializeVariable", () => {
    it("value should remain the same if type is neither file, json nor date", () => {
      // given
      let typedValue = { value: 21, type: "integer", valueInfo: {} };

      // then
      expect(serializeVariable({ typedValue })).toMatchObject(typedValue);
    });

    it("value should be stringifyed if type is JSON and value is not a string", () => {
      // given
      let parsedValue = { x: 10 };
      let typedValue = {
        value: parsedValue,
        type: "json",
        valueInfo: {},
      };
      let expectedTypedValue = {
        ...typedValue,
        value: JSON.stringify(parsedValue),
      };

      // then
      expect(serializeVariable({ typedValue })).toMatchObject(
        expectedTypedValue
      );
    });

    it("value should remain the same if type is JSON and value is a string", () => {
      // given
      let value = JSON.stringify({ x: 10 });
      let typedValue = {
        value,
        type: "json",
        valueInfo: {},
      };

      // then
      expect(serializeVariable({ typedValue })).toMatchObject(typedValue);
    });

    it("should return result of createTypedValue if instance if type is file and value is an instance of File", () => {
      // given
      let value = new File({ localPath: "some/path" });
      let createTypedValueSpy = jest.spyOn(value, "createTypedValue");
      let typedValue = { value, type: "File", valueInfo: {} };
      let result = serializeVariable({ typedValue });

      // then
      // createTypedValue should be called
      expect(createTypedValueSpy).toBeCalled();

      // result must be the result of createTypedValue
      expect(result).toMatchObject(value.createTypedValue());
    });

    it("value should remain the same if instance if type is file and value is not an instance of File", () => {
      // given
      let value = "some value";
      let typedValue = { value, type: "file", valueInfo: {} };
      let result = serializeVariable({ typedValue });

      // then
      expect(result).toMatchObject(typedValue);
    });

    it("value should be converted to proper formatted string if type is date and value is an instance of date", () => {
      // given
      let dateStr = "2013-06-30T21:04:22.000+0200";
      let formattedDate = "2013-06-30T19:04:22.000+0000";
      let dateObj = new Date(dateStr);
      let typedValue = {
        value: dateObj,
        type: "Date",
        valueInfo: {},
      };

      // then
      expect(serializeVariable({ typedValue }).value).toBe(formattedDate);
    });
  });
});
