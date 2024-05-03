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

import {
  getVariableType,
  mapEntries,
  serializeVariable,
  deserializeVariable,
} from "./__internal/utils.js";

function Variables(initialVariables = {}, options = {}) {
  const { readOnly, processInstanceId, engineService } = options;

  let dirtyVariables = {};

  /**
   * @returns the typedValue corresponding to variableName
   * @param variableName
   */
  this.getTyped = (variableName) => {
    let typedValue = initialVariables[variableName];

    if (!typedValue) {
      return null;
    }

    return deserializeVariable({
      key: variableName,
      typedValue,
      processInstanceId,
      engineService,
    });
  };

  /**
   * @returns the value corresponding to variableName
   * @param variableName
   */
  this.get = (variableName) => {
    const { value } = { ...this.getTyped(variableName) };
    return value;
  };

  /**
   * @returns the values of all variables
   */
  this.getAll = () =>
    mapEntries(initialVariables, ({ key }) => ({ [key]: this.get(key) }));

  /**
   * @returns the typed values of all variables
   */
  this.getAllTyped = () =>
    mapEntries(initialVariables, ({ key }) => ({ [key]: this.getTyped(key) }));

  /**
   * @returns the dirty variables
   */
  this.getDirtyVariables = () => {
    return dirtyVariables;
  };

  if (!readOnly) {
    /**
     * Sets typed value for variable corresponding to variableName
     * @param variableName
     * @param typedValue
     */
    this.setTyped = (variableName, typedValue) => {
      initialVariables[variableName] = dirtyVariables[variableName] =
        serializeVariable({
          key: variableName,
          typedValue,
        });
      return this;
    };

    /**
     * Sets value for variable corresponding to variableName
     * The type is determined automatically
     * @param variableName
     * @param value
     */
    this.set = (variableName, value) => {
      const type = getVariableType(value);
      return this.setTyped(variableName, { type, value, valueInfo: {} });
    };

    /**
     * Sets value for variable corresponding to variableName
     * The type is determined automatically
     * The variable has transient flag: true
     * @param variableName
     * @param value
     */
    this.setTransient = (variableName, value) => {
      const type = getVariableType(value);
      return this.setTyped(variableName, {
        type,
        value,
        valueInfo: { transient: true },
      });
    };

    /**
     * Sets the values of multiple variables at once
     * The new values are merged with existing ones
     * @param values
     */
    this.setAll = (values) => {
      const self = this;
      Object.entries(values).forEach(([key, value]) => {
        self.set(key, value);
      });
      return self;
    };

    /**
     * Sets the typed values of multiple variables at once
     * The new typedValues are merged with existing ones
     * @param typedValues
     */
    this.setAllTyped = (typedValues) => {
      const self = this;
      Object.entries(typedValues).forEach(([key, typedValue]) => {
        self.setTyped(key, typedValue);
      });
      return self;
    };
  }
}

export default Variables;
