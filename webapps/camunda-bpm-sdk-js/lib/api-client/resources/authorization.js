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
 * Authorization Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Authorization = AbstractClientResource.extend();

/**
 * API path for the process definition resource
 * @type {String}
 */
Authorization.path = 'authorization';

/**
 * Fetch a list of authorizations
 *
 * @param {Object} params
 * @param {Object} [params.id]            Authorization by the id of the authorization.
 * @param {Object} [params.type]          Authorization by the type of the authorization.
 * @param {Object} [params.userIdIn]      Authorization by a comma-separated list of userIds
 * @param {Object} [params.groupIdIn]     Authorization by a comma-separated list of groupIds
 * @param {Object} [params.resourceType]  Authorization by resource type
 * @param {Object} [params.resourceId]    Authorization by resource id.
 * @param {Object} [params.sortBy]        Sort the results lexicographically by a given criterion.
 *                                        Valid values are resourceType and resourceId.
 *                                        Must be used with the sortOrder parameter.
 * @param {Object} [params.sortOrder]     Sort the results in a given order.
 *                                        Values may be "asc" or "desc".
 *                                        Must be used in conjunction with the sortBy parameter.
 * @param {Object} [params.firstResult]   Pagination of results.
 *                                        Specifies the index of the first result to return.
 * @param {Object} [params.maxResults]    Pagination of results.
 *                                        Specifies the maximum number of results to return.
 * @param {Function} done
 */
Authorization.list = function(params, done) {
  return this.http.get(this.path, {
    data: params,
    done: done
  });
};

/**
 * Retrieve a single authorization
 *
 * @param  {uuid}     authorizationId     of the authorization to be requested
 * @param  {Function} done
 */
Authorization.get = function(authorizationId, done) {
  return this.http.get(this.path + '/' + authorizationId, {
    done: done
  });
};

/**
 * Creates an authorization
 *
 * @param  {Object}   authorization       is an object representation of an authorization
 * @param  {Function} done
 */
Authorization.create = function(authorization, done) {
  return this.http.post(this.path + '/create', {
    data: authorization,
    done: done
  });
};

/**
 * Update an authorization
 *
 * @param  {Object}   authorization       is an object representation of an authorization
 * @param  {Function} done
 */
Authorization.update = function(authorization, done) {
  return this.http.put(this.path + '/' + authorization.id, {
    data: authorization,
    done: done
  });
};

/**
 * Save an authorization
 *
 * @see Authorization.create
 * @see Authorization.update
 *
 * @param  {Object}   authorization   is an object representation of an authorization,
 *                                    if it has an id property, the authorization will be updated,
 *                                    otherwise created
 * @param  {Function} done
 */
Authorization.save = function(authorization, done) {
  return Authorization[authorization.id ? 'update' : 'create'](
    authorization,
    done
  );
};

/**
 * Delete an authorization
 *
 * @param  {uuid}     id   of the authorization to delete
 * @param  {Function} done
 */
Authorization.delete = function(id, done) {
  return this.http.del(this.path + '/' + id, {
    done: done
  });
};

Authorization.check = function(authorization, done) {
  return this.http.get(this.path + '/check', {
    data: authorization,
    done: done
  });
};

module.exports = Authorization;
