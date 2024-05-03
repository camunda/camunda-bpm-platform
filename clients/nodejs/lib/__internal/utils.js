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

import File from "../File.js";

/**
 * Checks if parameter is a function
 */
const isFunction = (f) => typeof f === "function";

/**
 * Applies test function on each element on the array and ANDs the results
 * @param [Array] arr
 * @param [Function] test
 */
const andArrayWith = (arr, test) =>
  arr.reduce((boolean, current) => boolean && test(current), true);

/**
 * Checks if parameter is an array of functions
 */
const isArrayOfFunctions = (a) =>
  Array.isArray(a) && a.length > 0 && andArrayWith(a, isFunction);

/**
 * Checks if parameter is undefined or null
 */
const isUndefinedOrNull = (a) => typeof a === "undefined" || a === null;

const typeMatchers = {
  null: isUndefinedOrNull,

  /**
   * @returns {boolean} true if value is Integer
   */
  integer(a) {
    return (
      Number.isInteger(a) && a >= -Math.pow(2, 31) && a <= Math.pow(2, 31) - 1
    );
  },

  /**
   * @returns {boolean} true if value is Long
   */
  long(a) {
    return Number.isInteger(a) && !typeMatchers.integer(a);
  },

  /**
   * @returns {boolean} true if value is Double
   */
  double(a) {
    return typeof a === "number" && !Number.isInteger(a);
  },

  /**
   * @returns {boolean} true if value is Boolean
   */
  boolean(a) {
    return typeof a === "boolean";
  },

  /**
   * @returns {boolean} true if value is String
   */
  string(a) {
    return typeof a === "string";
  },

  /**
   * @returns {boolean} true if value is File
   */
  file(a) {
    return a instanceof File;
  },

  /**
   * @returns {boolean} true if value is Date.
   * */
  date(a) {
    return a instanceof Date;
  },

  /**
   * @returns {boolean} true if value is JSON
   */
  json(a) {
    return typeof a === "object";
  },
};

/**
 * @returns the type of the variable
 * @param variable: external task variable
 */
const getVariableType = (variable) => {
  const match = Object.entries(typeMatchers).filter(
    ([matcherKey, matcherFunction]) => matcherFunction(variable)
  )[0];

  return match[0];
};

/**
 * @returns object mapped by applying mapper to each of its entries
 * @param object
 * @param mapper
 */
const mapEntries = (object, mapper) =>
  Object.entries(object).reduce((accumulator, [key, value]) => {
    return { ...accumulator, ...mapper({ key, value }) };
  }, {});

const deserializeVariable = ({
  key,
  typedValue,
  processInstanceId,
  engineService,
}) => {
  let { value, type } = { ...typedValue };

  type = type.toLowerCase();

  if (type === "json") {
    value = JSON.parse(value);
  }

  if (type === "file") {
    let remotePath = `/execution/${processInstanceId}/localVariables/${key}/data`;
    value = new File({ typedValue, remotePath, engineService });
  }

  if (type === "date") {
    value = new Date(value);
  }

  return { ...typedValue, value, type };
};

const serializeVariable = ({ key, typedValue }) => {
  let { value, type } = { ...typedValue };

  type = type.toLowerCase();

  if (type === "file" && value instanceof File) {
    return value.createTypedValue();
  }

  if (type === "json" && typeof value !== "string") {
    value = JSON.stringify(value);
  }

  if (type === "date" && value instanceof Date) {
    value = value.toISOString().replace(/Z$/, "+0000");
  }

  return { ...typedValue, value, type };
};

export {
  isFunction,
  andArrayWith,
  isArrayOfFunctions,
  isUndefinedOrNull,
  getVariableType,
  mapEntries,
  serializeVariable,
  deserializeVariable,
};
