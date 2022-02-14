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
 * Task Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var TaskReport = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
TaskReport.path = 'task/report';

/**
 * Fetch the count of tasks grouped by candidate group.
 *
 * @param {Function} done
 */
TaskReport.countByCandidateGroup = function(done) {
  return this.http.get(this.path + '/candidate-group-count', {
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
TaskReport.countByCandidateGroupAsCsv = function(done) {
  return this.http.get(this.path + '/candidate-group-count', {
    accept: 'text/csv',
    done: done
  });
};

module.exports = TaskReport;
