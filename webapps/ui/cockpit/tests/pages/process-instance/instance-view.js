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

module.exports = Base.extend({
  url: '/camunda/app/cockpit/default/#/process-instance/:instance/runtime',

  pageHeader: function() {
    return element(by.binding('{{ processInstance.id }}'));
  },

  fullPageHeaderProcessInstanceName: function() {
    return this.pageHeader().getText();
  },

  pageHeaderProcessInstanceName: function() {
    return element(by.binding('{{ processInstance.id }}'))
      .getText()
      .then(function(fullString) {
        return fullString.replace('<', '').replace('>', '');
      });
  },

  processName: function() {
    return element(
      by.binding('{{ processDefinition.name || processDefinition.key }}')
    ).getText();
  },

  instanceId: function() {
    return element(by.binding('{{ processInstance.id }}'))
      .getText()
      .then(function(fullString) {
        return fullString.replace('<', '').replace('>', '');
      });
  },

  businessKey: function() {
    return element(by.binding('{{ processInstance.businessKey}}'))
      .getText()
      .then(function(fullString) {
        return fullString.replace('<', '').replace('>', '');
      });
  },

  isInstanceSuspended: function() {
    return element(
      by.css('.cam-breadcrumb .active .badge-suspended')
    ).isPresent();
  },

  sidebarTabClick: function(name) {
    return element(
      by.cssContainingText('.ctn-sidebar .nav-tabs a', name)
    ).click();
  }
});
