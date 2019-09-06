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

var Q = require('q');
var AbstractClientResource = require('./../abstract-client-resource');

/**
 * No-Op callback
 */
function noop() {}

/**
 * Process Definition Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var ProcessDefinition = AbstractClientResource.extend(
  /** @lends  CamSDK.client.resource.ProcessDefinition.prototype */
  {
    /**
     * Suspends the process definition instance
     *
     * @param  {Object.<String, *>} [params]
     * @param  {requestCallback}    [done]
     */
    suspend: function(params, done) {
      // allows to pass only a callback
      if (typeof params === 'function') {
        done = params;
        params = {};
      }
      params = params || {};
      done = done || noop;

      return this.http.post(this.path, {
        done: done
      });
    },

    /**
     * Retrieves the statistics of a process definition.
     *
     * @param  {Function} [done]
     */
    stats: function(done) {
      return this.http.post(this.path, {
        done: done || noop
      });
    },

    /**
     * Retrieves the BPMN 2.0 XML document of a process definition.
     *
     * @param  {Function} [done]
     */
    // xml: function(id, done) {
    //   return this.http.post(this.path + +'/xml', {
    //     done: done || noop
    //   });
    // },

    /**
     * Starts a process instance from a process definition.
     *
     * @param  {Object} [varname]
     * @param  {Function} [done]
     */
    start: function(done) {
      return this.http.post(this.path, {
        data: {},
        done: done
      });
    }
  },
  /** @lends  CamSDK.client.resource.ProcessDefinition */
  {
    /**
     * API path for the process instance resource
     */
    path: 'process-definition',

    /**
     * Retrieve a single process definition
     *
     * @param  {uuid}     id    of the process definition to be requested
     * @param  {Function} done
     */
    get: function(id, done) {
      // var pointer = '';
      // if (data.key) {
      //   pointer = 'key/'+ data.key;
      // }
      // else if (data.id) {
      //   pointer = data.id;
      // }

      return this.http.get(this.path + '/' + id, {
        done: done
      });
    },

    /**
     * Retrieve a single process definition
     *
     * @param  {String}   key    of the process definition to be requested
     * @param  {Function} done
     */
    getByKey: function(key, done) {
      return this.http.get(this.path + '/key/' + key, {
        done: done
      });
    },

    /**
     * Get a list of process definitions
     * @param  {Object} params                        Query parameters as follow
     * @param  {String} [params.name]                 Filter by name.
     * @param  {String} [params.nameLike]             Filter by names that the parameter is a substring of.
     * @param  {String} [params.deploymentId]         Filter by the deployment the id belongs to.
     * @param  {String} [params.key]                  Filter by key, i.e. the id in the BPMN 2.0 XML. Exact match.
     * @param  {String} [params.keyLike]              Filter by keys that the parameter is a substring of.
     * @param  {String} [params.category]             Filter by category. Exact match.
     * @param  {String} [params.categoryLike]         Filter by categories that the parameter is a substring of.
     * @param  {String} [params.ver]                  Filter by version.
     * @param  {String} [params.latest]               Only include those process definitions that are latest versions.
     *                                                Values may be "true" or "false".
     * @param  {String} [params.resourceName]         Filter by the name of the process definition resource. Exact match.
     * @param  {String} [params.resourceNameLike]     Filter by names of those process definition resources that the parameter is a substring of.
     * @param  {String} [params.startableBy]          Filter by a user name who is allowed to start the process.
     * @param  {String} [params.active]               Only include active process definitions.
     *                                                Values may be "true" or "false".
     * @param  {String} [params.suspended]            Only include suspended process definitions.
     *                                                Values may be "true" or "false".
     * @param  {String} [params.incidentId]           Filter by the incident id.
     * @param  {String} [params.incidentType]         Filter by the incident type.
     * @param  {String} [params.incidentMessage]      Filter by the incident message. Exact match.
     * @param  {String} [params.incidentMessageLike]  Filter by the incident message that the parameter is a substring of.
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

     * @param  {requestCallback} [done]
     *
     * @example
     * CamSDK.resource('process-definition').list({
     *   nameLike: 'Process'
     * }, function(err, results) {
     *   //
     * });
     */
    list: function() {
      return AbstractClientResource.list.apply(this, arguments);
    },

    /**
     * Get a count of process definitions
     * Same parameters as list
     */
    count: function() {
      return AbstractClientResource.count.apply(this, arguments);
    },

    /**
     * Fetch the variables of a process definition
     * @param  {Object.<String, *>} data
     * @param  {String}             [data.id]     of the process
     * @param  {String}             [data.key]    of the process
     * @param  {Array}              [data.names]  of variables to be fetched
     * @param  {Function}           [done]
     */
    formVariables: function(data, done) {
      var pointer = '';
      done = done || noop;
      if (data.key) {
        pointer = 'key/' + data.key;
      } else if (data.id) {
        pointer = data.id;
      } else {
        var err = new Error(
          'Process definition task variables needs either a key or an id.'
        );
        done(err);
        return Q.reject(err);
      }

      var queryData = {
        deserializeValues: data.deserializeValues
      };

      if (data.names) {
        queryData.variableNames = (data.names || []).join(',');
      }

      return this.http.get(this.path + '/' + pointer + '/form-variables', {
        data: queryData,
        done: done
      });
    },

    /**
     * Submit a form to start a process definition
     *
     * @param  {Object.<String, *>} data
     * @param  {String}             [data.key]            start the process-definition with this key
     * @param  {String}             [data.tenantId]       and the this tenant-id
     * @param  {String}             [data.id]             or: start the process-definition with this id
     * @param  {String}             [data.businessKey]    of the process to be set
     * @param  {Array}              [data.variables]      variables to be set
     * @param  {Function}           [done]
     */
    submitForm: function(data, done) {
      var pointer = '';
      done = done || noop;
      if (data.key) {
        pointer = 'key/' + data.key;
        if (data.tenantId) {
          pointer += '/tenant-id/' + data.tenantId;
        }
      } else if (data.id) {
        pointer = data.id;
      } else {
        return done(
          new Error(
            'Process definition task variables needs either a key or an id.'
          )
        );
      }

      return this.http.post(this.path + '/' + pointer + '/submit-form', {
        data: {
          businessKey: data.businessKey,
          variables: data.variables
        },
        done: done
      });
    },

    /**
     * Delete multiple process definitions by key or a single process definition by id
     *
     * @param  {Object.<String, *>} data
     * @param  {String}             [data.key]                        delete the process-definition with this key
     * @param  {String}             [data.tenantId]                   and the this tenant-id
     * @param  {String}             [data.id]                         or: delete the process-definition with this id
     * @param  {Boolean}            [data.cascade]                    All instances, including historic instances,
     *                                                                will also be deleted
     * @param  {Boolean}            [data.skipCustomListeners]        Skip execution listener invocation for
     *                                                                activities that are started or ended
     *                                                                as part of this request.
     * @param  {Function}           [done]
     */
    delete: function(data, done) {
      done = done || noop;

      var pointer = '';
      if (data.key) {
        pointer = 'key/' + data.key;
        if (data.tenantId) {
          pointer += '/tenant-id/' + data.tenantId;
        }
        pointer += '/delete';
      } else if (data.id) {
        pointer = data.id;
      } else {
        return done(
          new Error('Process definition deletion needs either a key or an id.')
        );
      }

      var queryParams = '?';
      var param = 'cascade';
      if (typeof data[param] === 'boolean') {
        queryParams += param + '=' + data[param];
      }

      param = 'skipCustomListeners';
      if (typeof data[param] === 'boolean') {
        if (queryParams.length > 1) {
          queryParams += '&';
        }

        queryParams += param + '=' + data[param];
      }

      param = 'skipIoMappings';
      if (typeof data[param] === 'boolean') {
        if (queryParams.length > 1) {
          queryParams += '&';
        }

        queryParams += param + '=' + data[param];
      }

      return this.http.del(this.path + '/' + pointer + queryParams, {
        done: done
      });
    },

    /**
     * Retrieves the form of a process definition.
     * @param  {Function} [done]
     */
    startForm: function(data, done) {
      var path =
        this.path +
        '/' +
        (data.key ? 'key/' + data.key : data.id) +
        '/startForm';
      return this.http.get(path, {
        done: done || noop
      });
    },

    /**
     * Retrieves the form of a process definition.
     * @param  {Function} [done]
     */
    xml: function(data, done) {
      var path =
        this.path + '/' + (data.id ? data.id : 'key/' + data.key) + '/xml';
      return this.http.get(path, {
        done: done || noop
      });
    },

    /**
     * Retrieves runtime statistics of a given process definition grouped by activities
     * @param  {Function} [done]
     */
    statistics: function(data, done) {
      var path = this.path;

      if (data.id) {
        path += '/' + data.id;
      } else if (data.key) {
        path += '/key/' + data.key;
      }

      path += '/statistics';

      return this.http.get(path, {
        data: data,
        done: done || noop
      });
    },

    /**
     * Submits the form of a process definition.
     *
     * @param  {Object} [data]
     * @param  {Function} [done]
     */
    submit: function(data, done) {
      var path = this.path;
      if (data.key) {
        path += '/key/' + data.key;
      } else {
        path += '/' + data.id;
      }
      path += '/submit-form';

      return this.http.post(path, {
        data: data,
        done: done
      });
    },

    /**
     * Suspends one or more process definitions
     *
     * @param  {String|String[]}    ids
     * @param  {Object.<String, *>} [params]
     * @param  {requestCallback}    [done]
     */
    suspend: function(ids, params, done) {
      // allows to pass only a callback
      if (typeof params === 'function') {
        done = params;
        params = {};
      }
      params = params || {};
      done = done || noop;
      // allows to pass a single ID
      ids = Array.isArray(ids) ? ids : [ids];

      return this.http.post(this.path, {
        done: done
      });
    },

    /**
     * Instantiates a given process definition.
     *
     * @param {Object} [params]
     * @param {String} [params.id]              The id of the process definition to be instantiated. Must be omitted if key is provided.
     * @param {String} [params.key]             The key of the process definition (the latest version thereof) to be instantiated. Must be omitted if id is provided.
     * @param {String} [params.tenantId]				The id of the tenant the process definition belongs to. Must be omitted if id is provided.
     * @param {String} [params.variables]       A JSON object containing the variables the process is to be initialized with. Each key corresponds to a variable name and each value to a variable value.
     * @param {String} [params.businessKey]     The business key the process instance is to be initialized with. The business key uniquely identifies the process instance in the context of the given process definition.
     * @param {String} [params.caseInstanceId]  The case instance id the process instance is to be initialized with.
     */
    start: function(params, done) {
      var url = this.path + '/';

      if (params.id) {
        url = url + params.id;
      } else {
        url = url + 'key/' + params.key;

        if (params.tenantId) {
          url = url + '/tenant-id/' + params.tenantId;
        }
      }

      return this.http.post(url + '/start', {
        data: params,
        done: done
      });
    },

    /**
     * Instantiates a given process definition.

     * @param {String} [id]                        The id of the process definition to activate or suspend.
     * @param {Object} [params]
     * @param {Number} [params.historyTimeToLive]  New value for historyTimeToLive field of process definition. Can be null.
     */
    updateHistoryTimeToLive: function(id, params, done) {
      var url = this.path + '/' + id + '/history-time-to-live';

      return this.http.put(url, {
        data: params,
        done: done
      });
    },

    restart: function(id, params, done) {
      var url = this.path + '/' + id + '/restart';

      return this.http.post(url, {
        data: params,
        done: done
      });
    },

    restartAsync: function(id, params, done) {
      var url = this.path + '/' + id + '/restart-async';

      return this.http.post(url, {
        data: params,
        done: done
      });
    }
  }
);

module.exports = ProcessDefinition;
