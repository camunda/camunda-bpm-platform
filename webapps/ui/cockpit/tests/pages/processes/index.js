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

var Base = require('./../base');
var DeployedProcessesListPage = require('./deployed-processes-list');
var DeployedProcessesPreviewsPage = require('./deployed-processes-previews');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

var Page = Base.extend({
  url: '/camunda/app/cockpit/default/#/processes',

  pluginList: function() {
    return element.all(by.css('.dashboard'));
  }
});

module.exports = new Page();

module.exports.deployedProcessesList = new DeployedProcessesListPage();
module.exports.deployedProcessesPreviews = new DeployedProcessesPreviewsPage();
module.exports.authentication = new AuthenticationPage();
