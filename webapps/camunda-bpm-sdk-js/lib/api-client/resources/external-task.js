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
 * ExternalTask Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var ExternalTask = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
ExternalTask.path = 'external-task';

/**
 * Retrieves a single external task corresponding to the ExternalTask interface in the engine.
 *
 * @param {Object} [params]
 * @param {String} [params.id]      The id of the external task to be retrieved.
 */
ExternalTask.get = function(params, done) {
  return this.http.get(this.path + '/' + params.id, {
    data: params,
    done: done
  });
};

/**
 * Query for external tasks that fulfill given parameters in the form of a json object. This method is slightly more
 * powerful than the GET query because it allows to specify a hierarchical result sorting.
 *
 * @param {Object} [params]
 * @param {String} [params.externalTaskId]    Filter by an external task's id.
 * @param {String} [params.topicName]         Filter by an external task topic.
 * @param {String} [params.workerId]          Filter by the id of the worker that the task was most recently locked by.
 * @param {String} [params.locked]            Only include external tasks that are currently locked (i.e. they have a lock time and it has not expired). Value may only be true, as false matches any external task.
 * @param {String} [params.notLocked]         Only include external tasks that are currently not locked (i.e. they have no lock or it has expired). Value may only be true, as false matches any external task.
 * @param {String} [params.withRetriesLeft]	  Only include external tasks that have a positive (> 0) number of retries (or null). Value may only be true, as false matches any external task.
 * @param {String} [params.noRetriesLeft]	    Only include external tasks that have 0 retries. Value may only be true, as false matches any external task.
 * @param {String} [params.lockExpirationAfter]	Restrict to external tasks that have a lock that expires after a given date. The date must have the format yyyy-MM-dd'T'HH:mm:ss, e.g., 2013-01-23T14:42:45.
 * @param {String} [params.lockExpirationBefore]	Restrict to external tasks that have a lock that expires before a given date. The date must have the format yyyy-MM-dd'T'HH:mm:ss, e.g., 2013-01-23T14:42:45.
 * @param {String} [params.activityId]	      Filter by the id of the activity that an external task is created for.
 * @param {String} [params.executionId]	      Filter by the id of the execution that an external task belongs to.
 * @param {String} [params.processInstanceId]	Filter by the id of the process instance that an external task belongs to.
 * @param {String} [params.processDefinitionId]	Filter by the id of the process definition that an external task belongs to.
 * @param {String} [params.active]	          Only include active tasks. Value may only be true, as false matches any external task.
 * @param {String} [params.suspended]	        Only include suspended tasks. Value may only be true, as false matches any external task.
 * @param {String} [params.sorting]           A JSON array of criteria to sort the result by. Each element of the array is a JSON object that specifies one ordering. The position in the array identifies the rank of an ordering, i.e. whether it is primary, secondary, etc. The ordering objects have the following properties:
 *                                            - sortBy	Mandatory. Sort the results lexicographically by a given criterion. Valid values are id, lockExpirationTime, processInstanceId, processDefinitionId, and processDefinitionKey.
 *                                            - sortOrder	Mandatory. Sort the results in a given order. Values may be asc for ascending order or desc for descending order.
 * @param {String} [params.firstResult]	      Pagination of results. Specifies the index of the first result to return.
 * @param {String} [params.maxResults]	      Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 */
ExternalTask.list = function(params, done) {
  var path = this.path + '/';

  // those parameters have to be passed in the query and not body
  path += '?firstResult=' + (params.firstResult || 0);
  path += '&maxResults=' + (params.maxResults || 15);

  return this.http.post(path, {
    data: params,
    done: done
  });
};

/**
 * Query for the number of external tasks that fulfill given parameters. Takes the same parameters as the get external tasks method.
 *
 * @param {Object} [params]
 * @param {String} [params.externalTaskId]    Filter by an external task's id.
 * @param {String} [params.topicName]         Filter by an external task topic.
 * @param {String} [params.workerId]          Filter by the id of the worker that the task was most recently locked by.
 * @param {String} [params.locked]            Only include external tasks that are currently locked (i.e. they have a lock time and it has not expired). Value may only be true, as false matches any external task.
 * @param {String} [params.notLocked]         Only include external tasks that are currently not locked (i.e. they have no lock or it has expired). Value may only be true, as false matches any external task.
 * @param {String} [params.withRetriesLeft]	  Only include external tasks that have a positive (> 0) number of retries (or null). Value may only be true, as false matches any external task.
 * @param {String} [params.noRetriesLeft]	    Only include external tasks that have 0 retries. Value may only be true, as false matches any external task.
 * @param {String} [params.lockExpirationAfter]	Restrict to external tasks that have a lock that expires after a given date. The date must have the format yyyy-MM-dd'T'HH:mm:ss, e.g., 2013-01-23T14:42:45.
 * @param {String} [params.lockExpirationBefore]	Restrict to external tasks that have a lock that expires before a given date. The date must have the format yyyy-MM-dd'T'HH:mm:ss, e.g., 2013-01-23T14:42:45.
 * @param {String} [params.activityId]	      Filter by the id of the activity that an external task is created for.
 * @param {String} [params.executionId]	      Filter by the id of the execution that an external task belongs to.
 * @param {String} [params.processInstanceId]	Filter by the id of the process instance that an external task belongs to.
 * @param {String} [params.processDefinitionId]	Filter by the id of the process definition that an external task belongs to.
 * @param {String} [params.active]	          Only include active tasks. Value may only be true, as false matches any external task.
 * @param {String} [params.suspended]	        Only include suspended tasks. Value may only be true, as false matches any external task.
 */
ExternalTask.count = function(params, done) {
  return this.http.post(this.path + '/count', {
    data: params,
    done: done
  });
};

/**
 * Query for the number of external tasks that fulfill given parameters. Takes the same parameters as the get external tasks method.
 *
 * @param {Object} [params]
 * @param {String} [params.workerId]         Mandatory. The id of the worker on which behalf tasks are fetched. The returned tasks are locked for that worker and can only be completed when providing the same worker id.
 * @param {String} [params.maxTasks]         Mandatory. The maximum number of tasks to return.
 * @param {String} [params.topics]           A JSON array of topic objects for which external tasks should be fetched. The returned tasks may be arbitrarily distributed among these topics.
 *
 * Each topic object has the following properties:
 *  Name	         Description
 *  topicName	   Mandatory. The topic's name.
 *  lockDuration	 Mandatory. The duration to lock the external tasks for in milliseconds.
 *  variables	   A JSON array of String values that represent variable names. For each result task belonging to this topic, the given variables are returned as well if they are accessible from the external task's execution.
 */
ExternalTask.fetchAndLock = function(params, done) {
  return this.http.post(this.path + '/fetchAndLock', {
    data: params,
    done: done
  });
};

/**
 * Complete an external task and update process variables.
 *
 * @param {Object} [params]
 * @param {String} [params.id]            The id of the task to complete.
 * @param {String} [params.workerId]      The id of the worker that completes the task. Must match the id of the worker who has most recently locked the task.
 * @param {String} [params.variables]     A JSON object containing variable key-value pairs.
 *
 * Each key is a variable name and each value a JSON variable value object with the following properties:
 *  Name	        Description
 *  value	        The variable's value. For variables of type Object, the serialized value has to be submitted as a String value.
 *                For variables of type File the value has to be submitted as Base64 encoded string.
 *  type	        The value type of the variable.
 *  valueInfo	    A JSON object containing additional, value-type-dependent properties.
 *                For serialized variables of type Object, the following properties can be provided:
 *                - objectTypeName: A string representation of the object's type name.
 *                - serializationDataFormat: The serialization format used to store the variable.
 *                For serialized variables of type File, the following properties can be provided:
 *                - filename: The name of the file. This is not the variable name but the name that will be used when downloading the file again.
 *                - mimetype: The mime type of the file that is being uploaded.
 *                - encoding: The encoding of the file that is being uploaded.
 */
ExternalTask.complete = function(params, done) {
  return this.http.post(this.path + '/' + params.id + '/complete', {
    data: params,
    done: done
  });
};

/**
 * Report a failure to execute an external task. A number of retries and a timeout until
 * the task can be retried can be specified. If retries are set to 0, an incident for this
 * task is created.
 *
 * @param {Object} [params]
 * @param {String} [params.id]                 The id of the external task to report a failure for.
 * @param {String} [params.workerId]           The id of the worker that reports the failure. Must match the id of the worker who has most recently locked the task.
 * @param {String} [params.errorMessage]       An message indicating the reason of the failure.
 * @param {String} [params.retries]            A number of how often the task should be retried. Must be >= 0. If this is 0, an incident is created and the task cannot be fetched anymore unless the retries are increased again. The incident's message is set to the errorMessage parameter.
 * @param {String} [params.retryTimeout]       A timeout in milliseconds before the external task becomes available again for fetching. Must be >= 0.
 */
ExternalTask.failure = function(params, done) {
  return this.http.post(this.path + '/' + params.id + '/failure', {
    data: params,
    done: done
  });
};

/**
 * Unlock an external task. Clears the task’s lock expiration time and worker id.
 *
 * @param {Object} [params]
 * @param {String} [params.id]          The id of the external task to unlock.
 */
ExternalTask.unlock = function(params, done) {
  return this.http.post(this.path + '/' + params.id + '/unlock', {
    data: params,
    done: done
  });
};

/**
 * Set the number of retries left to execute an external task. If retries are set to 0, an incident is created.
 *
 * @param {Object} [params]
 * @param {String} [params.id]           The id of the external task to unlock.
 * @param {String} [params.retries]      The number of retries to set for the external task. Must be >= 0. If this is 0, an incident is created and the task cannot be fetched anymore unless the retries are increased again.
 */
ExternalTask.retries = function(params, done) {
  return this.http.put(this.path + '/' + params.id + '/retries', {
    data: params,
    done: done
  });
};

/**
 * Set the number of retries left to execute an external task asynchronously. If retries are set to 0, an incident is created.
 *
 * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/post-retries-async/
 *
 * @param   {Object}            params
 * @param   {requestCallback}   done
 */
ExternalTask.retriesAsync = function(params, done) {
  return this.http.post(this.path + '/retries-async', {
    data: params,
    done: done
  });
};

module.exports = ExternalTask;
