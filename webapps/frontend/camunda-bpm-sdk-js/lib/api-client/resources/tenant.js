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
 * No-Op callback
 */
function noop() {}

/**
 * Group Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Tenant = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Tenant.path = 'tenant';

/**
 * Creates a tenant
 *
 * @param  {Object}   tenant       is an object representation of a group
 * @param  {String}   tenant.id
 * @param  {String}   tenant.name
 * @param  {Function} done
 */
Tenant.create = function(options, done) {
  return this.http.post(this.path + '/create', {
    data: options,
    done: done || noop
  });
};

/**
 * Query for tenants using a list of parameters and retrieves the count
 *
 * @param {String} [options.id]           Filter by the id of the tenant.
 * @param {String} [options.name]         Filter by the name of the tenant.
 * @param {String} [options.nameLike]     Filter by the name that the parameter is a substring of.
 * @param {String} [options.userMember]   Only retrieve tenants where the given user id is a member of.
 * @param {String} [options.groupMember]  Only retrieve tenants where the given group id is a member of.
 * @param  {Function} done
 */
Tenant.count = function(options, done) {
  if (typeof options === 'function') {
    done = options;
    options = {};
  } else {
    options = options || {};
  }

  return this.http.get(this.path + '/count', {
    data: options,
    done: done || noop
  });
};

/**
 * Retrieves a single tenant
 *
 * @param  {String} [options.id]    The id of the tenant, can be a property (id) of an object
 * @param  {Function} done
 */
Tenant.get = function(options, done) {
  var id;

  if (typeof options === 'string') {
    id = options;
    options = {};
  } else {
    id = options.id;
    delete options.id;
  }

  return this.http.get(this.path + '/' + utils.escapeUrl(id), {
    data: options,
    done: done || noop
  });
};

/**
 * Query for a list of tenants using a list of parameters.
 * The size of the result set can be retrieved by using the get tenants count method
 *
 * @param {String} [options.id]           Filter by the id of the tenant.
 * @param {String} [options.name]         Filter by the name of the tenant.
 * @param {String} [options.nameLike]     Filter by the name that the parameter is a substring of.
 * @param {String} [options.userMember]   Only retrieve tenants where the given user id is a member of.
 * @param {String} [options.grouprMember] Only retrieve tenants where the given group id is a member of.
 * @param {String} [options.sortBy]       Sort the results lexicographically by a given criterion.
 *                                        Valid values are id and name.
 *                                        Must be used in conjunction with the sortOrder parameter.
 * @param {String} [options.sortOrder]    Sort the results in a given order.
 *                                        Values may be asc for ascending order or desc for descending order.
 *                                        Must be used in conjunction with the sortBy parameter.
 * @param {String} [options.firstResult]  Pagination of results.
 *                                        Specifies the index of the first result to return.
 * @param {String} [options.maxResults]   Pagination of results.
 *                                        Specifies the maximum number of results to return.
 *                                        Will return less results if there are no more results left.
 *
 * @param  {Function} done
 */
Tenant.list = function(options, done) {
  if (typeof options === 'function') {
    done = options;
    options = {};
  } else {
    options = options || {};
  }

  return this.http.get(this.path, {
    data: options,
    done: done || noop
  });
};

/**
 * Add a user member to a tenant
 *
 * @param {String} [options.id]       The id of the tenant
 * @param {String} [options.userId]   The id of user to add to the tenant
 * @param  {Function} done
 */
Tenant.createUserMember = function(options, done) {
  return this.http.put(
    this.path +
      '/' +
      utils.escapeUrl(options.id) +
      '/user-members/' +
      utils.escapeUrl(options.userId),
    {
      data: options,
      done: done || noop
    }
  );
};

/**
 * Add a group member to a tenant
 *
 * @param {String} [options.id]       The id of the tenant
 * @param {String} [options.groupId]   The id of group to add to the tenant
 * @param  {Function} done
 */
Tenant.createGroupMember = function(options, done) {
  return this.http.put(
    this.path +
      '/' +
      utils.escapeUrl(options.id) +
      '/group-members/' +
      utils.escapeUrl(options.groupId),
    {
      data: options,
      done: done || noop
    }
  );
};

/**
 * Removes a user member of a tenant
 *
 * @param {String} [options.id]       The id of the tenant
 * @param {String} [options.userId]   The id of user to add to the tenant
 * @param  {Function} done
 */
Tenant.deleteUserMember = function(options, done) {
  return this.http.del(
    this.path +
      '/' +
      utils.escapeUrl(options.id) +
      '/user-members/' +
      utils.escapeUrl(options.userId),
    {
      data: options,
      done: done || noop
    }
  );
};

/**
 * Removes a group member of a Tenant
 *
 * @param {String} [options.id]       The id of the tenant
 * @param {String} [options.groupId]   The id of group to add to the tenant
 * @param  {Function} done
 */
Tenant.deleteGroupMember = function(options, done) {
  return this.http.del(
    this.path +
      '/' +
      utils.escapeUrl(options.id) +
      '/group-members/' +
      utils.escapeUrl(options.groupId),
    {
      data: options,
      done: done || noop
    }
  );
};

/**
 * Update a tenant
 *
 * @param  {Object}   tenant   is an object representation of a tenant
 * @param  {Function} done
 */
Tenant.update = function(options, done) {
  return this.http.put(this.path + '/' + utils.escapeUrl(options.id), {
    data: options,
    done: done || noop
  });
};

/**
 * Delete a tenant
 *
 * @param  {Object}   tenant   is an object representation of a tenant
 * @param  {Function} done
 */
Tenant.delete = function(options, done) {
  return this.http.del(this.path + '/' + utils.escapeUrl(options.id), {
    data: options,
    done: done || noop
  });
};

Tenant.options = function(options, done) {
  var id;

  if (typeof options === 'function') {
    done = options;
    id = '';
  } else {
    id = typeof options === 'string' ? options : options.id;
    if (id === undefined) {
      id = '';
    }
  }

  return this.http.options(this.path + '/' + utils.escapeUrl(id), {
    done: done || noop,
    headers: {
      Accept: 'application/json'
    }
  });
};
module.exports = Tenant;
