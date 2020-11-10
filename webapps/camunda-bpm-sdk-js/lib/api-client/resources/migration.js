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
 * Migration Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Migration = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Migration.path = 'migration';

/**
 * Generate a migration plan for a given source and target process definition
 * @param  {Object}   params
 * @param  {String}   [params.sourceProcessDefinitionId]
 * @param  {String}   [params.targetProcessDefinitionId]
 * @param  {Function} done
 */
Migration.generate = function(params, done) {
  var path = this.path + '/generate';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

/**
 * Execute a migration plan
 * @param  {Object}   params
 * @param  {String}   [params.migrationPlan]
 * @param  {String}   [params.processInstanceIds]
 * @param  {Function} done
 */
Migration.execute = function(params, done) {
  var path = this.path + '/execute';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

/**
 * Execute a migration plan asynchronously
 * @param  {Object}   params
 * @param  {String}   [params.migrationPlan]
 * @param  {String}   [params.processInstanceIds]
 * @param  {Function} done
 */
Migration.executeAsync = function(params, done) {
  var path = this.path + '/executeAsync';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

Migration.validate = function(params, done) {
  var path = this.path + '/validate';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

module.exports = Migration;
