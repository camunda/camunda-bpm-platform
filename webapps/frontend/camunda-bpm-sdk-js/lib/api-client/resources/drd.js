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

var AbstractClientResource = require('../abstract-client-resource');
var utils = require('../../utils');

/**
 * DRD (Decision Requirements Definition) Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var DRD = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
DRD.path = 'decision-requirements-definition';

/**
 * Fetch a  count of DRD's
 * @param  {Object} params                          Query parameters as follow
 * @param  {String} [params.decisionDefinitionId]   Filter by decision definition id.
 * @param  {String} [params.decisionDefinitionIdIn] Filter by decision definition ids.
 * @param  {String} [params.name]                   Filter by name.
 * @param  {String} [params.nameLike]               Filter by names that the parameter is a substring of.
 * @param  {String} [params.deploymentId]           Filter by the deployment the id belongs to.
 * @param  {String} [params.key]                    Filter by key, i.e. the id in the DMN 1.0 XML. Exact match.
 * @param  {String} [params.keyLike]                Filter by keys that the parameter is a substring of.
 * @param  {String} [params.category]               Filter by category. Exact match.
 * @param  {String} [params.categoryLike]           Filter by categories that the parameter is a substring of.
 * @param  {String} [params.version]                Filter by version.
 * @param  {String} [params.latestVersion]          Only include those decision definitions that are latest versions.
 *                                                  Values may be "true" or "false".
 * @param  {String} [params.resourceName]           Filter by the name of the decision definition resource. Exact match.
 * @param  {String} [params.resourceNameLike]       Filter by names of those decision definition resources that the parameter is a substring of.
 *
 * @param  {String} [params.tenantIdInIdLn]         Filter by a comma-separated list of tenant ids. A decision requirements definition
 *                                                  must have one of the given tenant ids.
 *
 * @param  {Boolean} [params.withoutTenantId]       Only include decision requirements definitions which belongs to no tenant.
 *                                                  Value may only be true, as false is the default behavior.
 *
 * @param  {String} [params.includeDecisionRequirementsDefinitionsWithoutTenantId] Include decision requirements definitions which belongs to no tenant.
 *                                                  Can be used in combination with tenantIdIn. Value may only be true, as false is the default behavior.
 * @param {Function} done
 */
DRD.count = function(params, done) {
  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(this.path + '/count', {
    data: params,
    done: done
  });
};

/**
 * Fetch a list of decision definitions
 * @param  {Object} params                        Query parameters as follow
 * @param  {String} [params.decisionDefinitionId] Filter by decision definition id.
 * @param  {String} [params.decisionDefinitionIdIn] Filter by decision definition ids.
 * @param  {String} [params.name]                 Filter by name.
 * @param  {String} [params.nameLike]             Filter by names that the parameter is a substring of.
 * @param  {String} [params.deploymentId]         Filter by the deployment the id belongs to.
 * @param  {String} [params.key]                  Filter by key, i.e. the id in the DMN 1.0 XML. Exact match.
 * @param  {String} [params.keyLike]              Filter by keys that the parameter is a substring of.
 * @param  {String} [params.category]             Filter by category. Exact match.
 * @param  {String} [params.categoryLike]         Filter by categories that the parameter is a substring of.
 * @param  {String} [params.version]              Filter by version.
 * @param  {String} [params.latestVersion]        Only include those decision definitions that are latest versions.
 *                                                Values may be "true" or "false".
 * @param  {String} [params.resourceName]         Filter by the name of the decision definition resource. Exact match.
 * @param  {String} [params.resourceNameLike]     Filter by names of those decision definition resources that the parameter is a substring of.
 *
 * @param  {String} [params.tenantIdInIdLn]       Filter by a comma-separated list of tenant ids. A decision requirements definition
 *                                                must have one of the given tenant ids.
 *
 * @param  {Boolean} [params.withoutTenantId]     Only include decision requirements definitions which belongs to no tenant.
 *                                                Value may only be true, as false is the default behavior.
 *
 * @param  {String} [params.includeDecisionRequirementsDefinitionsWithoutTenantId] Include decision requirements definitions which belongs to no tenant.
 *                                                  Can be used in combination with tenantIdIn. Value may only be true, as false is the default behavior.
 *
 * @param  {String} [params.sortBy]               Sort the results lexicographically by a given criterion.
 *                                                Valid values are category, "key", "id", "name", "version" and "deploymentId".
 *                                                Must be used in conjunction with the "sortOrder" parameter.
 *
 * @param  {String} [params.sortOrder]            Sort the results in a given order.
 *                                                Values may be asc for ascending "order" or "desc" for descending order.
 *                                                Must be used in conjunction with the sortBy parameter.
 *
 * @param  {Integer} [params.firstResult]         Pagination of results. Specifies the index of the first result to return.
 * @param  {Integer} [params.maxResults]          Pagination of results. Specifies the maximum number of results to return.
 *                                                Will return less results, if there are no more results left.
 * @param {Function} done
 */
DRD.list = function(params, done) {
  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(this.path, {
    data: params,
    done: done
  });
};

function createIdUrl(path, id) {
  return path + '/' + utils.escapeUrl(id);
}

/**
 * Retrieves a single decision requirements definition.
 * @param  {uuid}     id   The id of the decision definition to be retrieved.
 * @param  {Function} done
 */
DRD.get = function(id, done) {
  return this.http.get(createIdUrl(this.path, id), {
    done: done
  });
};

function createKeyTenantUrl(path, key, tenantId) {
  var url = path + '/key/' + utils.escapeUrl(key);

  if (typeof tenantId !== 'function') {
    url += '/tenant-id/' + utils.escapeUrl(tenantId);
  }

  return url;
}

/**
 * Retrieves a single decision requirements definition.
 * @param  {string}     key   The key of the decision requirements definition (the latest version thereof) to be retrieved.
 * @param  {uuid}     [tenantId]   The id of the tenant to which the decision requirements definition belongs to.
 * @param  {Function} done
 */
DRD.getByKey = function(key, tenantId, done) {
  var url = createKeyTenantUrl(this.path, key, tenantId);

  if (typeof tenantId === 'function') {
    done = tenantId;
  }

  return this.http.get(url, {
    done: done
  });
};

/**
 * Retrieves the DMN XML of this decision requirements definition.
 * @param  {uuid}     id   The id of the decision definition to be retrieved.
 * @param  {Function} done
 */
DRD.getXML = function(id, done) {
  return this.http.get(createIdUrl(this.path, id) + '/xml', {
    done: done
  });
};

/**
 * Retrieves the DMN XML of this decision requirements definition.
 * @param  {string}     key   The key of the decision requirements definition (the latest version thereof) to be retrieved.
 * @param  {uuid}     [tenantId]   The id of the tenant to which the decision requirements definition belongs to.
 * @param  {Function} done
 */
DRD.getXMLByKey = function(key, tenantId, done) {
  var url = createKeyTenantUrl(this.path, key, tenantId) + '/xml';

  if (typeof tenantId === 'function') {
    done = tenantId;
  }

  return this.http.get(url, {
    done: done
  });
};

module.exports = DRD;
