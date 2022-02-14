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

var Viewer = require('bpmn-js/lib/NavigatedViewer').default;

var viewers = {};

module.exports = {
  generateViewer: generateViewer,
  cacheViewer: cacheViewer
};

function generateViewer(options) {
  // return a new bpmn viewer
  var BpmnViewer = Viewer;
  if (options.disableNavigation) {
    BpmnViewer = Object.getPrototypeOf(Viewer.prototype).constructor;
  }
  return new BpmnViewer(options);
}

function cacheViewer(options) {
  return options.key && (viewers[options.key] = options.viewer);
}
