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
 * Variable Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var Variable = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
Variable.path = 'variable-instance';

/**
 * Get variable instances
 *
 * @param  {Object}           params
 *
 * @param  {String}           [params.variableName]         Filter by variable instance name.
 *
 * @param  {String}           [params.variableNameLike]     Filter by the variable instance name.
 *                                                          The parameter can include the wildcard %
 *                                                          to express like-strategy such as:
 *                                                          - starts with (%name)
 *                                                          - ends with (name%)
 *                                                          - contains (%name%).
 *
 * @param  {String[]}         [params.processInstanceIdIn]  Only include variable instances which
 *                                                          belong to one of the passed and
 *                                                          comma-separated process instance ids.
 *
 * @param  {String[]}         [params.executionIdIn]        Only include variable instances which
 *                                                          belong to one of the passed and
 *                                                          comma-separated execution ids.
 *
 * @param  {String[]}         [params.caseInstanceIdIn]     Only include variable instances which
 *                                                          belong to one of the passed
 *                                                          case instance ids.
 *
 * @param  {String[]}         [params.caseExecutionIdIn]    Only include variable instances which
 *                                                          belong to one of the passed
 *                                                          case execution ids.
 *
 * @param  {String[]}         [params.taskIdIn]             Only include variable instances which
 *                                                          belong to one of the passed and
 *                                                          comma-separated task ids.
 *
 * @param  {String[]}         [params.activityInstanceIdIn] Only include variable instances which
 *                                                          belong to one of the passed and
 *                                                          comma-separated activity instance ids.
 *
 * @param  {String}           [params.variableValues]       Only include variable instances that
 *                                                          have the certain values. Value filtering
 *                                                          expressions are comma-separated and are
 *                                                          structured as follows:
 *                                                          A valid parameter value has the form
 *                                                          key_operator_value.
 *                                                          key is the variable name,
 *                                                          operator is the comparison operator to
 *                                                          be used and value the variable value.
 *                                                          *Note*: Values are always treated as
 *                                                          String objects on server side.
 *                                                          Valid operator values are:
 *                                                          - eq - equal to
 *                                                          - neq - not equal to
 *                                                          - gt - greater than
 *                                                          - gteq - greater than or equal to
 *                                                          - lt - lower than
 *                                                          - lteq - lower than or equal to
 *                                                          key and value may not contain underscore
 *                                                          or comma characters.
 *
 * @param  {String}           [params.sortBy]               Sort the results lexicographically by a
 *                                                          given criterion. Valid values are
 *                                                          variableName, variableType and
 *                                                          activityInstanceId.
 *                                                          Must be used in conjunction with the
 *                                                          sortOrder parameter.
 *
 * @param  {String}           [params.sortOrder]            Sort the results in a given order.
 *                                                          Values may be asc for ascending order or
 *                                                          desc for descending order.
 *                                                          Must be used in conjunction with the
 *                                                          sortBy parameter.
 *
 * @param  {String}           [params.firstResult]          Pagination of results. Specifies the
 *                                                          index of the first result to return.
 *
 * @param  {String}           [params.maxResults]           Pagination of results. Specifies the
 *                                                          maximum number of results to return.
 *                                                          Will return less results if there are no
 *                                                          more results left.
 *
 * @param  {String}           [params.deserializeValues]    Determines whether serializable variable
 *                                                          values (typically variables that store
 *                                                          custom Java objects) should be
 *                                                          deserialized on server side
 *                                                          (default true).
 *                                                          If set to true, a serializable variable
 *                                                          will be deserialized on server side and
 *                                                          transformed to JSON using
 *                                                          Jackson's POJO/bean property
 *                                                          introspection feature.
 *                                                          Note that this requires the Java classes
 *                                                          of the variable value to be on the
 *                                                          REST API's classpath.
 *                                                          If set to false, a serializable variable
 *                                                          will be returned in its serialized
 *                                                          format.
 *                                                          For example, a variable that is
 *                                                          serialized as XML will be returned as a
 *                                                          JSON string containing XML.
 *                                                          Note:While true is the default value for
 *                                                          reasons of backward compatibility, we
 *                                                          recommend setting this parameter to
 *                                                          false when developing web applications
 *                                                          that are independent of the Java process
 *                                                          applications deployed to the engine.
 *
 * @param  {RequestCallback}  done
 */
Variable.instances = function(params, done) {
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

  return this.http.post(this.path, {
    data: body,
    query: query,
    done: done
  });
};

/**
 * Get a count of variables
 * Same parameters as instances
 */

Variable.count = function(params, done) {
  var path = this.path + '/count';

  return this.http.post(path, {
    data: params,
    done: done
  });
};

module.exports = Variable;
