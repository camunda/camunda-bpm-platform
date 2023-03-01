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
 * No-Op callback
 */
function noop() {}

/**
 * CaseDefinition Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var CaseDefinition = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
CaseDefinition.path = 'case-definition';

/**
 * Retrieve a single case definition
 *
 * @param  {uuid}     id    of the case definition to be requested
 * @param  {Function} done
 */
CaseDefinition.get = function(id, done) {
  return this.http.get(this.path + '/' + id, {
    done: done
  });
};

/**
 * Retrieve a single cace definition
 *
 * @param  {String}   key    of the case definition to be requested
 * @param  {Function} done
 */
CaseDefinition.getByKey = function(key, done) {
  return this.http.get(this.path + '/key/' + key, {
    done: done
  });
};

CaseDefinition.list = function(params, done) {
  return this.http.get(this.path, {
    data: params,
    done: done
  });
};

/**
 * Instantiates a given case definition.
 *
 * @param {Object} [params]
 * @param {String} [params.id]              The id of the case definition to be instantiated. Must be omitted if key is provided.
 * @param {String} [params.key]             The key of the case definition (the latest version thereof) to be instantiated. Must be omitted if id is provided.
 * @param {String} [params.variables]       A JSON object containing the variables the case is to be initialized with. Each key corresponds to a variable name and each value to a variable value.
 * @param {String} [params.businessKey]     The business key the case instance is to be initialized with. The business key identifies the case instance in the context of the given case definition.
 */
CaseDefinition.create = function(params, done) {
  var url = this.path + '/';

  if (params.id) {
    url = url + params.id;
  } else {
    url = url + 'key/' + params.key;

    if (params.tenantId) {
      url = url + '/tenant-id/' + params.tenantId;
    }
  }

  return this.http.post(url + '/create', {
    data: params,
    done: done
  });
};

/**
 * Retrieves the CMMN XML of this case definition.
 * @param  {uuid}     id   The id of the case definition.
 * @param  {Function} done
 */
CaseDefinition.xml = function(data, done) {
  var path = this.path + '/' + (data.id ? data.id : 'key/' + data.key) + '/xml';
  return this.http.get(path, {
    done: done || noop
  });
};

/**
 * Instantiates a given process definition.
 *
 * @param {String} [id]                        The id of the process definition to activate or suspend.
 * @param {Object} [params]
 * @param {Number} [params.historyTimeToLive]  New value for historyTimeToLive field of process definition. Can be null.
 */
CaseDefinition.updateHistoryTimeToLive = function(id, params, done) {
  var url = this.path + '/' + id + '/history-time-to-live';

  return this.http.put(url, {
    data: params,
    done: done
  });
};

module.exports = CaseDefinition;
