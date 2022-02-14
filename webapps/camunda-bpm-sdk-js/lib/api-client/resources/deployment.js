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
 * Deployment Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Deployment = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Deployment.path = 'deployment';

/**
 * Create a deployment
 * @param  {Object} options
 *
 * @param  {Array} options.files
 *
 * @param  {String} options.deploymentName
 * @param  {String} [options.deploymentSource]
 * @param  {String} [options.enableDuplicateFiltering]
 * @param  {String} [options.deployChangedOnly]
 * @param	 {String} [options.tenantId]
 * @param  {Function} done
 */
Deployment.create = function(options, done) {
  var fields = {
    'deployment-name': options.deploymentName
  };

  var files = Array.isArray(options.files) ? options.files : [options.files];

  if (options.deploymentSource) {
    fields['deployment-source'] = options.deploymentSource;
  }

  if (options.enableDuplicateFiltering) {
    fields['enable-duplicate-filtering'] = 'true';
  }

  if (options.deployChangedOnly) {
    fields['deploy-changed-only'] = 'true';
  }

  if (options.tenantId) {
    fields['tenant-id'] = options.tenantId;
  }

  return this.http.post(this.path + '/create', {
    data: {},
    fields: fields,
    attachments: files,
    done: done
  });
};

/**
 * Deletes a deployment
 *
 * @param  {String}  id
 *
 * @param  {Object}  options
 *
 * @param  {Boolean} [options.cascade]
 * @param  {Boolean} [options.skipCustomListeners]
 *
 * @param  {Function} done
 */
Deployment.delete = function(id, options, done) {
  var path = this.path + '/' + id;

  if (options) {
    var queryParams = [];
    for (var key in options) {
      var value = options[key];
      queryParams.push(key + '=' + value);
    }

    if (queryParams.length) {
      path += '?' + queryParams.join('&');
    }
  }

  return this.http.del(path, {
    done: done
  });
};

/**
 * Lists the deployments
 * @param  {Object}   params                An object containing listing options.
 * @param  {uuid}     [params.id]           Filter by deployment id.
 * @param  {String}   [params.name]         Filter by the deployment name. Exact match.
 * @param  {String}   [params.nameLike]     Filter by the deployment name that the parameter is a
 *                                          substring of. The parameter can include the wildcard %
 *                                          to express like-strategy such as: starts with (%name),
 *                                          ends with (name%) or contains (%name%).
 * @param  {String}   [params.after]        Restricts to all deployments after the given date.
 *                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss,
 *                                          e.g., 2013-01-23T14:42:45
 * @param  {String}   [params.before]       Restricts to all deployments before the given date.
 *                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss,
 *                                          e.g., 2013-01-23T14:42:45
 * @param  {String}   [params.sortBy]       Sort the results lexicographically by a given criterion.
 *                                          Valid values are id, name and deploymentTime. Must be
 *                                          used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]    Sort the results in a given order. Values may be asc for
 *                                          ascending order or desc for descending order. Must be
 *                                          used in conjunction with the sortBy parameter.
 * @param  {Integer}  [params.firstResult]  Pagination of results. Specifies the index of the first
 *                                          result to return.
 * @param  {Integer}  [params.maxResults]   Pagination of results. Specifies the maximum number of
 *                                          results to return. Will return less results if there are
 *                                          no more results left.
 * @param  {Function} done
 */
Deployment.list = function() {
  return AbstractClientResource.list.apply(this, arguments);
};

/**
 * Returns information about a deployment resources for the given deployment.
 */
Deployment.get = function(id, done) {
  return this.http.get(this.path + '/' + id, {
    done: done
  });
};

/**
 * Returns a list of deployment resources for the given deployment.
 */
Deployment.getResources = function(id, done) {
  return this.http.get(this.path + '/' + id + '/resources', {
    done: done
  });
};

/**
 * Returns a deployment resource for the given deployment and resource id.
 */
Deployment.getResource = function(deploymentId, resourceId, done) {
  return this.http.get(
    this.path + '/' + deploymentId + '/resources/' + resourceId,
    {
      done: done
    }
  );
};

/**
 * Returns the binary content of a single deployment resource for the given deployment.
 */
Deployment.getResourceData = function(deploymentId, resourceId, done) {
  return this.http.get(
    this.path + '/' + deploymentId + '/resources/' + resourceId + '/data',
    {
      accept: '*/*',
      done: done
    }
  );
};

/**
 * Redeploy a deployment

 * @param  {Object} options
 * @param  {String} options.id
 * @param  {Array} [options.resourceIds]
 * @param  {Array} [options.resourceNames]
 * @param  {Function} done
 */
Deployment.redeploy = function(options, done) {
  var id = options.id;
  delete options.id;

  return this.http.post(this.path + '/' + id + '/redeploy', {
    data: options,
    done: done || function() {}
  });
};

module.exports = Deployment;
