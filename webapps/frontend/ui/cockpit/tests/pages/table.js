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

var Table = require('./base');

module.exports = Table.extend({
  tableTabs: function() {
    return element.all(by.repeater(this.tabRepeater));
  },

  selectTab: function() {
    return this.tableTabs()
      .get(this.tabIndex)
      .click();
  },

  tabSelectionStatus: function() {
    return this.tableTabs(this.tabRepeater)
      .get(this.tabIndex)
      .getAttribute('class');
  },

  isTabSelected: function() {
    return this.tabSelectionStatus().then(function(classes) {
      return classes.indexOf('active') !== -1;
    });
  },

  tabName: function() {
    return this.tableTabs(this.repeater)
      .get(this.tabIndex)
      .element(by.css('[class="ng-binding"]'))
      .getText();
  },

  table: function() {
    return element(by.css('.ctn-tabbed-content')).all(
      by.repeater(this.tableRepeater)
    );
  },

  tableItem: function(item, elementSelector) {
    if (arguments.length === 1) {
      return this.table().get(item);
    }

    if (typeof elementSelector === 'string') {
      elementSelector = by.css(elementSelector);
    }

    return this.table()
      .get(item)
      .element(elementSelector);
  }
});
