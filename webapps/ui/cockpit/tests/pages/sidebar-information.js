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

var Base = require('./base');

module.exports = Base.extend({
  sidebarTabs: function() {
    return element(by.css('.ctn-sidebar .nav-tabs'));
  },

  sidebarTabClick: function(name) {
    return this.sidebarTabs()
      .element(by.cssContainingText('a', name))
      .click();
  },

  listElement: function() {
    return element(by.css('.ctn-sidebar dl'));
  },

  instanceId: function() {
    return this.listElement()
      .element(by.css('.instance-id'))
      .getText();
  },

  definitionId: function() {
    return this.listElement()
      .element(by.css('.definition-id'))
      .getText();
  },

  definitionName: function() {
    return this.listElement()
      .element(by.css('.definition-name'))
      .getText();
  },

  definitionKey: function() {
    return this.listElement()
      .element(by.css('.definition-key'))
      .getText();
  },

  definitionVersion: function() {
    return this.listElement()
      .element(by.css('.definition-version'))
      .getText();
  },

  definitionVersionDropdownButton: function() {
    return this.listElement().element(
      by.css('.definition-version .dropdown-toggle')
    );
  },

  definitionVersionDropdownButtonText: function() {
    return this.definitionVersionDropdownButton().getText();
  },

  definitionVersionDropdownOptions: function() {
    return this.listElement().all(
      by.css('.definition-version .dropdown-menu li')
    );
  },

  definitionInstancesCurrent: function() {
    return this.listElement()
      .element(by.css('.current-version'))
      .getText();
  },

  definitionInstancesAll: function() {
    return this.listElement()
      .element(by.css('.all-versions'))
      .getText();
  },

  deploymentId: function() {
    return this.listElement()
      .element(by.css('.deployment-id'))
      .getText();
  },

  tenantId: function() {
    return this.listElement()
      .element(by.css('.tenant-id'))
      .getText();
  },

  versionTag: function() {
    return this.listElement()
      .element(by.css('.version-tag'))
      .getText();
  }
});
