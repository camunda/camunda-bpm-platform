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
var Group = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Group.path = 'group';

/**
 * Check resource access
 * @param  {Object}   options
 * @param  {String}   options.id
 * @param  {Function} done
 */
Group.options = function(options, done) {
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

/**
 * Creates a group
 *
 * @param  {Object}   group       is an object representation of a group
 * @param  {String}   group.id
 * @param  {String}   group.name
 * @param  {String}   group.type
 * @param  {Function} done
 */
Group.create = function(options, done) {
  return this.http.post(this.path + '/create', {
    data: options,
    done: done || noop
  });
};

/**
 * Query for groups using a list of parameters and retrieves the count
 *
 * @param {String} [options.id]        Filter by the id of the group.
 * @param {String} [options.name]      Filter by the name of the group.
 * @param {String} [options.nameLike]  Filter by the name that the parameter is a substring of.
 * @param {String} [options.type]      Filter by the type of the group.
 * @param {String} [options.member]    Only retrieve groups where the given user id is a member of.
 * @param  {Function} done
 */
Group.count = function(options, done) {
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
 * Retrieves a single group
 *
 * @param  {String} [options.id]    The id of the group, can be a property (id) of an object
 * @param  {Function} done
 */
Group.get = function(options, done) {
  var id = typeof options === 'string' ? options : options.id;

  return this.http.get(this.path + '/' + utils.escapeUrl(id), {
    data: options,
    done: done || noop
  });
};

/**
 * Query for a list of groups using a list of parameters.
 * The size of the result set can be retrieved by using the get groups count method
 *
 * @param {String} [options.id]           Filter by the id of the group.
 * @param {String} [options.idIn]         Filter by multiple ids.
 * @param {String} [options.name]         Filter by the name of the group.
 * @param {String} [options.nameLike]     Filter by the name that the parameter is a substring of.
 * @param {String} [options.type]         Filter by the type of the group.
 * @param {String} [options.member]       Only retrieve groups where the given user id is a member of.
 * @param {String} [options.sortBy]       Sort the results lexicographically by a given criterion.
 *                                        Valid values are id, name and type.
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
Group.list = function(options, done) {
  if (typeof options === 'function') {
    done = options;
    options = {};
  } else {
    options = options || {};
  }

  var query = {};

  if (options.maxResults) {
    query.maxResults = options.maxResults;
    query.firstResult = options.firstResult;
  }

  return this.http.post(this.path, {
    data: options,
    query: query,
    done: done || noop
  });
};

/**
 * Add a memeber to a Group
 *
 * @param {String} [options.id]       The id of the group
 * @param {String} [options.userId]   The id of user to add to the group
 * @param  {Function} done
 */
Group.createMember = function(options, done) {
  return this.http.put(
    this.path +
      '/' +
      utils.escapeUrl(options.id) +
      '/members/' +
      utils.escapeUrl(options.userId),
    {
      data: options,
      done: done || noop
    }
  );
};

/**
 * Removes a memeber of a Group
 *
 * @param {String} [options.id]       The id of the group
 * @param {String} [options.userId]   The id of user to add to the group
 * @param  {Function} done
 */
Group.deleteMember = function(options, done) {
  return this.http.del(
    this.path +
      '/' +
      utils.escapeUrl(options.id) +
      '/members/' +
      utils.escapeUrl(options.userId),
    {
      data: options,
      done: done || noop
    }
  );
};

/**
 * Update a group
 *
 * @param  {Object}   group   is an object representation of a group
 * @param  {Function} done
 */
Group.update = function(options, done) {
  return this.http.put(this.path + '/' + utils.escapeUrl(options.id), {
    data: options,
    done: done || noop
  });
};

/**
 * Delete a group
 *
 * @param  {Object}   group   is an object representation of a group
 * @param  {Function} done
 */
Group.delete = function(options, done) {
  return this.http.del(this.path + '/' + utils.escapeUrl(options.id), {
    data: options,
    done: done || noop
  });
};

module.exports = Group;
