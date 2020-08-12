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
 * Incident Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Incident = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Incident.path = 'incident';

/**
 * Query for incidents that fulfill given parameters. The size of the result set can be retrieved by using the get incidents count method.
 *
 * @param  {Object}           params
 *
 * @param  {String}           [params.incidentId]           Restricts to incidents that have the given id.
 *
 * @param  {String}           [params.incidentType]         Restricts to incidents that belong to the given incident type.
 *
 * @param  {String}           [params.incidentMessage]      Restricts to incidents that have the given incident message.
 *
 * @param  {String}           [params.processDefinitionId]  Restricts to incidents that belong to a process definition with the given id.
 *
 * @param  {String}           [params.processInstanceId]    Restricts to incidents that belong to a process instance with the given id.
 *
 * @param  {String}           [params.executionId]          Restricts to incidents that belong to an execution with the given id.
 *
 * @param  {String}           [params.activityId]           Restricts to incidents that belong to an activity with the given id.
 *
 * @param  {String}           [params.causeIncidentId]      Restricts to incidents that have the given incident id as cause incident.
 *
 * @param  {String}           [params.rootCauseIncidentId]  Restricts to incidents that have the given incident id as root cause incident.
 *
 * @param  {String}           [params.configuration]        Restricts to incidents that have the given parameter set as configuration.
 *
 * @param  {String}           [params.sortBy]               Sort the results lexicographically by a given criterion. Valid values are
 *                                                          incidentId, incidentTimestamp, incidentType, executionId, activityId,
 *                                                          processInstanceId, processDefinitionId, causeIncidentId, rootCauseIncidentId
 *                                                          and configuration. Must be used in conjunction with the sortOrder parameter.
 *
 * @param  {String}           [params.sortOrder]            Sort the results in a given order. Values may be asc for ascending order or
 *                                                          desc for descending order. Must be used in conjunction with the sortBy parameter.
 *
 * @param  {String}           [params.firstResult]          Pagination of results. Specifies the
 *                                                          index of the first result to return.
 *
 * @param  {String}           [params.maxResults]           Pagination of results. Specifies the
 *                                                          maximum number of results to return.
 *                                                          Will return less results if there are no
 *                                                          more results left.
 *
 * @param  {RequestCallback}  done
 */
Incident.get = function(params, done) {
  return this.http.get(this.path, {
    data: params,
    done: done
  });
};

/**
 * Query for the number of incidents that fulfill given parameters. Takes the same parameters as the get incidents method.
 *
 * @param  {Object}           params
 *
 * @param  {String}           [params.incidentId]           Restricts to incidents that have the given id.
 *
 * @param  {String}           [params.incidentType]         Restricts to incidents that belong to the given incident type.
 *
 * @param  {String}           [params.incidentMessage]      Restricts to incidents that have the given incident message.
 *
 * @param  {String}           [params.processDefinitionId]  Restricts to incidents that belong to a process definition with the given id.
 *
 * @param  {String}           [params.processInstanceId]    Restricts to incidents that belong to a process instance with the given id.
 *
 * @param  {String}           [params.executionId]          Restricts to incidents that belong to an execution with the given id.
 *
 * @param  {String}           [params.activityId]           Restricts to incidents that belong to an activity with the given id.
 *
 * @param  {String}           [params.causeIncidentId]      Restricts to incidents that have the given incident id as cause incident.
 *
 * @param  {String}           [params.rootCauseIncidentId]  Restricts to incidents that have the given incident id as root cause incident.
 *
 * @param  {String}           [params.configuration]        Restricts to incidents that have the given parameter set as configuration.
 *
 * @param  {RequestCallback}  done
 */
Incident.count = function(params, done) {
  return this.http.get(this.path + '/count', {
    data: params,
    done: done
  });
};

module.exports = Incident;
