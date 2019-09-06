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
var utils = require('../../utils');

/**
 * No-Op callback
 */
function noop() {}

/**
 * Task Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Task = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Task.path = 'task';

/**
 * Fetch a list of tasks
 * @param {Object} [params]
 * @param {String} [params.processInstanceId]               Restrict to tasks that belong to process instances with the given id.
 * @param {String} [params.processInstanceBusinessKey]      Restrict to tasks that belong to process instances with the given business key.
 * @param {String} [params.processInstanceBusinessKeyLike]  Restrict to tasks that have a process instance business key that has the parameter value as a substring.
 * @param {String} [params.processDefinitionId]             Restrict to tasks that belong to a process definition with the given id.
 * @param {String} [params.processDefinitionKey]            Restrict to tasks that belong to a process definition with the given key.
 * @param {String} [params.processDefinitionName]           Restrict to tasks that belong to a process definition with the given name.
 * @param {String} [params.processDefinitionNameLike]       Restrict to tasks that have a process definition name that has the parameter value as a substring.
 * @param {String} [params.executionId]                     Restrict to tasks that belong to an execution with the given id.
 * @param {String} [params.activityInstanceIdIn]            Only include tasks which belongs to one of the passed and comma-separated activity instance ids.
 * @param {String} [params.assignee]                        Restrict to tasks that the given user is assigned to.
 * @param {String} [params.assigneeLike]                    Restrict to tasks that have an assignee that has the parameter value as a substring.
 * @param {String} [params.owner]                           Restrict to tasks that the given user owns.
 * @param {String} [params.candidateGroup]                  Only include tasks that are offered to the given group.
 * @param {String} [params.candidateUser]                   Only include tasks that are offered to the given user.
 * @param {String} [params.involvedUser]                    Only include tasks that the given user is involved in.
 *                                                          A user is involved in a task if there exists an identity link between task and user (e.g. the user is the assignee).
 * @param {String} [params.unassigned]                      If set to true, restricts the query to all tasks that are unassigned.
 * @param {String} [params.taskDefinitionKey]               Restrict to tasks that have the given key.
 * @param {String} [params.taskDefinitionKeyLike]           Restrict to tasks that have a key that has the parameter value as a substring.
 * @param {String} [params.name]                            Restrict to tasks that have the given name.
 * @param {String} [params.nameLike]                        Restrict to tasks that have a name with the given parameter value as substring.
 * @param {String} [params.description]                     Restrict to tasks that have the given description.
 * @param {String} [params.descriptionLike]                 Restrict to tasks that have a description that has the parameter value as a substring.
 * @param {String} [params.priority]                        Restrict to tasks that have the given priority.
 * @param {String} [params.maxPriority]                     Restrict to tasks that have a lower or equal priority.
 * @param {String} [params.minPriority]                     Restrict to tasks that have a higher or equal priority.
 * @param {String} [params.due]                             Restrict to tasks that are due on the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.dueAfter]                        Restrict to tasks that are due after the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.dueBefore]                       Restrict to tasks that are due before the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.followUp]                        Restrict to tasks that have a followUp date on the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.followUpAfter]                   Restrict to tasks that have a followUp date after the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.followUpBefore]                  Restrict to tasks that have a followUp date before the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.created]                         Restrict to tasks that were created on the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.createdAfter]                    Restrict to tasks that were created after the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.createdBefore]                   Restrict to tasks that were created before the given date.
 *                                                          The date must have the format yyyy-MM-dd'T'HH:mm:ss, so for example 2013-01-23T14:42:45 is valid.
 * @param {String} [params.delegationState]                 Restrict to tasks that are in the given delegation state.
 *                                                          Valid values are "PENDING" and "RESOLVED".
 * @param {String} [params.candidateGroups]                 Restrict to tasks that are offered to any of the given candidate groups. Takes a comma-separated list of group names, so for example developers,support,sales.
 * @param {String} [params.active]                          Only include active tasks. Values may be true or false. suspended Only include suspended tasks.
 *                                                          Values may be "true" or "false".
 * @param {String} [params.taskVariables]                   Only include tasks that have variables with certain values. Variable tasking expressions are comma-separated and are structured as follows:
 *                                                          A valid parameter value has the form key_operator_value. key is the variable name, op is the comparison operator to be used and value the variable value. Note: Values are always treated as String objects on server side. Valid operator values are: eq - equals; neq - not equals; gt - greater than; gteq - greater than or equals; lt - lower than; lteq - lower than or equals; like. key and value may not contain underscore or comma characters.
 * @param {String} [params.processVariables]                Only include tasks that belong to process instances that have variables with certain values.
 *                                                          Variable tasking expressions are comma-separated and are structured as follows:
 *                                                          A valid parameter value has the form key_operator_value. "key" is the variable name, "op" is the comparison operator to be used and value the variable value.
 *                                                          Note: Values are always treated as String objects on server side.
 *                                                          Valid operator values are: "eq" - equals; "neq" - not equals; "gt" - greater than; "gteq" - greater than or equals; "lt" - lower than; "lteq" - lower than or equals; like.
 *                                                          "key" and "value" may not contain underscore or comma characters.
 *
 * @param {String} [params.sortBy]                          Sort the results lexicographically by a given criterion.
 *                                                          Valid values are "instanceId", "dueDate", "executionId", "assignee", "created", "description", "id", "name" and "priority".
 *                                                          Must be used in conjunction with the sortOrder parameter.
 * @param {String} [params.sortOrder]                       Sort the results in a given order. Values may be "asc" for ascending order or "desc" for descending order.
 *                                                          Must be used in conjunction with the sortBy parameter.
 *
 * @param {String} [params.firstResult]                     Pagination of results. Specifies the index of the first result to return.
 * @param {String} [params.maxResults]                      Pagination of results. Specifies the maximum number of results to return.
 *                                                          Will return less results, if there are no more results left.
 * @param {Function} done
 */
Task.list = function(params, done) {
  done = done || noop;
  var deferred = Q.defer();

  this.http.get(this.path, {
    data: params,
    done: function(err, data) {
      if (err) {
        done(err);
        return deferred.reject(err);
      }

      if (data._embedded) {
        // to ease the use of task data, we compile them here
        var tasks = data._embedded.task || data._embedded.tasks;
        var procDefs = data._embedded.processDefinition;

        for (var t in tasks) {
          var task = tasks[t];
          task._embedded = task._embedded || {};
          for (var p in procDefs) {
            if (procDefs[p].id === task.processDefinitionId) {
              task._embedded.processDefinition = [procDefs[p]];
              break;
            }
          }
        }
      }

      done(null, data);
      deferred.resolve(data);
    }
  });

  return deferred.promise;
};

/**
 * Retrieve a single task
 * @param  {uuid}     taskId   of the task to be requested
 * @param  {Function} done
 */
Task.get = function(taskId, done) {
  return this.http.get(this.path + '/' + taskId, {
    done: done
  });
};

/**
 * Retrieve the comments for a single task
 * @param  {uuid}     taskId   of the task for which the comments are requested
 * @param  {Function} done
 */
Task.comments = function(taskId, done) {
  return this.http.get(this.path + '/' + taskId + '/comment', {
    done: done
  });
};

/**
 * Retrieve the identity links for a single task
 * @param  {uuid}     taskId   of the task for which the identity links are requested
 * @param  {Function} done
 */
Task.identityLinks = function(taskId, done) {
  return this.http.get(this.path + '/' + taskId + '/identity-links', {
    done: done
  });
};

/**
 * Add an identity link to a task
 * @param  {uuid}     taskId          of the task for which the identity link is created
 * @param  {Object} [params]
 * @param  {String} [params.userId]   The id of the user to link to the task. If you set this parameter, you have to omit groupId
 * @param  {String} [params.groupId]  The id of the group to link to the task. If you set this parameter, you have to omit userId
 * @param  {String} [params.type]     Sets the type of the link. Must be provided
 * @param  {Function} done
 */
Task.identityLinksAdd = function(taskId, params, done) {
  if (arguments.length === 2) {
    done = arguments[1];
    params = arguments[0];
    taskId = params.id;
  }
  return this.http.post(this.path + '/' + taskId + '/identity-links', {
    data: params,
    done: done
  });
};

/**
 * Removes an identity link from a task.
 * @param  {uuid}     taskId          The id of the task to remove a link from
 * @param  {Object} [params]
 * @param  {String} [params.userId]   The id of the user being part of the link. If you set this parameter, you have to omit groupId.
 * @param  {String} [params.groupId]  The id of the group being part of the link. If you set this parameter, you have to omit userId.
 * @param  {String} [params.type]     Specifies the type of the link. Must be provided.
 * @param  {Function} done
 */
Task.identityLinksDelete = function(taskId, params, done) {
  if (arguments.length === 2) {
    done = arguments[1];
    params = arguments[0];
    taskId = params.id;
  }

  return this.http.post(this.path + '/' + taskId + '/identity-links/delete', {
    data: params,
    done: done
  });
};

/**
 * Create a comment for a task.
 *
 * @param  {String}   taskId  The id of the task to add the comment to.
 * @param  {String}   message The message of the task comment to create.
 * @param  {Function} done
 */
Task.createComment = function(taskId, message, done) {
  return this.http.post(this.path + '/' + taskId + '/comment/create', {
    data: {
      message: message
    },
    done: done
  });
};

/**
 * Creates a task
 *
 * @param  {Object}   task   is an object representation of a task
 * @param  {Function} done
 */
Task.create = function(task, done) {
  return this.http.post(this.path + '/create', {
    data: task,
    done: done
  });
};

/**
 * Update a task
 *
 * @param  {Object}   task   is an object representation of a task
 * @param  {Function} done
 */
Task.update = function(task, done) {
  return this.http.put(this.path + '/' + task.id, {
    data: task,
    done: done
  });
};

// /**
//  * Save a task
//  *
//  * @see Task.create
//  * @see Task.update
//  *
//  * @param  {Object}   task   is an object representation of a task, if it has
//  *                             an id property, the task will be updated, otherwise created
//  * @param  {Function} done
//  */
// Task.save = function(task, done) {
//   return Task[task.id ? 'update' : 'create'](task, done);
// };

/**
 * Change the assignee of a task to a specific user.
 *
 * Note: The difference with claim a task is that
 * this method does not check if the task already has a user assigned to it
 *
 * Note: The response of this call is empty.
 *
 * @param  {String}   taskId
 * @param  {String}   userId
 * @param  {Function} done
 */
Task.assignee = function(taskId, userId, done) {
  var data = {
    userId: userId
  };

  if (arguments.length === 2) {
    taskId = arguments[0].taskId;
    data.userId = arguments[0].userId;
    done = arguments[1];
  }

  return this.http.post(this.path + '/' + taskId + '/assignee', {
    data: data,
    done: done
  });
};

/**
 * Delegate a task to another user.
 *
 * Note: The response of this call is empty.
 *
 * @param  {String}   taskId
 * @param  {String}   userId
 * @param  {Function} done
 */
Task.delegate = function(taskId, userId, done) {
  var data = {
    userId: userId
  };

  if (arguments.length === 2) {
    taskId = arguments[0].taskId;
    data.userId = arguments[0].userId;
    done = arguments[1];
  }

  return this.http.post(this.path + '/' + taskId + '/delegate', {
    data: data,
    done: done
  });
};

/**
 * Claim a task for a specific user.
 *
 * Note: The difference with set a assignee is that
 * here a check is performed to see if the task already has a user assigned to it.
 *
 * Note: The response of this call is empty.
 *
 * @param  {String}   taskId
 * @param  {String}   userId
 * @param  {Function} done
 */
Task.claim = function(taskId, userId, done) {
  var data = {
    userId: userId
  };

  if (arguments.length === 2) {
    taskId = arguments[0].taskId;
    data.userId = arguments[0].userId;
    done = arguments[1];
  }

  return this.http.post(this.path + '/' + taskId + '/claim', {
    data: data,
    done: done
  });
};

/**
 * Resets a task's assignee. If successful, the task is not assigned to a user.
 *
 * Note: The response of this call is empty.
 *
 * @param  {String}   taskId
 * @param  {Function} done
 */
Task.unclaim = function(taskId, done) {
  if (typeof taskId !== 'string') {
    taskId = taskId.taskId;
  }

  return this.http.post(this.path + '/' + taskId + '/unclaim', {
    done: done
  });
};

/**
 * Complete a task and update process variables using a form submit.
 * There are two difference between this method and the complete method:
 *
 * If the task is in state PENDING - ie. has been delegated before,
 * it is not completed but resolved. Otherwise it will be completed.
 *
 * If the task has Form Field Metadata defined,
 * the process engine will perform backend validation for any form fields which have validators defined.
 * See the Generated Task Forms section of the User Guide for more information.
 *
 * @param  {Object}   data
 * @param  {Function} done
 */
Task.submitForm = function(data, done) {
  done = done || noop;
  if (!data.id) {
    var err = new Error('Task submitForm needs a task id.');
    done(err);
    return Q.reject(err);
  }

  return this.http.post(this.path + '/' + data.id + '/submit-form', {
    data: {
      variables: data.variables
    },
    done: done
  });
};

/**
 * Complete a task and update process variables.
 *
 * @param  {object}             [params]
 * @param  {uuid}               [params.id]           Id of the task. This value is mandatory.
 * @param  {Object.<String, *>} [params.variables]    Process variables which need to be updated.
 * @param  {Function} done
 */
Task.complete = function(params, done) {
  done = done || noop;

  if (!params.id) {
    var err = new Error('Task complete needs a task id.');
    done(err);
    return Q.reject(err);
  }

  return this.http.post(this.path + '/' + params.id + '/complete', {
    data: {
      variables: params.variables
    },
    done: done
  });
};

/**
 * Reports an escalation in the context of a running task by id.
 *
 * @param  {Object}             [data]
 * @param  {uuid}               [data.id]             Id of the task. This value is mandatory.
 * @param  {String}             [data.escalationCode] An escalation code that indicates the predefined escalation. This value is mandatory.
 * @param  {Object.<String, *>} [data.variables]      Process variables which need to be updated.
 * @param  {Function}           done
 */
Task.bpmnEscalation = function(data, done) {
  done = done || noop;
  if (!data.id || !data.escalationCode) {
    var err = new Error(
      'Task bpmnEscalation needs a task id and escalation code.'
    );
    done(err);
    return Q.reject(err);
  }

  return this.http.post(this.path + '/' + data.id + '/bpmnEscalation', {
    data: {
      escalationCode: data.escalationCode,
      variables: data.variables
    },
    done: done
  });
};

/**
 * Reports an error in the context of a running task by id.
 *
 * @param  {Object}             [data]
 * @param  {uuid}               [data.id]           Id of the task. This value is mandatory.
 * @param  {String}             [data.errorCode]    An error code that indicates the predefined error. This value is mandatory.
 * @param  {String}             [data.errorMessage] An error message that describes the error.
 * @param  {Object.<String, *>} [data.variables]    Process variables which need to be updated.
 * @param  {Function}           done
 */
Task.bpmnError = function(data, done) {
  done = done || noop;
  if (!data.id || !data.errorCode) {
    var err = new Error('Task bpmnError needs a task id and error code.');
    done(err);
    return Q.reject(err);
  }

  return this.http.post(this.path + '/' + data.id + '/bpmnError', {
    data: {
      variables: data.variables,
      errorCode: data.errorCode,
      errorMessage: data.errorMessage
    },
    done: done
  });
};

Task.formVariables = function(data, done) {
  done = done || noop;
  var pointer = '';
  if (data.key) {
    pointer = 'key/' + data.key;
  } else if (data.id) {
    pointer = data.id;
  } else {
    var err = new Error('Task variables needs either a key or an id.');
    done(err);
    return Q.reject(err);
  }

  var queryData = {
    deserializeValues: data.deserializeValues
  };

  if (data.names) {
    queryData.variableNames = data.names.join(',');
  }

  return this.http.get(this.path + '/' + pointer + '/form-variables', {
    data: queryData,
    done: done
  });
};

/**
 * Retrieve the form for a single task
 * @param  {uuid}     taskId   of the task for which the form is requested
 * @param  {Function} done
 */
Task.form = function(taskId, done) {
  return this.http.get(this.path + '/' + taskId + '/form', {
    done: done
  });
};

/**
 * Sets a variable in the context of a given task.
 * @param {Object} [params]
 * @param {String} [params.id]         The id of the task to set the variable for.
 * @param {String} [params.varId]      The name of the variable to set.
 * @param {String} [params.value]      The variable's value. For variables of type Object, the serialized value has to be submitted as a String value.
 * @param {String} [params.type]       The value type of the variable.
 * @param {String} [params.valueInfo]  A JSON object containing additional, value-type-dependent properties.
 * @param {Function} done
 */
Task.localVariable = function(params, done) {
  return this.http.put(
    this.path + '/' + params.id + '/localVariables/' + params.varId,
    {
      data: params,
      done: done
    }
  );
};

/**
 * Retrieve the local variables for a single task
 * @param  {uuid}     taskId   of the task for which the variables are requested
 * @param  {Function} done
 */
Task.localVariables = function(taskId, done) {
  return this.http.get(this.path + '/' + taskId + '/localVariables', {
    done: done
  });
};

/**
 * Updates or deletes the variables in the context of a task.
 * Updates precede deletions.
 * So, if a variable is updated AND deleted, the deletion overrides the update.
 */
Task.modifyVariables = function(data, done) {
  return this.http.post(this.path + '/' + data.id + '/localVariables', {
    data: data,
    done: done
  });
};

/**
 * Removes a local variable from a task.
 */
Task.deleteVariable = function(data, done) {
  return this.http.del(
    this.path +
      '/' +
      data.id +
      '/localVariables/' +
      utils.escapeUrl(data.varId),
    {
      done: done
    }
  );
};

module.exports = Task;
