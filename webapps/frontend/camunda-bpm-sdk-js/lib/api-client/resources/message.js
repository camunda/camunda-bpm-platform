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
 * Message Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Message = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Message.path = 'message';

/**
 * correlates a message
 *
 * @param {Object} [params]
 * @param {String} [params.messageName]     The message name of the message to be corrolated
 * @param {String} [params.businessKey]     The business key the workflow instance is to be initialized with. The business key identifies the workflow instance in the context of the given workflow definition.
 * @param {String} [params.correlationKeys]       A JSON object containing the keys the recieve task is to be corrolated with. Each key corresponds to a variable name and each value to a variable value.
 * @param {String} [params.processVariables]       A JSON object containing the variables the recieve task is to be corrolated with. Each key corresponds to a variable name and each value to a variable value.
 */
Message.correlate = function(params, done) {
  var url = this.path + '/';

  return this.http.post(url, {
    data: params,
    done: done
  });
};

module.exports = Message;
