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
 * Job Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Job = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Job.path = 'job';

Job.get = function(id, done) {
  return this.http.get(this.path + '/' + id, {
    done: done
  });
};

/**
 * Query for jobs that fulfill given parameters.
 * @param  {Object}   params
 * @param  {String}   [params.jobId]                Filter by job id.
 * @param  {String}   [params.processInstanceId]    Only select jobs which exist for the given process instance.
 * @param  {String}   [params.executionId]          Only select jobs which exist for the given execution.
 * @param  {String}   [params.processDefinitionId]  Filter by the id of the process definition the jobs run on.
 * @param  {String}   [params.processDefinitionKey] Filter by the key of the process definition the jobs run on.
 * @param  {String}   [params.activityId]           Only select jobs which exist for an activity with the given id.
 * @param  {Bool}     [params.withRetriesLeft]      Only select jobs which have retries left.
 * @param  {Bool}     [params.executable]           Only select jobs which are executable, ie. retries > 0 and due date is null or due date is in the past.
 * @param  {Bool}     [params.timers]               Only select jobs that are timers. Cannot be used together with messages.
 * @param  {Bool}     [params.messages]             Only select jobs that are messages. Cannot be used together with timers.
 * @param  {String}   [params.dueDates]             Only select jobs where the due date is lower or higher than the given date. Due date expressions are comma-separated and are structured as follows:
 *                                                  A valid condition value has the form operator_value. operator is the comparison operator to be used and value the date value as string.
 *                                                  Valid operator values are: gt - greater than; lt - lower than.
 *                                                  value may not contain underscore or comma characters.
 * @param  {Bool}     [params.withException]        Only select jobs that failed due to an exception.
 * @param  {String}   [params.exceptionMessage]     Only select jobs that failed due to an exception with the given message.
 * @param  {Bool}     [params.noRetriesLeft]        Only select jobs which have no retries left.
 * @param  {Bool}     [params.active]               Only include active jobs.
 * @param  {Bool}     [params.suspended]            Only include suspended jobs.
 * @param  {Array}    [params.sorting]              A JSON array of criteria to sort the result by. Each element of the array is a JSON object that specifies one ordering. The position in the array identifies the rank of an ordering, i.e. whether it is primary, secondary, etc.
 * @param  {String}   params.sorting.sortBy         Sort the results lexicographically by a given criterion. Valid values are jobId, executionId, processInstanceId, jobRetries and jobDueDate.
 * @param  {String}   params.sorting.sortOrder      Sort the results in a given order. Values may be asc for ascending order or desc for descending order.
 * @param  {String}   [params.firstResult]          Pagination of results. Specifies the index of the first result to return.
 * @param  {String}   [params.maxResults]           Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 * @param  {Function} done
 */
Job.list = function(params, done) {
  var path = this.path;

  // those parameters have to be passed in the query and not body
  path += '?firstResult=' + (params.firstResult || 0);
  if (params.maxResults) {
    path += '&maxResults=' + params.maxResults;
  }

  return this.http.post(path, {
    data: params,
    done: done
  });
};

/**
 * Sets the retries of the job to the given number of retries.
 * @param  {Object}   params
 * @param  {String}   params.id      The id of the job.
 * @param  {String}   params.retries The number of retries to set that a job has left.
 * @param  {Function} done
 */
Job.setRetries = function(params, done) {
  return this.http.put(this.path + '/' + params.id + '/retries', {
    data: params,
    done: done
  });
};

Job.delete = function(id, done) {
  return this.http.del(this.path + '/' + id, {
    done: done
  });
};

Job.stacktrace = function(id, done) {
  var url = this.path + '/' + id + '/stacktrace';
  return this.http.get(url, {
    accept: 'text/plain',
    done: done
  });
};

/**
 * Recalculates the duedate for a given job.
 * @param {Object}    params
 * @param {String}    params.id                   The id of the job.Job
 * @param {Bool}      params.creationDateBased    Base recalculation on Job creation date. Default: true
 * @param {Function}  done
 */
Job.recalculateDuedate = function(params, done) {
  var url = this.path + '/' + params.id + '/duedate/recalculate';

  if (params.creationDateBased == false) {
    url += '?creationDateBased=' + params.creationDateBased;
  }
  return this.http.post(url, {
    done: done
  });
};

/**
 * Sets the duedate of the job to the given date.
 * @param  {Object}   params
 * @param  {String}   params.id      The id of the job.
 * @param  {String}   params.duedate The duedate of the job.
 * @param  {Function} done
 */

Job.setDuedate = function(params, done) {
  var url = this.path + '/' + params.id + '/duedate';
  return this.http.put(url, {
    data: params,
    done: done
  });
};

Job.suspended = function(params, done) {
  return this.http.put(this.path + '/' + params.id + '/suspended', {
    data: {
      suspended: !!params.suspended
    },
    done: done
  });
};

module.exports = Job;
