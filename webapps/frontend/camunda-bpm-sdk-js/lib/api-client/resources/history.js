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
var helpers = require('../helpers');

/**
 * History Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var History = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
History.path = 'history';

/**
 * Queries for the number of user operation log entries that fulfill the given parameters
 *
 * @param {Object}   [params]
 * @param {String}   [params.processDefinitionId]   Filter by process definition id.
 * @param {String}   [params.processDefinitionKey]  Filter by process definition key.
 * @param {String}   [params.processInstanceId]     Filter by process instance id.
 * @param {String}   [params.executionId]           Filter by execution id.
 * @param {String}   [params.caseDefinitionId]      Filter by case definition id.
 * @param {String}   [params.caseInstanceId]        Filter by case instance id.
 * @param {String}   [params.caseExecutionId]       Filter by case execution id.
 * @param {String}   [params.taskId]                Only include operations on this task.
 * @param {String}   [params.userId]                Only include operations of this user.
 * @param {String}   [params.operationId]           Filter by the id of the operation. This allows fetching of multiple entries which are part of a composite operation.
 * @param {String}   [params.operationType]         Filter by the type of the operation like Claim or Delegate.
 * @param {String}   [params.entityType]            Filter by the type of the entity that was affected by this operation, possible values are Task, Attachment or IdentityLink.
 * @param {String}   [params.property]              Only include operations that changed this property, e.g. owner or assignee
 * @param {String}   [params.afterTimestamp]        Restrict to entries that were created after the given timestamp. The timestamp must have the format yyyy-MM-dd'T'HH:mm:ss, e.g. 2014-02-25T14:58:37
 * @param {String}   [params.beforeTimestamp]       Restrict to entries that were created before the given timestamp. The timestamp must have the format yyyy-MM-dd'T'HH:mm:ss, e.g. 2014-02-25T14:58:37
 * @param {String}   [params.sortBy]                Sort the results by a given criterion. At the moment the query only supports sorting based on the timestamp.
 * @param {String}   [params.sortOrder]             Sort the results in a given order. Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param {Number}   [params.firstResult]           Pagination of results. Specifies the index of the first result to return.
 * @param {Number}   [params.maxResults]            Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 * @param {Function} done
 */
History.userOperationCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/user-operation/count', {
    data: params,
    done: done
  });
};

/**
 * Queries for user operation log entries that fulfill the given parameters
 * This method takes the same parameters as `History.userOperationCount`.
 */
History.userOperation = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/user-operation', {
    data: params,
    done: done
  });
};

/**
 *  Set an annotation for auditing reasons.
 *
 * @param {Object}   [params]
 * @param {String}   [params.id]            Operation ID to set the Annotation
 * @param {String}   [params.annotation]    An arbitrary text annotation set by a user for auditing reasons.
 * @param {Function} done
 */

History.setUserOperationAnnotation = function(params, done) {
  return this.http.put(
    this.path + '/user-operation/' + params.id + '/set-annotation',
    {
      data: params,
      done: done
    }
  );
};

/**
 *  Clear the annotation which was previously set for auditing reasons.
 *
 * @param {String}   [id]                   The operation id of the operation log to be updated.
 * @param {Function} done
 */

History.clearUserOperationAnnotation = function(id, done) {
  return this.http.put(
    this.path + '/user-operation/' + id + '/clear-annotation',
    {
      done: done
    }
  );
};

/**
 * Query for historic process instances that fulfill the given parameters.
 *
 * @param  {Object}   [params]
 * @param  {uuid}     [params.processInstanceId]                Filter by process instance id.
 * @param  {uuid[]}   [params.processInstanceIds]               Filter by process instance ids.
 *                                                              Must be a json array process instance ids.
 * @param  {String}   [params.processInstanceBusinessKey]       Filter by process instance business key.
 * @param  {String}   [params.processInstanceBusinessKeyLike]   Filter by process instance business key that the parameter is a substring of.
 * @param  {uuid}     [params.superProcessInstanceId]           Restrict query to all process instances that are sub process instances of the given process instance.
 *                                                              Takes a process instance id.
 * @param  {uuid}     [params.subProcessInstanceId]             Restrict query to one process instance that has a sub process instance with the given id.
 * @param  {uuid}     [params.superCaseInstanceId]              Restrict query to all process instances that are sub process instances of the given case instance.
 *                                                              Takes a case instance id.
 * @param  {uuid}     [params.subCaseInstanceId]                Restrict query to one process instance that has a sub case instance with the given id.
 * @param  {uuid}     [params.caseInstanceId]                   Restrict query to all process instances that are sub process instances of the given case instance.
 *                                                              Takes a case instance id.
 * @param  {uuid}     [params.processDefinitionId]              Filter by the process definition the instances run on.
 * @param  {String}   [params.processDefinitionKey]             Filter by the key of the process definition the instances run on.
 * @param  {String[]} [params.processDefinitionKeyNotIn]        Exclude instances that belong to a set of process definitions.
 *                                                              Must be a json array of process definition keys.
 * @param  {String}   [params.processDefinitionName]            Filter by the name of the process definition the instances run on.
 * @param  {String}   [params.processDefinitionNameLike]        Filter by process definition names that the parameter is a substring of.
 * @param  {Boolean}  [params.finished]                         Only include finished process instances.
 *                                                              Values may be `true` or `false`.
 * @param  {Boolean}  [params.unfinished]                       Only include unfinished process instances.
 *                                                              Values may be `true` or `false`.
 * @param  {String}   [params.startedBy]                        Only include process instances that were started by the given user.
 * @param  {String}   [params.startedBefore]                    Restrict to instances that were started before the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.startedAfter]                     Restrict to instances that were started after the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.finishedBefore]                   Restrict to instances that were finished before the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.finishedAfter]                    Restrict to instances that were finished after the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {Object[]} [params.variables]                        A JSON array to only include process instances that have/had variables with certain values. The array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.
 *                                                              `value` may be String, Number or Boolean.
 *                                                              Valid operator values are:
 *                                                              - `eq` - equal to
 *                                                              - `neq` - not equal to
 *                                                              - `gt` - greater than
 *                                                              - `gteq` - greater than or equal to
 *                                                              - `lt` - lower than
 *                                                              - `lteq` - lower than or equal to
 *                                                              - `like`
 * @param  {String}   [params.sortBy]                           Sort the results by a given criterion.
 *                                                              Valid values are instanceId, definitionId, businessKey, startTime, endTime, duration. Must be used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]                        Sort the results in a given order.
 *                                                              Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param  {Number}   [params.firstResult]                      Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}   [params.maxResults]                       Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.

 * @param  {Function} done
 */
History.processInstance = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  var body = {};
  var query = {};
  var queryParams = ['firstResult', 'maxResults'];

  for (var p in params) {
    if (queryParams.indexOf(p) > -1) {
      query[p] = params[p];
    } else {
      body[p] = params[p];
    }
  }

  return this.http.post(this.path + '/process-instance', {
    data: body,
    query: query,
    done: done
  });
};

/**
 * Query for the number of historic process instances that fulfill the given parameters.
 * This method takes the same message body as `History.processInstance`.
 */
History.processInstanceCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/process-instance/count', {
    data: params,
    done: done
  });
};

/**
 * Delete finished process instances asynchronously. With creation of a batch operation.
 *
 * @param params - either list of process instance ID's or an object corresponding to a processInstances
 *                  POST request based query
 * @param done - a callback function
 * @returns {*}
 */
History.deleteProcessInstancesAsync = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/process-instance/delete', {
    data: params,
    done: done
  });
};

/**
 * Set removal time to historic process instances asynchronously. With creation of a batch operation.
 *
 * @param params - either list of process instance ID's or an object corresponding to a processInstances
 *                  POST request based query
 * @param done - a callback function
 * @returns {*}
 */
History.setRemovalTimeToHistoricProcessInstancesAsync = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/process-instance/set-removal-time', {
    data: params,
    done: done
  });
};

/**
 * Set removal time to historic decision instances asynchronously. With creation of a batch operation.
 *
 * @param params - either list of decision instance ID's or an object corresponding to a decisionInstances
 *                  POST request based query
 * @param done - a callback function
 * @returns {*}
 */
History.setRemovalTimeToHistoricDecisionInstancesAsync = function(
  params,
  done
) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/decision-instance/set-removal-time', {
    data: params,
    done: done
  });
};

/**
 * Set removal time to historic batches asynchronously. With creation of a batch operation.
 *
 * @param params - either list of batch ID's or an object corresponding to a batches
 *                  POST request based query
 * @param done - a callback function
 * @returns {*}
 */
History.setRemovalTimeToHistoricBatchesAsync = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/batch/set-removal-time', {
    data: params,
    done: done
  });
};

/**
 * Query for historic decision instances that fulfill the given parameters.
 *
 * @param  {Object}   [params]
 * @param  {uuid}     [params.decisionInstanceId]                 Filter by decision instance id.
 * @param  {String}   [params.decisionInstanceIdIn]               Filter by decision instance ids. Must be a comma-separated list of decision instance ids.
 * @param  {uuid}     [params.decisionDefinitionId]               Filter by the decision definition the instances belongs to.
 * @param  {String}   [params.decisionDefinitionKey]              Filter by the key of the decision definition the instances belongs to.
 * @param  {String}   [params.decisionDefinitionName]             Filter by the name of the decision definition the instances belongs to.
 * @param  {uuid}     [params.processDefinitionId]                Filter by the process definition the instances belongs to.
 * @param  {String}   [params.processDefinitionKey]               Filter by the key of the process definition the instances belongs to.
 * @param  {uuid}     [params.processInstanceId]                  Filter by the process instance the instances belongs to.
 * @param  {uuid}     [params.activityIdIn]                       Filter by the activity ids the instances belongs to. Must be a comma-separated list of acitvity ids.
 * @param  {String}   [params.activityInstanceIdIn]               Filter by the activity instance ids the instances belongs to. Must be a comma-separated list of acitvity instance ids.
 * @param  {String}   [params.evaluatedBefore]                    Restrict to instances that were evaluated before the given date. The date must have the format yyyy-MM-dd'T'HH:mm:ss, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.evaluatedAfter]                     Restrict to instances that were evaluated after the given date. The date must have the format yyyy-MM-dd'T'HH:mm:ss, e.g., 2013-01-23T14:42:45.
 * @param  {Boolean}  [params.includeInputs]                      Include input values in the result. Value may only be true, as false is the default behavior.
 * @param  {Boolean}  [params.includeOutputs]                     Include output values in the result. Value may only be true, as false is the default behavior.
 * @param  {Boolean}  [params.disableBinaryFetching]              Disables fetching of byte array input and output values. Value may only be true, as false is the default behavior.
 * @param  {Boolean}  [params.disableCustomObjectDeserialization] Disables deserialization of input and output values that are custom objects. Value may only be true, as false is the default behavior.
 * @param  {String}   [params.sortBy]                             Sort the results by a given criterion.
 *                                                                Valid values are evaluationTime. Must be used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]                          Sort the results in a given order.
 *                                                                Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param  {Number}   [params.firstResult]                        Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}   [params.maxResults]                         Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 * @param  {Function} done
 */
History.decisionInstance = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/decision-instance', {
    data: params,
    done: done
  });
};

/**
 * Query for the number of historic decision instances that fulfill the given parameters.
 * This method takes the same parameters as `History.decisionInstance`.
 */
History.decisionInstanceCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/decision-instance/count', {
    data: params,
    done: done
  });
};

/**
 * Delete historic decision instances asynchronously. With creation of a batch operation.
 *
 * @param params - either list of decision instance ID's or an object corresponding to a decisionInstances
 *                  POST request based query
 * @param done - a callback function
 * @returns {*}
 */
History.deleteDecisionInstancesAsync = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/decision-instance/delete', {
    data: params,
    done: done
  });
};

/**
 * Query for historic batches that fulfill given parameters. Parameters may be the properties of batches, such as the id or type.
 * The size of the result set can be retrieved by using the GET query count.
 */
History.batch = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/batch', {
    data: params,
    done: done
  });
};

/**
 * Retrieves a single historic batch according to the HistoricBatch interface in the engine.
 */
History.singleBatch = function(id, done) {
  return this.http.get(this.path + '/batch/' + id, {
    done: done
  });
};

/**
 * Request the number of historic batches that fulfill the query criteria.
 * Takes the same filtering parameters as the GET query.
 */
History.batchCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/batch/count', {
    data: params,
    done: done
  });
};

History.batchDelete = function(id, done) {
  var path = this.path + '/batch/' + id;

  return this.http.del(path, {
    done: done
  });
};

/**
 * Query for process instance durations report.
 * @param  {Object}   [params]
 * @param  {Object}   [params.reportType]           Must be 'duration'.
 * @param  {Object}   [params.periodUnit]           Can be one of `month` or `quarter`, defaults to `month`
 * @param  {Object}   [params.processDefinitionIn]  Comma separated list of process definition IDs
 * @param  {Object}   [params.startedAfter]         Date after which the process instance were started
 * @param  {Object}   [params.startedBefore]        Date before which the process instance were started
 * @param  {Function} done
 */
History.report = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  params.reportType = params.reportType || 'duration';
  params.periodUnit = params.periodUnit || 'month';

  return this.http.get(this.path + '/process-instance/report', {
    data: params,
    done: done
  });
};

/**
 * Query for process instance durations report.
 * @param  {Object}   [params]
 * @param  {Object}   [params.reportType]           Must be 'duration'.
 * @param  {Object}   [params.periodUnit]           Can be one of `month` or `quarter`, defaults to `month`
 * @param  {Object}   [params.processDefinitionIn]  Comma separated list of process definition IDs
 * @param  {Object}   [params.startedAfter]         Date after which the process instance were started
 * @param  {Object}   [params.startedBefore]        Date before which the process instance were started
 * @param  {Function} done
 */
History.reportAsCsv = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  params.reportType = params.reportType || 'duration';
  params.periodUnit = params.periodUnit || 'month';

  return this.http.get(this.path + '/process-instance/report', {
    data: params,
    accept: 'text/csv',
    done: done
  });
};

/**
 * Query for historic task instances that fulfill the given parameters.
 *
 * @param  {Object}   [params]
 * @param  {uuid}     [params.taskId]                           Filter by taskId.
 * @param  {uuid}     [params.taskParentTaskId]                 Filter by parent task id.
 * @param  {uuid}     [params.processInstanceId]                Filter by process instance id.
 * @param  {uuid}     [params.executionId]                      Filter by the id of the execution that executed the task.
 * @param  {uuid}     [params.processDefinitionId]              Filter by process definition id.
 * @param  {String}   [params.processDefinitionKey]             Restrict to tasks that belong to a process definition with the given key.
 * @param  {String}   [params.processDefinitionName]            Restrict to tasks that belong to a process definition with the given name.
 * @param  {uuid}     [params.caseInstanceId]                   Filter by case instance id.

 * @param  {uuid}     [params.caseExecutionId]                  Filter by the id of the case execution that executed the task.
 * @param  {uuid}     [params.caseDefinitionId]                 Filter by case definition id.
 * @param  {String}   [params.caseDefinitionKey]                Restrict to tasks that belong to a case definition with the given key.
 * @param  {String}   [params.caseDefinitionName]               Restrict to tasks that belong to a case definition with the given name.
 * @param  {uuid[]}   [params.activityInstanceIdIn]             Only include tasks which belong to one of the passed activity instance ids.
 *                                                              Must be a json array of activity instance ids.
 * @param  {String}   [params.taskName]                         Restrict to tasks that have the given name.
 * @param  {String}   [params.taskNameLike]                     Restrict to tasks that have a name with the given parameter value as substring.
 * @param  {String}   [params.taskDescription]                  Restrict to tasks that have the given description.
 * @param  {String}   [params.taskDescriptionLike]              Restrict to tasks that have a description that has the parameter value as a substring.
 * @param  {String}   [params.taskDefinitionKey]                Restrict to tasks that have the given key.
 * @param  {String}   [params.taskDeleteReason]                 Restrict to tasks that have the given delete reason.
 * @param  {String}   [params.taskDeleteReasonLike]             Restrict to tasks that have a delete reason that has the parameter value as a substring.
 * @param  {String}   [params.taskAssignee]                     Restrict to tasks that the given user is assigned to.
 * @param  {String}   [params.taskAssigneeLike]                 Restrict to tasks that are assigned to users with the parameter value as a substring.
 * @param  {String}   [params.taskOwner]                        Restrict to tasks that the given user owns.
 * @param  {String}   [params.taskOwnerLike]                    Restrict to tasks that are owned by users with the parameter value as a substring.
 * @param  {String}   [params.taskPriority]                     Restrict to tasks that have the given priority.
 * @param  {String}   [params.assigned]                         If set to true, restricts the query to all tasks that are assigned.
 *                                                              Values may be `true` or `false`.
 * @param  {String}   [params.unassigned]                       If set to true, restricts the query to all tasks that are unassigned.
 *                                                              Values may be `true` or `false`.
 * @param  {String}   [params.finished]                         Only include finished tasks. Value may only be true, as false is the default behavior.
 * @param  {String}   [params.unfinished]                       Only include unfinished tasks. Value may only be true, as false is the default behavior.
 * @param  {String}   [params.processFinished]                  Only include tasks of finished processes. Value may only be true, as false is the default behavior.
 * @param  {String}   [params.processUnfinished]                Only include tasks of unfinished processes. Value may only be true, as false is the default behavior.
 * @param  {Date}     [params.taskDueDate]                      Restrict to tasks that are due on the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.taskDueDateBefore]                RestRestrict to tasks that are due before the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.taskDueDateAfter]                 Restrict to tasks that are due after the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.taskFollowUpDate]                 ReRestrict to tasks that have a followUp date on the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.taskFollowUpDateBefore]           Restrict to tasks that have a followUp date before the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.taskFollowUpDateAfter]            Restrict to tasks that have a followUp date after the given date.
 *                                                              The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {uuid[]}   [params.tenantIdIn]                       Filter by a comma-separated list of tenant ids. A task instance must have one of the given tenant ids.
 *                                                              Must be a json array of tenant ids.
 * @param  {Object[]} [params.taskVariables]                    A JSON array to only include process instances that have/had variables with certain values. The array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.
 *                                                              `value` may be String, Number or Boolean.
 *                                                              Valid operator values are:
 *                                                              - `eq` - equal to
 *                                                              - `neq` - not equal to
 *                                                              - `gt` - greater than
 *                                                              - `gteq` - greater than or equal to
 *                                                              - `lt` - lower than
 *                                                              - `lteq` - lower than or equal to
 *                                                              - `like`
 * @param  {Object[]} [params.processVariables]                 A JSON array to only include process instances that have/had variables with certain values. The array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.
 *                                                              `value` may be String, Number or Boolean.
 *                                                              Valid operator values are:
 *                                                              - `eq` - equal to
 *                                                              - `neq` - not equal to
 *                                                              - `gt` - greater than
 *                                                              - `gteq` - greater than or equal to
 *                                                              - `lt` - lower than
 *                                                              - `lteq` - lower than or equal to
 *                                                              - `like`
 * @param  {String}   [params.taskInvolvedUser]                 Restrict on the historic identity links of any type of user.
 * @param  {String}   [params.taskInvolvedGroup]                Restrict on the historic identity links of any type of group.
 * @param  {String}   [params.taskHadCandidateUser]             Restrict on the historic identity links of type candidate user.
 * @param  {String}   [params.taskHadCandidateGroup]            Restrict on the historic identity links of type candidate group.
 * @param  {String}   [params.withCandidateGroups]              Only include tasks which have a candidate group. Value may only be true, as false is the default behavior.
 * @param  {String}   [params.withoutCandidateGroups]           Only include tasks which have no candidate group. Value may only be true, as false is the default behavior.
 * @param  {String}   [params.sortBy]                           Sort the results by a given criterion.
 *                                                              Valid values are taskId, activityInstanceID, processDefinitionId, processInstanceId, executionId,
 *                                                              duration, endTime, startTime, taskName, taskDescription, assignee, owner, dueDate, followUpDate,
 *                                                              deleteReason, taskDefinitionKey, priority, caseDefinitionId, caseInstanceId, caseExecutionId and
 *                                                              tenantId. Must be used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]                        Sort the results in a given order.
 *                                                              Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param  {Number}   [params.firstResult]                      Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}   [params.maxResults]                       Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.

 * @param  {Function} done
 */
History.task = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  var body = {};
  var query = {};
  var queryParams = ['firstResult', 'maxResults'];

  for (var p in params) {
    if (queryParams.indexOf(p) > -1) {
      query[p] = params[p];
    } else {
      body[p] = params[p];
    }
  }

  return this.http.post(this.path + '/task', {
    data: body,
    query: query,
    done: done
  });
};

/**
 * Query for the number of historic task instances that fulfill the given parameters.
 * This method takes the same parameters as `History.task`.
 */
History.taskCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/task/count', {
    data: params,
    done: done
  });
};

/**
 * Query for a historic task instance duration report.
 *
 * @param  {Object}   [params]
 * @param  {Date}     [params.completedBefore]    Restrict to tasks which are completed before a given date.
 *                                                The date must have the format `yyyy-MM-dd'T'HH:mm:ss`,
 *                                                e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.completedAfter]     Restrict to tasks which are completed after a given date.
 *                                                The date must have the format `yyyy-MM-dd'T'HH:mm:ss`,
 *                                                e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.periodUnit]         Can be one of `month` or `quarter`, defaults to `month`
 * @param  {Function}  done
 */
History.taskDurationReport = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  params.reportType = params.reportType || 'duration';
  params.periodUnit = params.periodUnit || 'month';

  return this.http.get(this.path + '/task/report', {
    data: params,
    done: done
  });
};

/**
 * Query for a completed task instance report
 *
 * @param  {Object}   [params]
 * @param  {Date}     [params.completedBefore]    Restrict to tasks which are completed before a given date.
 *                                                The date must have the format `yyyy-MM-dd'T'HH:mm:ss`,
 *                                                e.g., 2013-01-23T14:42:45.
 * @param  {Date}     [params.completedAfter]     Restrict to tasks which are completed after a given date.
 *                                                The date must have the format `yyyy-MM-dd'T'HH:mm:ss`,
 *                                                e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.groupBy]            Groups the task report by `taskDefinitionKey` (Default) or
 *                                                `processDefinitionKey`. Valid values are `taskDefinition` or
 *                                                `processDefinition`.
 * @param done
 * @returns {*}
 */
History.taskReport = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  params.reportType = params.reportType || 'count';

  return this.http.get(this.path + '/task/report', {
    data: params,
    done: done
  });
};

/**
 * Query for historic case instances that fulfill the given parameters.
 *
 * @param  {Object}   [params]
 * @param  {uuid}     [params.caseInstanceId]                Filter by case instance id.
 * @param  {uuid[]}   [params.caseInstanceIds]               Filter by case instance ids.
 *                                                           Must be a json array case instance ids.
 *
 * @param  {uuid}     [params.caseDefinitionId]              Filter by the case definition the instances run on.
 * @param  {String}   [params.caseDefinitionKey]             Filter by the key of the case definition the instances run on.
 * @param  {String[]} [params.caseDefinitionKeyNotIn]        Exclude instances that belong to a set of case definitions.
 *
 * @param  {String}   [params.caseDefinitionName]            Filter by the name of the case definition the instances run on.
 * @param  {String}   [params.caseDefinitionNameLike]        Filter by case definition names that the parameter is a substring of.
 *
 * @param  {String}   [params.caseInstanceBusinessKey]       Filter by case instance business key.
 * @param  {String}   [params.caseInstanceBusinessKeyLike]   Filter by case instance business key that the parameter is a substring of.
 *
 *
 * @param  {String}   [params.createdBefore]                 Restrict to instances that were created before the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.createdAfter]                  Restrict to instances that were created after the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 *
 * @param  {String}   [params.closedBefore]                  Restrict to instances that were closed before the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.closedAfter]                   Restrict to instances that were closed after the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 *
 * @param  {String}   [params.createdBy]                     Only include case instances that were created by the given user.
 *
 *
 * @param  {uuid}     [params.superCaseInstanceId]           Restrict query to all case instances that are sub case instances of the given case instance.
 *                                                           Takes a case instance id.
 * @param  {uuid}     [params.subCaseInstanceId]             Restrict query to one case instance that has a sub case instance with the given id.
 *
 * @param  {uuid}     [params.superProcessInstanceId]        Restrict query to all process instances that are sub case instances of the given process instance.
 *                                                           Takes a process instance id.
 * @param  {uuid}     [params.subProcessInstanceId]          Restrict query to one case instance that has a sub process instance with the given id.
 *
 * @param  {uuid}     [params.tenantIdIn]                    Filter by a comma-separated list of tenant ids. A case instance must have one of the given tenant ids.
 *
 * @param  {Boolean}  [params.active]                        Only include active case instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.completed]                     Only include completed case instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.terminated]                    Only include terminated case instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.closed]                        Only include closed case instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.notClosed]                     Only include not closed case instances.
 *                                                           Values may be `true` or `false`.
 *
 * @param  {Object[]} [params.variables]                     A JSON array to only include case instances that have/had variables with certain values. The array consists of objects with the three properties name, operator and value. name (String) is the variable name, operator (String) is the comparison operator to be used and value the variable value.
 *                                                           `value` may be String, Number or Boolean.
 *                                                           Valid operator values are:
 *                                                           - `eq` - equal to
 *                                                           - `neq` - not equal to
 *                                                           - `gt` - greater than
 *                                                           - `gteq` - greater than or equal to
 *                                                           - `lt` - lower than
 *                                                           - `lteq` - lower than or equal to
 *                                                           - `like`
 *
 * @param  {String}   [params.sortBy]                        Sort the results by a given criterion.
 *                                                           Valid values are instanceId, definitionId, businessKey, startTime, endTime, duration. Must be used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]                     Sort the results in a given order.
 *                                                           Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param  {Number}   [params.firstResult]                   Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}   [params.maxResults]                    Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.

 * @param  {Function} done
 */
History.caseInstance = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  var body = {};
  var query = {};
  var queryParams = ['firstResult', 'maxResults'];

  for (var p in params) {
    if (queryParams.indexOf(p) > -1) {
      query[p] = params[p];
    } else {
      body[p] = params[p];
    }
  }

  return this.http.post(this.path + '/case-instance', {
    data: body,
    query: query,
    done: done
  });
};

/**
 * Query for the number of historic case instances that fulfill the given parameters.
 * This method takes the same parameters as `History.caseInstance`.
 */
History.caseInstanceCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/case-instance/count', {
    data: params,
    done: done
  });
};

/**
 * Query for historic case activty instances that fulfill the given parameters.
 *
 * @param  {Object}   [params]
 * @param  {uuid}     [params.caseActivityInstanceId]        Filter by case activity instance id.
 * @param  {String}   [params.caseExecutionId]               Filter by the id of the case execution that executed the case activity instance.
 * @param  {uuid}     [params.caseInstanceId]                Filter by case instance id.
 *
 * @param  {uuid}     [params.caseDefinitionId]              Filter by the case definition the instances run on.
 *
 * @param  {String}   [params.caseActivityId]                Filter by the case activity id.
 * @param  {String}   [params.caseActivityName]              Filter by the case activity name.
 * @param  {String}   [params.caseActivityType]              Filter by the case activity type.
 *
 * @param  {String}   [params.createdBefore]                 Restrict to instances that were created before the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.createdAfter]                  Restrict to instances that were created after the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 *
 * @param  {String}   [params.endedBefore]                   Restrict to instances that were ended before the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 * @param  {String}   [params.endedAfter]                    Restrict to instances that were ended after the given date.
 *                                                           The date must have the format `yyyy-MM-dd'T'HH:mm:ss`, e.g., 2013-01-23T14:42:45.
 *
 * @param  {Boolean}  [params.required]                      Only include required case activity instances.
 *                                                           Values may be `true` or `false`.
 *
 * @param  {Boolean}  [params.finished]                      Only include finished case activity instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.unfinished]                    Only include unfinished case activity instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.available]                     Only include available case activity instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.enabled]                       Only include enabled case activity instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.disabled]                      Only include disabled case activity instances.
 *                                                           Values may be `true` or `false`.
 * @param  {Boolean}  [params.active]                        Only include active case activity instances.
 *                                                           Values may be `true` or `false`.
 *
 * @param  {Boolean}  [params.completed]                     Only include completed case activity instances.
 *                                                           Values may be `true` or `false`.
 *
 * @param  {Boolean}  [params.terminated]                    Only include terminated case activity instances.
 *                                                           Values may be `true` or `false`.
 *
 * @param  {uuid[]}   [params.tenantIdIn]                    Filter by a comma-separated list of tenant ids. A case activity instance must have one of the given tenant ids.
 *
 * @param  {String}   [params.sortBy]                        Sort the results by a given criterion.
 *                                                           Valid values are caseActivityInstanceId, caseInstanceId, caseExecutionId, caseActivityId, caseActivityName, createTime, endTime, duration,
 *                                                           caseDefinitionId and tenantId. Must be used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]                     Sort the results in a given order.
 *                                                           Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param  {Number}   [params.firstResult]                   Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}   [params.maxResults]                    Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.

 * @param  {Function} done
 */
History.caseActivityInstance = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/case-activity-instance', {
    data: params,
    done: done
  });
};

/**
 * Query for the number of historic case activity instances that fulfill the given parameters.
 * This method takes the same parameters as `History.caseActivityInstance`.
 */
History.caseActivityInstanceCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/case-activity-instance/count', {
    data: params,
    done: done
  });
};

/**
 * Queries for historic activity instances that fulfill the given parameters.
 * @param {Object}  [params]
 * @param {String}  params.activityInstanceId	    Filter by activity instance id.
 * @param {String}  params.processInstanceId      Filter by process instance id.
 * @param {String}  params.processDefinitionId    Filter by process definition id.
 * @param {String}  params.executionId            Filter by the id of the execution that executed the activity instance.
 * @param {String}  params.activityId             Filter by the activity id (according to BPMN 2.0 XML).
 * @param {String}  params.activityName           Filter by the activity name (according to BPMN 2.0 XML).
 * @param {String}  params.activityType           Filter by activity type.
 * @param {String}  params.taskAssignee           Only include activity instances that are user tasks and assigned to a given user.
 * @param {Boolean} params.finished               Only include finished activity instances. Value may only be true, as false behaves the same as when the property is not set.
 * @param {Boolean} params.unfinished             Only include unfinished activity instances. Value may only be true, as false behaves the same as when the property is not set.
 * @param {Boolean} params.canceled               Only include canceled activity instances. Value may only be true, as false behaves the same as when the property is not set.
 * @param {Boolean} params.completeScope          Only include activity instances which completed a scope. Value may only be true, as false behaves the same as when the property is not set.
 * @param {String}  params.startedBefore          Restrict to instances that were started before the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200.
 * @param {String}  params.startedAfter           Restrict to instances that were started after the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200.
 * @param {String}  params.finishedBefore         Restrict to instances that were finished before the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200.
 * @param {String}  params.finishedAfter          Restrict to instances that were finished after the given date. By default*, the date must have the format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g., 2013-01-23T14:42:45.000+0200.
 * @param {String}  params.tenantIdIn             Filter by a comma-separated list of tenant ids. An activity instance must have one of the given tenant ids.
 * @param {String}  params.sortBy                 Sort the results by a given criterion. Valid values are activityInstanceId, instanceId, executionId, activityId, activityName, activityType, startTime, endTime, duration, definitionId, occurrence and tenantId. Must be used in conjunction with the sortOrder parameter.
 * @param {String}  params.sortOrder              Sort the results in a given order. Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param {Number}  params.firstResult            Pagination of results. Specifies the index of the first result to return.
 * @param {Number}  params.maxResults             Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 */
History.activityInstance = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/activity-instance', {
    data: params,
    done: done
  });
};

/**
 * Queries for historic activity instances that fulfill the given parameters.
 * @param {Object}  [params]
 * @param {String}  params.incidentId           Restricts to incidents that have the given id.
 * @param {String}  params.incidentType         Restricts to incidents that belong to the given incident type. See the User Guide for a list of incident types.
 * @param {String}  params.incidentMessage      Restricts to incidents that have the given incident message.
 * @param {String}  params.processDefinitionId  Restricts to incidents that belong to a process definition with the given id.
 * @param {String}  params.processInstanceId    Restricts to incidents that belong to a process instance with the given id.
 * @param {String}  params.executionId          Restricts to incidents that belong to an execution with the given id.
 * @param {String}  params.activityId           Restricts to incidents that belong to an activity with the given id.
 * @param {String}  params.causeIncidentId      Restricts to incidents that have the given incident id as cause incident.
 * @param {String}  params.rootCauseIncidentId  Restricts to incidents that have the given incident id as root cause incident.
 * @param {String}  params.configuration        Restricts to incidents that have the given parameter set as configuration.
 * @param {String}  params.tenantIdIn           Restricts to incidents that have one of the given comma-separated tenant ids.
 * @param {String}  params.jobDefinitionIdIn    Restricts to incidents that have one of the given comma-separated job definition ids.
 * @param {String}  params.open             	  Restricts to incidents that are open.
 * @param {String}  params.deleted              Restricts to incidents that are deleted.
 * @param {String}  params.resolved             Restricts to incidents that are resolved.
 * @param {String}  params.sortBy               Sort the results lexicographically by a given criterion. Valid values are incidentId, incidentMessage, createTime, endTime, incidentType, executionId, activityId, processInstanceId, processDefinitionId, causeIncidentId, rootCauseIncidentId, configuration, tenantId and incidentState. Must be used in conjunction with the sortOrder parameter.
 * @param {String}  params.sortOrder            Sort the results in a given order. Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 */
History.incident = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.get(this.path + '/incident', {
    data: params,
    done: done
  });
};

/**
 * Query for historic variable instances that fulfill the given parameters.
 *
 * @param  {Object}   [params]
 * @param  {uuid}     [params.variableName]                 Filter by variable name.
 * @param  {String}   [params.variableNameLike]             Restrict to variables with a name like the parameter.
 * @param  {uuid[]}   [params.variableValue]                Filter by variable value.
 *
 * @param  {uuid}     [params.processInstanceId]            Filter by the process instance the variable belongs to.
 * @param  {String[]} [params.executionIdIn]                Filter by the execution ids.
 * @param  {String}   [params.caseInstanceId]               Filter by the case instance id.
 * @param  {String[]} [params.caseExecutionIdIn]            Filter by the case execution ids.
 * @param  {String[]} [params.taskIdIn]                     Filter by the task ids.
 * @param  {String[]} [params.activityInstanceIdIn]         Filter by the activity instance ids.
 *
 * @param  {uuid[]}   [params.tenantIdIn]                    Filter by a comma-separated list of tenant ids. A case activity instance must have one of the given tenant ids.
 *
 * @param  {String}   [params.sortBy]                        Sort the results by a given criterion.
 *                                                           Valid values are instanceId, variableName and tenantId. Must be used in conjunction with the sortOrder parameter.
 * @param  {String}   [params.sortOrder]                     Sort the results in a given order.
 *                                                           Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter.
 * @param  {Number}   [params.firstResult]                   Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}   [params.maxResults]                    Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.

 * @param  {Function} done
 */
History.variableInstance = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  var body = {};
  var query = {};
  var queryParams = ['firstResult', 'maxResults', 'deserializeValues'];

  for (var p in params) {
    if (queryParams.indexOf(p) > -1) {
      query[p] = params[p];
    } else {
      body[p] = params[p];
    }
  }

  return this.http.post(this.path + '/variable-instance', {
    data: body,
    query: query,
    done: done
  });
};

/**
 * Query for the number of historic variable instances that fulfill the given parameters.
 * This method takes the same parameters as `History.variableInstance`.
 */
History.variableInstanceCount = function(params, done) {
  if (typeof params === 'function') {
    done = arguments[0];
    params = {};
  }

  return this.http.post(this.path + '/variable-instance/count', {
    data: params,
    done: done
  });
};

History.caseActivityStatistics = function(params, done) {
  var id = params.id || params;

  return this.http.get(this.path + '/case-definition/' + id + '/statistics', {
    done: done
  });
};

History.drdStatistics = function(id, params, done) {
  var url =
    this.path + '/decision-requirements-definition/' + id + '/statistics';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the history cleanup configuration
 */
History.cleanupConfiguration = function(params, done) {
  var url = this.path + '/cleanup/configuration';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Delete the history of a single variable
 */

History.deleteVariable = function(id, done) {
  var url = this.path + '/variable-instance/' + id;

  return this.http.del(url, {
    done: done
  });
};

/**
 * Delete the history of a single variable
 */

History.deleteAllVariables = function(id, done) {
  var url = this.path + '/process-instance/' + id + '/variable-instances';

  return this.http.del(url, {
    done: done
  });
};

/**
 * Query for the history cleanup job
 */
History.cleanupJobs = function(params, done) {
  var url = this.path + '/cleanup/jobs';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query to start a history cleanup job
 * @param  {Object}      [params]
 * @param  {Boolean}     [params.executeAtOnce]        Execute job in nearest future
 */
History.cleanup = function(params, done) {
  var url = this.path + '/cleanup';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.post(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the count of the finished historic process instances, cleanable process instances and basic process definition data - id, key, name and version
 * @param  {Object}      [params]
 * @param  {uuid[]}      [params.processDefinitionIdIn]        Array of processDefinition ids
 * @param  {uuid[]}      [params.processDefinitionKeyIn]       Array of processDefinition keys
 * @param  {Number}      [params.firstResult]                  Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}      [params.maxResults]                   Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 */
History.cleanableProcessCount = function(params, done) {
  var url =
    this.path + '/process-definition/cleanable-process-instance-report/count';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the report results about a process definition and finished process instances relevant to history cleanup
 * This method takes the same parameterers as 'History.cleanableProcessInstanceCount'
 */
History.cleanableProcess = function(params, done) {
  var url = this.path + '/process-definition/cleanable-process-instance-report';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the count of the finished historic case instances, cleanable case instances and basic case definition data - id, key, name and version.
 * @param  {uuid[]}      [params.caseDefinitionIdIn]           Array of caseDefinition ids
 * @param  {uuid[]}      [params.caseDefinitionKeyIn]          Array of caseDefinition keys
 * @param  {Number}      [params.firstResult]                  Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}      [params.maxResults]                   Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 */
History.cleanableCaseCount = function(params, done) {
  var url = this.path + '/case-definition/cleanable-case-instance-report/count';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the report results about a case definition and finished case instances relevant to history cleanup
 * This method takes the same parameterers as 'History.cleanableCaseInstanceCount '
 */
History.cleanableCase = function(params, done) {
  var url = this.path + '/case-definition/cleanable-case-instance-report';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the count of the finished historic decision instances, cleanable decision instances and basic decision definition data - id, key, name and version
 * @param  {Object}      [params]
 * @param  {uuid[]}      [params.decisionDefinitionIdIn]           Array of decisionDefinition ids
 * @param  {uuid[]}      [params.decisionDefinitionKeyIn]          Array of decisionDefinition keys
 * @param  {Number}      [params.firstResult]                      Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}      [params.maxResults]                       Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 */
History.cleanableDecisionCount = function(params, done) {
  var url =
    this.path + '/decision-definition/cleanable-decision-instance-report/count';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the report results about a decision definition and finished decision instances relevant to history cleanup
 * This method takes the same parameterers as 'History.cleanableDecisionInstanceCount '
 */
History.cleanableDecision = function(params, done) {
  var url =
    this.path + '/decision-definition/cleanable-decision-instance-report';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the count of report results about historic batch operations relevant to history cleanup
 * @param  {Object}      [params]
 * @param  {Number}      [params.firstResult]                      Pagination of results. Specifies the index of the first result to return.
 * @param  {Number}      [params.maxResults]                       Pagination of results. Specifies the maximum number of results to return. Will return less results if there are no more results left.
 */
History.cleanableBatchCount = function(params, done) {
  var url = this.path + '/batch/cleanable-batch-report/count';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

/**
 * Query for the report about historic batch operations relevant to history cleanup
 * This method takes the same parameterers as 'History.cleanableBatchCount'
 */
History.cleanableBatch = function(params, done) {
  var url = this.path + '/batch/cleanable-batch-report';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
};

History.jobLogList = function(params, done) {
  var url = this.path + '/job-log';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });


};

History.jobLogCount = function(params, done) {
  var url = this.path + '/job-log/count';

  if (typeof params === 'function') {
    done = params;
    params = {};
  }

  return this.http.get(url, {
    data: params,
    done: done
  });
}

History.externalTaskLogList = helpers.createSimpleGetQueryFunction(
  '/external-task-log'
);
History.externalTaskLogCount = helpers.createSimpleGetQueryFunction(
  '/external-task-log/count'
);

module.exports = History;
