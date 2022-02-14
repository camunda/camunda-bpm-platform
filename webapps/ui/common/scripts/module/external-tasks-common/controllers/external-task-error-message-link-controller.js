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

module.exports = [
  '$scope',
  'Uri',
  'exposeScopeProperties',
  ExternalTaskErrorMessageLink
];

function ExternalTaskErrorMessageLink($scope, Uri, exposeScopeProperties) {
  exposeScopeProperties($scope, this, ['taskId', 'historic']);

  this.Uri = Uri;
}

ExternalTaskErrorMessageLink.prototype.getStacktraceUrl = function() {
  var abstractUrl =
    'engine://engine/:engine/external-task/' + this.taskId + '/errorDetails';

  if (this.historic) {
    abstractUrl =
      'engine://engine/:engine/history/external-task-log/' +
      this.taskId +
      '/error-details';
  }

  return this.Uri.appUri(abstractUrl);
};
