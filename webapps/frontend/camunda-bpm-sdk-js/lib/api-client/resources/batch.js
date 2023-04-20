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
 * Batch Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Batch = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Batch.path = 'batch';

/**
 * Retrieves a single batch according to the Batch interface in the engine.
 */
Batch.get = function(id, done) {
  return this.http.get(this.path + '/' + id, {
    done: done
  });
};

Batch.suspended = function(params, done) {
  return this.http.put(this.path + '/' + params.id + '/suspended', {
    data: {
      suspended: !!params.suspended
    },
    done: done
  });
};

Batch.statistics = function(params, done) {
  return this.http.get(this.path + '/statistics/', {
    data: params,
    done: done
  });
};

Batch.statisticsCount = function(params, done) {
  return this.http.get(this.path + '/statistics/count', {
    data: params,
    done: done
  });
};

Batch.delete = function(params, done) {
  var path = this.path + '/' + params.id;

  if (params.cascade) {
    path += '?cascade=true';
  }

  return this.http.del(path, {
    done: done
  });
};

module.exports = Batch;
