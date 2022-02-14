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
 * DecisionDefinition Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var DecisionDefinition = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
DecisionDefinition.path = 'decision-definition';

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
DecisionDefinition.list = function(params, done) {
  return this.http.get(this.path, {
    data: params,
    done: done
  });
};

/**
 * Retrieves a single decision definition according to the DecisionDefinition interface in the engine.
 * @param  {uuid}     id   The id of the decision definition to be retrieved.
 * @param  {Function} done
 */
DecisionDefinition.get = function(id, done) {
  return this.http.get(this.path + '/' + id, {
    done: done
  });
};

/**
 * Retrieves the DMN 1.0 XML of this decision definition.
 * @param  {uuid}     id   The id of the decision definition.
 * @param  {Function} done
 */
DecisionDefinition.getXml = function(id, done) {
  return this.http.get(this.path + '/' + id + '/xml', {
    done: done
  });
};

/**
 * Evaluates a given decision.
 *
 * @param {Object} [params]
 * @param {String} [params.id]              The id of the decision definition to be evaluated. Must be omitted if key is provided.
 * @param {String} [params.key]             The key of the decision definition (the latest version thereof) to be evaluated. Must be omitted if id is provided.
 * @param {String} [params.variables]       A JSON object containing the input variables of the decision. Each key corresponds to a variable name and each value to a variable value.
 */
DecisionDefinition.evaluate = function(params, done) {
  return this.http.post(
    this.path +
      '/' +
      (params.id ? params.id : 'key/' + params.key) +
      '/evaluate',
    {
      data: params,
      done: done
    }
  );
};

/**
 * Instantiates a given process definition.
 *
 * @param {String} [id]                        The id of the process definition to activate or suspend.
 * @param {Object} [params]
 * @param {Number} [params.historyTimeToLive]  New value for historyTimeToLive field of process definition. Can be null.
 */
DecisionDefinition.updateHistoryTimeToLive = function(id, params, done) {
  var url = this.path + '/' + id + '/history-time-to-live';

  return this.http.put(url, {
    data: params,
    done: done
  });
};

module.exports = DecisionDefinition;
