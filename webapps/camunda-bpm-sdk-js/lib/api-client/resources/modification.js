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

/**
 * Modification Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Modification = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Modification.path = 'modification';

/**
 * Execute a modification
 * @param  {Object}   params
 * @param  {String}   [params.processDefinitionId]
 * @param  {String}   [params.skipCustomListeners]
 * @param  {String}   [params.skipIoMappings]
 * @param  {String}   [params.processInstanceIds]
 * @param  {String}   [params.processInstanceQuery]
 * @param  {String}   [params.instructions]
 * @param  {String}   [params.annotation]
 * @param  {Function} done
 */
Modification.execute = function(params, done) {
  var path = this.path + '/execute';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

/**
 * Execute a modification asynchronously
 * @param  {Object}   params
 * @param  {String}   [params.processDefinitionId]
 * @param  {String}   [params.skipCustomListeners]
 * @param  {String}   [params.skipIoMappings]
 * @param  {String}   [params.processInstanceIds]
 * @param  {String}   [params.processInstanceQuery]
 * @param  {String}   [params.instructions]
 * @param  {String}   [params.annotation]
 * @param  {Function} done
 */
Modification.executeAsync = function(params, done) {
  var path = this.path + '/executeAsync';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

module.exports = Modification;
