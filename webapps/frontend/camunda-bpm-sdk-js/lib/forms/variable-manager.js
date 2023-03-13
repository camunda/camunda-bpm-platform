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

'use strict';

var moment = require('moment');
var convertToType = require('./type-util').convertToType;

/**
 * @class
 * the variable manager is responsible for managing access to variables.
 *
 * Variable Datatype
 *
 * A variable has the following properties:
 *
 *   name: the name of the variable
 *
 *   type: the type of the variable. The type is a "backend type"
 *
 *
 */
function VariableManager() {
  /** @member object containing the form fields. Initially empty. */
  this.variables = {};

  /** @member boolean indicating whether the variables are fetched */
  this.isVariablesFetched = false;
}

VariableManager.prototype.fetchVariable = function(variable) {
  if (this.isVariablesFetched) {
    throw new Error(
      'Illegal State: cannot call fetchVariable(), variables already fetched.'
    );
  }
  this.createVariable({name: variable});
};

VariableManager.prototype.createVariable = function(variable) {
  if (!this.variables[variable.name]) {
    this.variables[variable.name] = variable;
  } else {
    throw new Error(
      'Cannot add variable with name ' + variable.name + ': already exists.'
    );
  }
};

VariableManager.prototype.destroyVariable = function(variableName) {
  if (this.variables[variableName]) {
    delete this.variables[variableName];
  } else {
    throw new Error(
      'Cannot remove variable with name ' +
        variableName +
        ': variable does not exist.'
    );
  }
};

VariableManager.prototype.setOriginalValue = function(variableName, value) {
  if (this.variables[variableName]) {
    this.variables[variableName].originalValue = value;
  } else {
    throw new Error(
      'Cannot set original value of variable with name ' +
        variableName +
        ': variable does not exist.'
    );
  }
};

VariableManager.prototype.variable = function(variableName) {
  return this.variables[variableName];
};

VariableManager.prototype.variableValue = function(variableName, value) {
  var variable = this.variable(variableName);

  if (typeof value === 'undefined' || value === null) {
    value = null;
  } else if (value === '' && variable.type !== 'String') {
    // convert empty string to null for all types except String
    value = null;
  } else if (typeof value === 'string' && variable.type !== 'String') {
    // convert string value into model value
    value = convertToType(value, variable.type);
  }

  if (arguments.length === 2) {
    variable.value = value;
  }

  return variable.value;
};

VariableManager.prototype.isDirty = function(name) {
  var variable = this.variable(name);
  if (this.isJsonVariable(name)) {
    return variable.originalValue !== JSON.stringify(variable.value);
  } else if (
    this.isDateVariable(name) &&
    variable.originalValue &&
    variable.value
  ) {
    // check, if it is the same moment
    return !moment(variable.originalValue, moment.ISO_8601).isSame(
      variable.value
    );
  } else {
    return (
      variable.originalValue !== variable.value || variable.type === 'Object'
    );
  }
};

VariableManager.prototype.isJsonVariable = function(name) {
  var variable = this.variable(name);
  var type = variable.type;

  var supportedTypes = ['Object', 'json', 'Json'];
  var idx = supportedTypes.indexOf(type);

  if (idx === 0) {
    return (
      variable.valueInfo.serializationDataFormat.indexOf('application/json') !==
      -1
    );
  }

  return idx !== -1;
};

VariableManager.prototype.isDateVariable = function(name) {
  var variable = this.variable(name);
  return variable.type === 'Date';
};

VariableManager.prototype.variableNames = function() {
  // since we support IE 8+ (http://kangax.github.io/compat-table/es5/)
  return Object.keys(this.variables);
};

module.exports = VariableManager;
