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

import { Variables, File } from "../index.js";

describe("Variables", () => {
  describe("read-only", () => {
    it("should only have getters if readOnly is true", () => {
      // given
      const readOnlyVariables = new Variables({}, { readOnly: true });

      // then
      expect(Object.keys(readOnlyVariables)).toMatchSnapshot();
    });

    it("should have getters and setters if readOnly is not true", () => {
      // given
      const readOnlyVariables = new Variables({});

      // then
      expect(Object.keys(readOnlyVariables)).toMatchSnapshot();
    });
  });

  describe("getters", () => {
    let variables;
    beforeEach(() => {
      variables = new Variables({
        foo: { type: "string", value: "FooValue", valueInfo: {} },
        bar: { type: "integer", value: 2, valueInfo: {} },
        baz: { type: "json", value: '{"name":"baz"}', valueInfo: {} },
        qux: {
          type: "date",
          value: new Date("2018-01-23T14:42:45.435+0200"),
          valueInfo: {},
        },
        zex: {
          type: "file",
          value: null,
          valueInfo: {},
        },
      });

      const file = new File({ localPath: "some/local/path" });
      file.content = Buffer.from("some content");

      variables.setTyped("blax", {
        type: "file",
        value: file,
        valueInfo: {},
      });
    });

    it("getAllTyped() should return all variables", () => {
      expect(variables.getAllTyped()).toMatchSnapshot();
    });

    it("getAll() should return values of all variables", () => {
      expect(variables.getAll()).toMatchSnapshot();
    });

    it("getDirtyVariables() should return all dirty variables", () => {
      expect(variables.getDirtyVariables()).toMatchSnapshot();
    });

    it("get('foo') should return value of key foo", () => {
      expect(variables.get("foo")).toMatchSnapshot();
    });

    it("getTyped('non_existing_key') should return null", () => {
      expect(variables.getTyped("non_existing_key")).toBeNull();
    });

    it("getTyped('foo') should return the typed value of key foo", () => {
      expect(variables.getTyped("foo")).toMatchSnapshot();
    });
  });

  describe("setters", () => {
    let variables;
    beforeEach(() => {
      variables = new Variables();
    });

    it('setTyped("baz",someTypeValue) should set typed value with key "baz"', () => {
      // given
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
      const key = "baz";
      const typedValue = {
        type: "json",
        value: { name: "bazname" },
        valueInfo: {},
      };

      // when
      variables.setTyped(key, typedValue);

      // then
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
    });

    it("setAllTyped(someTypedValues) should add someTypedValues to variables", () => {
      // given
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
      const typedValues = {
        foo: { value: "fooValue", type: "string", valueInfo: {} },
      };
      variables.set("bar", "barValue");

      // when
      variables.setAllTyped(typedValues);

      // then
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
    });

    it('set("foo", "fooValue")) should set variable with key "foo" and value "fooValue"', () => {
      // given
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
      variables.set("foo", "fooValue");

      // then
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
    });

    it('setTransient("fooTransient", "fooValueTransient")) should set variable with key "fooTransient" and value "fooValueTransient" and transient "true"', () => {
      // given
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
      variables.setTransient("fooTransient", "fooValueTransient");

      // then
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
    });

    it("setAll(someValues)  should add someValues to variables", () => {
      // given
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
      const someValues = {
        foo: "FooValue",
        bar: 2,
      };

      // when
      variables.setAll(someValues);

      // then
      // given
      expect(variables.getAllTyped()).toMatchSnapshot("variables");
      expect(variables.getDirtyVariables()).toMatchSnapshot("dirty variables");
    });
  });
});
