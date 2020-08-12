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

var AbstractClientResource = require('./../abstract-client-resource');
var utils = require('../../utils');

/**
 * Execution Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Execution = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Execution.path = 'execution';

/**
 * Deletes a variable in the context of a given execution. Deletion does not propagate upwards in the execution hierarchy.
 */
Execution.deleteVariable = function(data, done) {
  return this.http.del(
    this.path +
      '/' +
      data.id +
      '/localVariables/' +
      utils.escapeUrl(data.varId),
    {
      done: done
    }
  );
};

/**
 * Updates or deletes the variables in the context of an execution.
 * The updates do not propagate upwards in the execution hierarchy.
 * Updates precede deletions.
 * So, if a variable is updated AND deleted, the deletion overrides the update.
 */
Execution.modifyVariables = function(data, done) {
  return this.http.post(this.path + '/' + data.id + '/localVariables', {
    data: data,
    done: done
  });
};

module.exports = Execution;
