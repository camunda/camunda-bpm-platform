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
 * Process Instance Resource
 *
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var ProcessInstance = AbstractClientResource.extend(
  /** @lends  CamSDK.client.resource.ProcessInstance.prototype */
  {},

  /** @lends  CamSDK.client.resource.ProcessInstance */
  {
    /**
     * API path for the process instance resource
     */
    path: 'process-instance',

    /**
     * Retrieve a single process instance
     *
     * @param  {uuid}     id    of the process instance to be requested
     * @param  {Function} done
     */
    get: function(id, done) {
      return this.http.get(this.path + '/' + id, {
        done: done
      });
    },

    /**
     * Creates a process instance from a process definition
     *
     * @param  {Object}   params
     * @param  {String}   [params.id]
     * @param  {String}   [params.key]
     * @param  {Object.<String, *>} [params.variables]
     * @param  {requestCallback} [done]
     */
    create: function(params, done) {
      return this.http.post(params, done);
    },

    list: function(params, done) {
      var path = this.path;

      // those parameters have to be passed in the query and not body
      path += '?firstResult=' + (params.firstResult || 0);
      path += '&maxResults=' + (params.maxResults || 15);

      return this.http.post(path, {
        data: params,
        done: done
      });
    },

    count: function(params, done) {
      var path = this.path + '/count';

      return this.http.post(path, {
        data: params,
        done: done
      });
    },

    getActivityInstances: function(id, done) {
      return this.http.get(this.path + '/' + id + '/activity-instances', {
        done: done
      });
    },

    /**
     * Post process instance modifications
     * @see http://docs.camunda.org/api-references/rest/#process-instance-modify-process-instance-execution-state-method
     *
     * @param  {Object}           params
     * @param  {UUID}             params.id                     process instance UUID
     *
     * @param  {Array}            params.instructions           Array of instructions
     *
     * @param  {Boolean}          [params.skipCustomListeners]  Skip execution listener invocation for
     *                                                          activities that are started or ended
     *                                                          as part of this request.
     *
     * @param  {Boolean}          [params.skipIoMappings]       Skip execution of input/output
     *                                                          variable mappings for activities that
     *                                                          are started or ended as part of
     *                                                          this request.
     *
     * @param  {String}          [params.annotation]            Add Annotation to the user operation log
     *
     * @param  {requestCallback}  done
     */
    modify: function(params, done) {
      return this.http.post(this.path + '/' + params.id + '/modification', {
        data: params,
        done: done
      });
    },

    modifyAsync: function(params, done) {
      return this.http.post(
        this.path + '/' + params.id + '/modification-async',
        {
          data: params,
          done: done
        }
      );
    },

    /**
     * Delete multiple process instances asynchronously (batch).
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-delete/
     *
     * @param   {Object}            payload
     * @param   {requestCallback}   done
     *
     */
    deleteAsync: function(payload, done) {
      return this.http.post(this.path + '/delete', {
        data: payload,
        done: done
      });
    },

    /**
     * Delete a set of process instances asynchronously (batch) based on a historic process instance query.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-delete-historic-query-based/
     *
     * @param   {Object}            payload
     * @param   {requestCallback}   done
     *
     */
    deleteAsyncHistoricQueryBased: function(payload, done) {
      return this.http.post(this.path + '/delete-historic-query-based', {
        data: payload,
        done: done
      });
    },

    /**
     * Set retries of jobs belonging to process instances asynchronously (batch).
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-set-job-retries
     *
     * @param   {Object}            payload
     * @param   {requestCallback}   done
     *
     */
    setJobsRetriesAsync: function(payload, done) {
      return this.http.post(this.path + '/job-retries', {
        data: payload,
        done: done
      });
    },

    /**
     * Create a batch to set retries of jobs asynchronously based on a historic process instance query.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-set-job-retries-historic-query-based
     *
     * @param   {Object}            payload
     * @param   {requestCallback}   done
     *
     */
    setJobsRetriesAsyncHistoricQueryBased: function(payload, done) {
      return this.http.post(this.path + '/job-retries-historic-query-based', {
        data: payload,
        done: done
      });
    },

    /**
     * Activates or suspends process instances asynchronously with a list of process instance ids, a process instance query, and/or a historical process instance query
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-activate-suspend-in-batch/
     *
     * @param   {Object}            payload
     * @param   {requestCallback}   done
     */
    suspendAsync: function(payload, done) {
      return this.http.post(this.path + '/suspended-async', {
        data: payload,
        done: done
      });
    },

    /**
     * Sets a variable of a given process instance by id.
     *
     * @see http://docs.camunda.org/manual/develop/reference/rest/process-instance/variables/put-variable/
     *
     * @param   {uuid}              id
     * @param   {Object}            params
     * @param   {requestCallback}   done
     */
    setVariable: function(id, params, done) {
      var url =
        this.path + '/' + id + '/variables/' + utils.escapeUrl(params.name);
      return this.http.put(url, {
        data: params,
        done: done
      });
    }
  }
);

module.exports = ProcessInstance;
